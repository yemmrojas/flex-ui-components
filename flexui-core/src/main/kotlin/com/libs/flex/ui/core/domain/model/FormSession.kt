package com.libs.flex.ui.core.domain.model

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.event.DomainEvent
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.model.node.CheckNode
import com.libs.flex.ui.core.domain.model.node.InputCapableNode
import com.libs.flex.ui.core.domain.model.node.InputNode
import com.libs.flex.ui.core.domain.model.node.SelectNode
import com.libs.flex.ui.core.domain.model.node.SliderNode
import com.libs.flex.ui.core.domain.ports.ActionResult
import com.libs.flex.ui.core.domain.ports.ClockPort
import com.libs.flex.ui.core.domain.ports.UserAction
import com.libs.flex.ui.core.domain.validation.ValidationError
import com.libs.flex.ui.core.domain.value.ActionId
import com.libs.flex.ui.core.domain.value.FieldValue
import com.libs.flex.ui.core.domain.value.NodeKey
import com.libs.flex.ui.core.domain.value.SessionId

public class FormSession private constructor(
    public val sessionId: SessionId,
    public val document: LayoutDocument,
    private val state: FormInteractionState
) {
    public val formValues: Map<NodeKey, FieldValue> get() = state.values
    public val formErrors: Map<NodeKey, ValidationError> get() = state.errors
    public val isValid: Boolean get() = state.isValid()

    private fun copyWith(newState: FormInteractionState): FormSession =
        FormSession(sessionId, document, newState)

    public fun updateInput(
        key: NodeKey,
        value: FieldValue,
        occurredAt: Long
    ): Either<DomainError, FormOutcome> {
        val node = document.findNode(key.componentId)
            ?: return Either.Left(DomainError.Interaction.ComponentNotFound(key.componentId))

        if (node !is InputCapableNode) {
            return Either.Left(DomainError.Interaction.ComponentNotInputCapable(key.componentId))
        }

        val resolvedValue = decodeRawValueIfNeeded(node, value)

        when (val acceptResult = node.accept(resolvedValue)) {
            is Either.Left -> return Either.Left(acceptResult.value)
            is Either.Right -> { /* Accepted by node invariants */ }
        }

        val stateOutcome = state.update(key, resolvedValue, node, occurredAt)
        val newSession = copyWith(stateOutcome.state)
        return Either.Right(FormOutcome(newSession, stateOutcome.events))
    }

    private fun decodeRawValueIfNeeded(node: InputCapableNode, value: FieldValue): FieldValue {
        if (value !is FieldValue.Text) return value
        val raw = value.value.trim()
        return when (node) {
            is CheckNode -> when {
                raw.equals("true", ignoreCase = true) || raw == "1" -> FieldValue.Bool(true)
                raw.equals("false", ignoreCase = true) || raw == "0" -> FieldValue.Bool(false)
                else -> value
            }
            is SliderNode -> {
                val number = raw.toDoubleOrNull()
                if (number != null) FieldValue.Number(number) else value
            }
            is SelectNode -> {
                if (raw.isEmpty()) {
                    FieldValue.Selection(emptyList())
                } else {
                    val indices = raw.split(",").mapNotNull { it.trim().toIntOrNull() }
                    if (indices.isNotEmpty()) FieldValue.Selection(indices) else value
                }
            }
            is InputNode -> value
            else -> value
        }
    }

    public fun requestAction(
        key: NodeKey,
        occurredAt: Long
    ): Either<DomainError, ActionDecision> {
        val node = document.findInteractiveNode(key.componentId)
            ?: return Either.Left(DomainError.Interaction.ComponentNotInteractive(key.componentId))

        if (!node.isEnabled) {
            return Either.Left(DomainError.Interaction.ComponentDisabled(key.componentId))
        }

        val currentSession = if (node.requiresValidation) {
            val validatedState = state.validateAll(document)
            if (!validatedState.isValid()) {
                val newSession = copyWith(validatedState)
                val blockedOutcome = FormOutcome(newSession, emptyList())
                return Either.Right(ActionDecision.Blocked(blockedOutcome))
            }
            copyWith(validatedState)
        } else {
            this
        }

        val userAction = UserAction(
            actionId = node.actionId,
            sourceKey = key,
            formValues = currentSession.formValues
        )

        val dispatchEvent = DomainEvent.UserActionDispatchedEvent(node.actionId, key, occurredAt)
        val dispatchOutcome = FormOutcome(currentSession, listOf(dispatchEvent))

        return Either.Right(ActionDecision.Dispatch(userAction, dispatchOutcome))
    }

    public fun recordActionResult(
        actionId: ActionId,
        sourceKey: NodeKey,
        result: ActionResult,
        occurredAt: Long
    ): FormOutcome {
        val events = mutableListOf<DomainEvent>()
        when (result) {
            is ActionResult.Handled -> {}
            is ActionResult.Rejected -> events.add(
                DomainEvent.UserActionRejectedEvent(actionId, sourceKey, result.reason, occurredAt)
            )
            is ActionResult.Unhandled -> events.add(
                DomainEvent.UserActionUnhandledEvent(actionId, sourceKey, occurredAt)
            )
            is ActionResult.Blocked -> {}
        }
        return FormOutcome(this, events)
    }

    public companion object {
        public fun start(
            sessionId: SessionId,
            document: LayoutDocument,
            clock: ClockPort
        ): FormOutcome {
            val initialValues = mutableMapOf<NodeKey, FieldValue>()
            document.allInputNodes().forEach { node ->
                if (node is CheckNode && node.defaultChecked) {
                    initialValues[NodeKey(node.id)] = FieldValue.Bool(true)
                }
            }
            val initialState = FormInteractionState.create(values = initialValues)
            val session = FormSession(sessionId, document, initialState)
            val events = listOf<DomainEvent>(
                DomainEvent.FormSessionStartedEvent(
                    sessionId = sessionId,
                    layoutVersion = document.schemaVersion,
                    timestamp = clock.currentTimeMillis()
                )
            )
            return FormOutcome(session, events)
        }

        internal fun reconstitute(
            sessionId: SessionId,
            document: LayoutDocument,
            values: Map<NodeKey, FieldValue> = emptyMap(),
            errors: Map<NodeKey, ValidationError> = emptyMap()
        ): FormSession = FormSession(sessionId, document, FormInteractionState.create(values, errors))
    }
}

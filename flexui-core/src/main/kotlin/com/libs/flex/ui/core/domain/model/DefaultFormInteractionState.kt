package com.libs.flex.ui.core.domain.model

import com.libs.flex.ui.core.domain.event.DomainEvent
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.model.node.InputCapableNode
import com.libs.flex.ui.core.domain.validation.ValidationError
import com.libs.flex.ui.core.domain.value.FieldValue
import com.libs.flex.ui.core.domain.value.NodeKey

internal class DefaultFormInteractionState internal constructor(
    override val values: Map<NodeKey, FieldValue> = emptyMap(),
    override val errors: Map<NodeKey, ValidationError> = emptyMap()
) : FormInteractionState {

    override fun isValid(): Boolean = errors.isEmpty()

    override fun update(
        key: NodeKey,
        value: FieldValue,
        node: InputCapableNode,
        occurredAt: Long
    ): InternalStateOutcome {
        val validationError = node.validationRules.asSequence()
            .map { it.validate(value) }
            .filterIsInstance<Either.Left<ValidationError>>()
            .firstOrNull()?.value
        val previousError = errors[key]
        val newErrors = if (validationError != null) {
            errors + (key to validationError)
        } else {
            errors - key
        }
        val newValues = values + (key to value)
        val newState = DefaultFormInteractionState(newValues, newErrors)

        val events = mutableListOf<DomainEvent>()

        val eventValue = if (node.isSensitive) FieldValue.Text("••••••••") else value
        events.add(DomainEvent.InputValueChangedEvent(key, eventValue, occurredAt))

        if (validationError != null && previousError == null) {
            events.add(DomainEvent.InputValidationFailedEvent(key, validationError.message, occurredAt))
        } else if (validationError == null && previousError != null) {
            events.add(DomainEvent.InputValidationRecoveredEvent(key, occurredAt))
        }

        return InternalStateOutcome(newState, events)
    }

    override fun validateAll(document: LayoutDocument): FormInteractionState {
        val newErrors = mutableMapOf<NodeKey, ValidationError>()

        fun validateKey(node: InputCapableNode, key: NodeKey) {
            val currentValue = values[key]
            val err = node.validationRules.asSequence()
                .map { it.validate(currentValue) }
                .filterIsInstance<Either.Left<ValidationError>>()
                .firstOrNull()?.value
            if (err != null) {
                newErrors[key] = err
            }
        }

        document.standaloneInputNodes().forEach { node ->
            validateKey(node, NodeKey(node.id))
        }

        values.keys
            .filter { it.instanceIndex != null }
            .forEach { key ->
                val node = document.findNode(key.componentId) as? InputCapableNode
                if (node != null) {
                    validateKey(node, key)
                }
            }

        return DefaultFormInteractionState(values, newErrors)
    }
}

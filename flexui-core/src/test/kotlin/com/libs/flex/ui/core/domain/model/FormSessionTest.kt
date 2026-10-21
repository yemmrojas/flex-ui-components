package com.libs.flex.ui.core.domain.model

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.event.DomainEvent
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.model.node.ButtonNode
import com.libs.flex.ui.core.domain.model.node.CheckNode
import com.libs.flex.ui.core.domain.model.node.InputNode
import com.libs.flex.ui.core.domain.model.node.SliderNode
import com.libs.flex.ui.core.domain.model.node.TextNode
import com.libs.flex.ui.core.domain.model.node.VerticalLayoutNode
import com.libs.flex.ui.core.domain.ports.ActionResult
import com.libs.flex.ui.core.domain.ports.UserAction
import com.libs.flex.ui.core.domain.validation.MinLengthRule
import com.libs.flex.ui.core.domain.validation.RequiredRule
import com.libs.flex.ui.core.domain.validation.ValidationError
import com.libs.flex.ui.core.domain.value.ActionId
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.FieldValue
import com.libs.flex.ui.core.domain.value.InputVariant
import com.libs.flex.ui.core.domain.value.NodeKey
import com.libs.flex.ui.core.domain.value.NumberRange
import com.libs.flex.ui.core.domain.value.SchemaVersion
import com.libs.flex.ui.core.domain.value.SessionId
import com.libs.flex.ui.core.fakes.FakeClock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FormSessionTest {

    private val clock = FakeClock(currentTime = 5000L)
    private val sessionId = SessionId.of("session-1").fold({ error("invalid id") }, { it })
    private val schemaVersion = SchemaVersion.CURRENT

    private val textInputId = ComponentId.of("input_name").fold({ error("invalid id") }, { it })
    private val passwordInputId = ComponentId.of("input_pwd").fold({ error("invalid id") }, { it })
    private val checkAgreeId = ComponentId.of("check_agree").fold({ error("invalid id") }, { it })
    private val checkOptId = ComponentId.of("check_opt").fold({ error("invalid id") }, { it })
    private val sliderAgeId = ComponentId.of("slider_age").fold({ error("invalid id") }, { it })
    private val labelTextId = ComponentId.of("label_title").fold({ error("invalid id") }, { it })
    private val submitBtnId = ComponentId.of("btn_submit").fold({ error("invalid id") }, { it })
    private val cancelBtnId = ComponentId.of("btn_cancel").fold({ error("invalid id") }, { it })
    private val submitActionId = ActionId.of("action.submit").fold({ error("invalid id") }, { it })
    private val cancelActionId = ActionId.of("action.cancel").fold({ error("invalid id") }, { it })

    private fun createSampleDocument(): LayoutDocument {
        val minLengthRule = MinLengthRule.of(3).fold({ error("invalid rule") }, { it })
        val requiredRule = RequiredRule("Name is required")

        val textInput = InputNode(
            id = textInputId,
            label = "Name",
            validationRules = listOf(requiredRule, minLengthRule)
        )

        val passwordInput = InputNode(
            id = passwordInputId,
            label = "Password",
            inputVariant = InputVariant.Password,
            validationRules = listOf(RequiredRule("Password required"))
        )

        val checkAgree = CheckNode(
            id = checkAgreeId,
            label = "Agree to terms",
            defaultChecked = true
        )

        val checkOpt = CheckNode(
            id = checkOptId,
            label = "Receive newsletter",
            defaultChecked = false
        )

        val slider = SliderNode(
            id = sliderAgeId,
            range = NumberRange.of(18.0, 100.0, 1.0).fold({ error("invalid range") }, { it })
        )

        val label = TextNode(
            id = labelTextId,
            text = "Welcome Form"
        )

        val submitButton = ButtonNode(
            id = submitBtnId,
            text = "Submit",
            actionId = submitActionId,
            requiresValidation = true
        )

        val cancelButton = ButtonNode(
            id = cancelBtnId,
            text = "Cancel",
            actionId = cancelActionId,
            requiresValidation = false
        )

        val root = VerticalLayoutNode(
            id = ComponentId.of("root_layout").fold({ error("invalid id") }, { it }),
            children = listOf(label, textInput, passwordInput, checkAgree, checkOpt, slider, submitButton, cancelButton)
        )

        return LayoutDocument.create(schemaVersion, root).fold(
            { errors -> error("Failed to create document: ${errors.toList()}") },
            { it }
        )
    }

    @Test
    fun `start seeds defaultChecked values and emits FormSessionStartedEvent`() {
        val document = createSampleDocument()
        val outcome = FormSession.start(sessionId, document, clock)
        val session = outcome.session

        assertTrue(session.isValid)
        assertEquals(1, session.formValues.size)
        assertEquals(FieldValue.Bool(true), session.formValues[NodeKey(checkAgreeId)])
        assertNull(session.formValues[NodeKey(checkOptId)])

        assertEquals(1, outcome.events.size)
        val event = outcome.events.first() as DomainEvent.FormSessionStartedEvent
        assertEquals(sessionId, event.sessionId)
        assertEquals(schemaVersion, event.layoutVersion)
        assertEquals(5000L, event.timestamp)
    }

    @Test
    fun `FormSession has referential equality`() {
        val document = createSampleDocument()
        val session1 = FormSession.start(sessionId, document, clock).session
        val session2 = FormSession.start(sessionId, document, clock).session

        assertNotEquals(session1, session2)
        assertEquals(session1, session1)
    }

    @Test
    fun `updateInput returns ComponentNotFound when component does not exist`() {
        val document = createSampleDocument()
        val session = FormSession.start(sessionId, document, clock).session
        val unknownId = ComponentId.of("unknown_comp").fold({ error("invalid id") }, { it })

        val result = session.updateInput(NodeKey(unknownId), FieldValue.Text("val"), 6000L)

        assertTrue(result.isLeft)
        val error = (result as Either.Left).value
        assertTrue(error is DomainError.Interaction.ComponentNotFound)
        assertEquals(unknownId, (error as DomainError.Interaction.ComponentNotFound).id)
    }

    @Test
    fun `updateInput returns ComponentNotInputCapable when target is not an input`() {
        val document = createSampleDocument()
        val session = FormSession.start(sessionId, document, clock).session

        val result = session.updateInput(NodeKey(labelTextId), FieldValue.Text("val"), 6000L)

        assertTrue(result.isLeft)
        val error = (result as Either.Left).value
        assertTrue(error is DomainError.Interaction.ComponentNotInputCapable)
        assertEquals(labelTextId, (error as DomainError.Interaction.ComponentNotInputCapable).id)
    }

    @Test
    fun `updateInput returns ValueTypeMismatch when input node rejects field value type`() {
        val document = createSampleDocument()
        val session = FormSession.start(sessionId, document, clock).session

        val result = session.updateInput(NodeKey(textInputId), FieldValue.Bool(true), 6000L)

        assertTrue(result.isLeft)
        val error = (result as Either.Left).value
        assertTrue(error is DomainError.Value.ValueTypeMismatch)
    }

    @Test
    fun `updateInput updates form value and emits plain InputValueChangedEvent for non-sensitive input`() {
        val document = createSampleDocument()
        val session = FormSession.start(sessionId, document, clock).session
        val key = NodeKey(textInputId)

        val result = session.updateInput(key, FieldValue.Text("John Doe"), 6000L)

        assertTrue(result.isRight)
        val outcome = (result as Either.Right).value
        assertEquals(FieldValue.Text("John Doe"), outcome.session.formValues[key])
        assertTrue(outcome.session.isValid)

        assertEquals(1, outcome.events.size)
        val event = outcome.events.first() as DomainEvent.InputValueChangedEvent
        assertEquals(key, event.key)
        assertEquals(FieldValue.Text("John Doe"), event.value)
        assertEquals(6000L, event.timestamp)
    }

    @Test
    fun `updateInput masks sensitive password in event while storing real value in session`() {
        val document = createSampleDocument()
        val session = FormSession.start(sessionId, document, clock).session
        val key = NodeKey(passwordInputId)

        val result = session.updateInput(key, FieldValue.Text("super-secret-123"), 6000L)

        assertTrue(result.isRight)
        val outcome = (result as Either.Right).value
        assertEquals(FieldValue.Text("super-secret-123"), outcome.session.formValues[key])

        assertEquals(1, outcome.events.size)
        val event = outcome.events.first() as DomainEvent.InputValueChangedEvent
        assertEquals(key, event.key)
        assertEquals(FieldValue.Text("••••••••"), event.value)
        assertEquals(6000L, event.timestamp)
    }

    @Test
    fun `updateInput handles validation transitions correctly`() {
        val document = createSampleDocument()
        var session = FormSession.start(sessionId, document, clock).session
        val key = NodeKey(textInputId)

        // 1. Valid to Invalid (violating MinLengthRule min=3)
        val result1 = session.updateInput(key, FieldValue.Text("AB"), 6000L)
        assertTrue(result1.isRight)
        val outcome1 = (result1 as Either.Right).value
        session = outcome1.session

        assertFalse(session.isValid)
        assertNotNull(session.formErrors[key])
        assertTrue(session.formErrors[key] is ValidationError.MinLength)

        assertEquals(2, outcome1.events.size)
        assertTrue(outcome1.events[0] is DomainEvent.InputValueChangedEvent)
        val failEvent = outcome1.events[1] as DomainEvent.InputValidationFailedEvent
        assertEquals(key, failEvent.key)
        assertEquals(6000L, failEvent.timestamp)

        // 2. Invalid to Invalid (still length < 3) -> no new InputValidationFailedEvent
        val result2 = session.updateInput(key, FieldValue.Text("A"), 7000L)
        assertTrue(result2.isRight)
        val outcome2 = (result2 as Either.Right).value
        session = outcome2.session

        assertFalse(session.isValid)
        assertEquals(1, outcome2.events.size)
        assertTrue(outcome2.events[0] is DomainEvent.InputValueChangedEvent)

        // 3. Invalid to Valid -> emits InputValidationRecoveredEvent
        val result3 = session.updateInput(key, FieldValue.Text("Alice"), 8000L)
        assertTrue(result3.isRight)
        val outcome3 = (result3 as Either.Right).value
        session = outcome3.session

        assertTrue(session.isValid)
        assertNull(session.formErrors[key])
        assertEquals(2, outcome3.events.size)
        assertTrue(outcome3.events[0] is DomainEvent.InputValueChangedEvent)
        val recoveredEvent = outcome3.events[1] as DomainEvent.InputValidationRecoveredEvent
        assertEquals(key, recoveredEvent.key)
        assertEquals(8000L, recoveredEvent.timestamp)

        // 4. Valid to Valid -> emits InputValueChangedEvent only
        val result4 = session.updateInput(key, FieldValue.Text("Bob"), 9000L)
        assertTrue(result4.isRight)
        val outcome4 = (result4 as Either.Right).value
        assertEquals(1, outcome4.events.size)
        assertTrue(outcome4.events[0] is DomainEvent.InputValueChangedEvent)
    }

    @Test
    fun `requestAction returns ComponentNotInteractive when node is not interactive`() {
        val document = createSampleDocument()
        val session = FormSession.start(sessionId, document, clock).session

        val result = session.requestAction(NodeKey(textInputId), 6000L)

        assertTrue(result.isLeft)
        val error = (result as Either.Left).value
        assertTrue(error is DomainError.Interaction.ComponentNotInteractive)
    }

    @Test
    fun `requestAction returns ComponentDisabled when node is disabled`() {
        val disabledBtn = ButtonNode(
            id = ComponentId.of("disabled_btn").fold({ error("invalid id") }, { it }),
            text = "Disabled",
            actionId = submitActionId,
            isEnabled = false
        )
        val doc = LayoutDocument.create(schemaVersion, disabledBtn).fold({ error("doc fail") }, { it })
        val session = FormSession.start(sessionId, doc, clock).session

        val result = session.requestAction(NodeKey(disabledBtn.id), 6000L)

        assertTrue(result.isLeft)
        val error = (result as Either.Left).value
        assertTrue(error is DomainError.Interaction.ComponentDisabled)
    }

    @Test
    fun `requestAction dispatches immediately when requiresValidation is false`() {
        val document = createSampleDocument()
        val session = FormSession.start(sessionId, document, clock).session

        // Cancel button does not require validation, even if required fields are empty
        val result = session.requestAction(NodeKey(cancelBtnId), 6000L)

        assertTrue(result.isRight)
        val decision = (result as Either.Right).value
        assertTrue(decision is ActionDecision.Dispatch)
        val dispatch = decision as ActionDecision.Dispatch

        assertEquals(cancelActionId, dispatch.action.actionId)
        assertEquals(NodeKey(cancelBtnId), dispatch.action.sourceKey)
        assertEquals(1, dispatch.outcome.events.size)
        val event = dispatch.outcome.events.first() as DomainEvent.UserActionDispatchedEvent
        assertEquals(cancelActionId, event.actionId)
        assertEquals(6000L, event.timestamp)
    }

    @Test
    fun `requestAction blocks and marks errors when requiresValidation is true and form is invalid`() {
        val document = createSampleDocument()
        val session = FormSession.start(sessionId, document, clock).session

        // Submit button requires validation, textInput and passwordInput are required but not filled
        val result = session.requestAction(NodeKey(submitBtnId), 6000L)

        assertTrue(result.isRight)
        val decision = (result as Either.Right).value
        assertTrue(decision is ActionDecision.Blocked)
        val blocked = decision as ActionDecision.Blocked

        assertFalse(blocked.outcome.session.isValid)
        assertTrue(blocked.outcome.session.formErrors.containsKey(NodeKey(textInputId)))
        assertTrue(blocked.outcome.session.formErrors.containsKey(NodeKey(passwordInputId)))
        assertTrue(blocked.outcome.events.isEmpty())
    }

    @Test
    fun `requestAction dispatches when requiresValidation is true and all fields are valid`() {
        val document = createSampleDocument()
        var session = FormSession.start(sessionId, document, clock).session

        session = (session.updateInput(NodeKey(textInputId), FieldValue.Text("Valid Name"), 6000L) as Either.Right).value.session
        session = (session.updateInput(NodeKey(passwordInputId), FieldValue.Text("Secr3t!"), 6000L) as Either.Right).value.session

        val result = session.requestAction(NodeKey(submitBtnId), 7000L)

        assertTrue(result.isRight)
        val decision = (result as Either.Right).value
        assertTrue(decision is ActionDecision.Dispatch)
        val dispatch = decision as ActionDecision.Dispatch

        assertEquals(submitActionId, dispatch.action.actionId)
        assertEquals(FieldValue.Text("Valid Name"), dispatch.action.formValues[NodeKey(textInputId)])
        assertEquals(FieldValue.Text("Secr3t!"), dispatch.action.formValues[NodeKey(passwordInputId)])
        assertEquals(1, dispatch.outcome.events.size)
        val event = dispatch.outcome.events.first() as DomainEvent.UserActionDispatchedEvent
        assertEquals(submitActionId, event.actionId)
        assertEquals(7000L, event.timestamp)
    }

    @Test
    fun `recordActionResult creates appropriate audit events`() {
        val document = createSampleDocument()
        val session = FormSession.start(sessionId, document, clock).session
        val key = NodeKey(submitBtnId)

        // Handled -> no events
        val handledOutcome = session.recordActionResult(submitActionId, key, ActionResult.Handled, 8000L)
        assertTrue(handledOutcome.events.isEmpty())

        // Blocked -> no events
        val blockedOutcome = session.recordActionResult(submitActionId, key, ActionResult.Blocked(emptyMap()), 8000L)
        assertTrue(blockedOutcome.events.isEmpty())

        // Rejected -> UserActionRejectedEvent
        val rejectedOutcome = session.recordActionResult(submitActionId, key, ActionResult.Rejected("Network timeout"), 8000L)
        assertEquals(1, rejectedOutcome.events.size)
        val rejectedEvent = rejectedOutcome.events.first() as DomainEvent.UserActionRejectedEvent
        assertEquals(submitActionId, rejectedEvent.actionId)
        assertEquals(key, rejectedEvent.sourceKey)
        assertEquals("Network timeout", rejectedEvent.reason)
        assertEquals(8000L, rejectedEvent.timestamp)

        // Unhandled -> UserActionUnhandledEvent
        val unhandledOutcome = session.recordActionResult(submitActionId, key, ActionResult.Unhandled, 9000L)
        assertEquals(1, unhandledOutcome.events.size)
        val unhandledEvent = unhandledOutcome.events.first() as DomainEvent.UserActionUnhandledEvent
        assertEquals(submitActionId, unhandledEvent.actionId)
        assertEquals(key, unhandledEvent.sourceKey)
        assertEquals(9000L, unhandledEvent.timestamp)
    }

    @Test
    fun `reconstitute restores session with custom values and errors`() {
        val document = createSampleDocument()
        val key = NodeKey(textInputId)
        val error = ValidationError.Required("Custom error")
        val values = mapOf(key to FieldValue.Text("Saved Value"))
        val errors = mapOf(key to error)

        val session = FormSession.reconstitute(sessionId, document, values, errors)

        assertEquals(sessionId, session.sessionId)
        assertEquals(document, session.document)
        assertEquals(values, session.formValues)
        assertEquals(errors, session.formErrors)
        assertFalse(session.isValid)
    }
}

package com.libs.flex.ui.core.application.usecase

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.event.DomainEvent
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.model.FormSession
import com.libs.flex.ui.core.domain.model.LayoutDocument
import com.libs.flex.ui.core.domain.model.node.ButtonNode
import com.libs.flex.ui.core.domain.model.node.InputNode
import com.libs.flex.ui.core.domain.model.node.VerticalLayoutNode
import com.libs.flex.ui.core.domain.ports.ActionResult
import com.libs.flex.ui.core.domain.validation.RequiredRule
import com.libs.flex.ui.core.domain.value.ActionId
import com.libs.flex.ui.core.domain.value.ButtonVariant
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.FieldValue
import com.libs.flex.ui.core.domain.value.InputVariant
import com.libs.flex.ui.core.domain.value.NodeKey
import com.libs.flex.ui.core.domain.value.SchemaVersion
import com.libs.flex.ui.core.domain.value.SessionId
import com.libs.flex.ui.core.fakes.FakeClock
import com.libs.flex.ui.core.fakes.FakeEventPublisher
import com.libs.flex.ui.core.fakes.FakeFormSessionRepository
import com.libs.flex.ui.core.fakes.FakeHostActionHandler
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ProcessUserActionUseCaseTest {

    private fun componentId(value: String): ComponentId =
        ComponentId.of(value).fold({ throw IllegalStateException(it.toString()) }, { it })

    private fun actionId(value: String): ActionId =
        ActionId.of(value).fold({ throw IllegalStateException(it.toString()) }, { it })

    private fun sessionId(value: String): SessionId =
        SessionId.of(value).fold({ throw IllegalStateException(it.toString()) }, { it })

    private fun createFormDocument(
        inputId: ComponentId,
        buttonId: ComponentId,
        actionId: ActionId,
        requiresValidation: Boolean = true,
        buttonEnabled: Boolean = true
    ): LayoutDocument {
        val root = VerticalLayoutNode(
            id = componentId("root"),
            children = listOf(
                InputNode(
                    id = inputId,
                    label = "Email",
                    placeholder = "Enter email",
                    inputVariant = InputVariant.Outlined,
                    isSensitive = false,
                    validationRules = listOf(RequiredRule("Email is required"))
                ),
                ButtonNode(
                    id = buttonId,
                    text = "Submit",
                    actionId = actionId,
                    buttonVariant = ButtonVariant.Primary,
                    isEnabled = buttonEnabled,
                    requiresValidation = requiresValidation
                )
            )
        )
        return LayoutDocument.create(SchemaVersion("1.0.0"), root).fold(
            { throw IllegalStateException(it.toString()) },
            { it }
        )
    }

    @Test
    fun `when session does not exist, returns SessionNotFound error without invoking host`() = runTest {
        val sessionPort = FakeFormSessionRepository()
        val hostHandler = FakeHostActionHandler()
        val eventPublisher = FakeEventPublisher()
        val clock = FakeClock()

        val sid = sessionId("non-existent")
        val key = NodeKey(componentId("btn-submit"))

        val useCase = ProcessUserActionUseCase(sessionPort, hostHandler, eventPublisher, clock)
        val result = useCase.execute(sid, key)

        assertIs<Either.Left<DomainError>>(result)
        assertIs<DomainError.Interaction.SessionNotFound>(result.value)
        assertTrue(hostHandler.recordedActions.isEmpty())
        assertTrue(eventPublisher.events.isEmpty())
    }

    @Test
    fun `when button is disabled, returns ComponentDisabled error`() = runTest {
        val sessionPort = FakeFormSessionRepository()
        val hostHandler = FakeHostActionHandler()
        val eventPublisher = FakeEventPublisher()
        val clock = FakeClock()

        val sid = sessionId("session-1")
        val inputId = componentId("input-email")
        val buttonId = componentId("btn-submit")
        val aid = actionId("submit_action")
        val doc = createFormDocument(inputId, buttonId, aid, buttonEnabled = false)

        val outcome = FormSession.start(sid, doc, clock)
        sessionPort.save(outcome.session)
        eventPublisher.clear()

        val key = NodeKey(buttonId)
        val useCase = ProcessUserActionUseCase(sessionPort, hostHandler, eventPublisher, clock)
        val result = useCase.execute(sid, key)

        assertIs<Either.Left<DomainError>>(result)
        assertIs<DomainError.Interaction.ComponentDisabled>(result.value)
        assertTrue(hostHandler.recordedActions.isEmpty())
    }

    @Test
    fun `when form validation fails, blocks action and persists errors without calling host`() = runTest {
        val sessionPort = FakeFormSessionRepository()
        val hostHandler = FakeHostActionHandler()
        val eventPublisher = FakeEventPublisher()
        val clock = FakeClock()

        val sid = sessionId("session-1")
        val inputId = componentId("input-email")
        val buttonId = componentId("btn-submit")
        val aid = actionId("submit_action")
        val doc = createFormDocument(inputId, buttonId, aid, requiresValidation = true)

        val outcome = FormSession.start(sid, doc, clock)
        sessionPort.save(outcome.session)
        eventPublisher.clear()

        val key = NodeKey(buttonId)
        val useCase = ProcessUserActionUseCase(sessionPort, hostHandler, eventPublisher, clock)
        val result = useCase.execute(sid, key)

        val actionResult = assertIs<Either.Right<ActionResult>>(result).value
        assertIs<ActionResult.Blocked>(actionResult)
        assertTrue(actionResult.errors.containsKey(NodeKey(inputId)))

        // Host was never called
        assertTrue(hostHandler.recordedActions.isEmpty())

        // Saved session contains errors
        val savedSession = sessionPort.load(sid)
        assertTrue(savedSession != null)
        assertTrue(savedSession.formErrors.containsKey(NodeKey(inputId)))

        // Verify events: empty since blockedOutcome does not emit additional events
        assertTrue(eventPublisher.events.isEmpty())
    }

    @Test
    fun `when action is valid and host handles it, dispatches action and saves handled result`() = runTest {
        val sessionPort = FakeFormSessionRepository()
        val hostHandler = FakeHostActionHandler(defaultResult = ActionResult.Handled)
        val eventPublisher = FakeEventPublisher()
        val clock = FakeClock(1000L)

        val sid = sessionId("session-1")
        val inputId = componentId("input-email")
        val buttonId = componentId("btn-submit")
        val aid = actionId("submit_action")
        val doc = createFormDocument(inputId, buttonId, aid, requiresValidation = true)

        val outcome = FormSession.start(sid, doc, clock)
        // Fill input validly
        val filledOutcome = outcome.session.updateInput(NodeKey(inputId), FieldValue.Text("test@example.com"), 1100L)
            .fold({ throw IllegalStateException() }, { it })
        sessionPort.save(filledOutcome.session)
        eventPublisher.clear()

        val key = NodeKey(buttonId)
        clock.setTime(2000L)
        val useCase = ProcessUserActionUseCase(sessionPort, hostHandler, eventPublisher, clock)
        val result = useCase.execute(sid, key)

        val actionResult = assertIs<Either.Right<ActionResult>>(result).value
        assertEquals(ActionResult.Handled, actionResult)

        // Verify host was called with UserAction containing form values
        assertEquals(1, hostHandler.recordedActions.size)
        val dispatchedAction = hostHandler.recordedActions.first()
        assertEquals(aid, dispatchedAction.actionId)
        assertEquals(key, dispatchedAction.sourceKey)
        assertEquals(FieldValue.Text("test@example.com"), dispatchedAction.formValues[NodeKey(inputId)])

        // Verify events: UserActionDispatchedEvent was published
        assertTrue(eventPublisher.events.any { it is DomainEvent.UserActionDispatchedEvent })
    }

    @Test
    fun `when host rejects action, records rejection outcome and publishes UserActionRejectedEvent`() = runTest {
        val sessionPort = FakeFormSessionRepository()
        val hostHandler = FakeHostActionHandler(defaultResult = ActionResult.Rejected("Rate limited"))
        val eventPublisher = FakeEventPublisher()
        val clock = FakeClock(1000L)

        val sid = sessionId("session-1")
        val inputId = componentId("input-email")
        val buttonId = componentId("btn-submit")
        val aid = actionId("submit_action")
        val doc = createFormDocument(inputId, buttonId, aid, requiresValidation = false)

        val outcome = FormSession.start(sid, doc, clock)
        sessionPort.save(outcome.session)
        eventPublisher.clear()

        val key = NodeKey(buttonId)
        val useCase = ProcessUserActionUseCase(sessionPort, hostHandler, eventPublisher, clock)
        val result = useCase.execute(sid, key)

        val actionResult = assertIs<Either.Right<ActionResult>>(result).value
        assertIs<ActionResult.Rejected>(actionResult)
        assertEquals("Rate limited", actionResult.reason)

        // Events include dispatched and rejected
        assertTrue(eventPublisher.events.any { it is DomainEvent.UserActionDispatchedEvent })
        val rejectedEvent = eventPublisher.events.filterIsInstance<DomainEvent.UserActionRejectedEvent>().firstOrNull()
        assertTrue(rejectedEvent != null)
        assertEquals("Rate limited", rejectedEvent.reason)
        assertEquals(aid, rejectedEvent.actionId)
    }

    @Test
    fun `when host leaves action unhandled, publishes UserActionUnhandledEvent`() = runTest {
        val sessionPort = FakeFormSessionRepository()
        val hostHandler = FakeHostActionHandler(defaultResult = ActionResult.Unhandled)
        val eventPublisher = FakeEventPublisher()
        val clock = FakeClock(1000L)

        val sid = sessionId("session-1")
        val inputId = componentId("input-email")
        val buttonId = componentId("btn-submit")
        val aid = actionId("submit_action")
        val doc = createFormDocument(inputId, buttonId, aid, requiresValidation = false)

        val outcome = FormSession.start(sid, doc, clock)
        sessionPort.save(outcome.session)
        eventPublisher.clear()

        val key = NodeKey(buttonId)
        val useCase = ProcessUserActionUseCase(sessionPort, hostHandler, eventPublisher, clock)
        val result = useCase.execute(sid, key)

        val actionResult = assertIs<Either.Right<ActionResult>>(result).value
        assertEquals(ActionResult.Unhandled, actionResult)

        assertTrue(eventPublisher.events.any { it is DomainEvent.UserActionDispatchedEvent })
        val unhandledEvent = eventPublisher.events.filterIsInstance<DomainEvent.UserActionUnhandledEvent>().firstOrNull()
        assertTrue(unhandledEvent != null)
        assertEquals(aid, unhandledEvent.actionId)
    }
}

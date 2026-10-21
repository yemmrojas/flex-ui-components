package com.libs.flex.ui.core.application.handler

import com.libs.flex.ui.core.application.command.ProcessUserActionCommand
import com.libs.flex.ui.core.application.usecase.ProcessUserActionUseCase
import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.model.FormSession
import com.libs.flex.ui.core.domain.model.LayoutDocument
import com.libs.flex.ui.core.domain.model.node.ButtonNode
import com.libs.flex.ui.core.domain.model.node.VerticalLayoutNode
import com.libs.flex.ui.core.domain.ports.ActionResult
import com.libs.flex.ui.core.domain.value.ActionId
import com.libs.flex.ui.core.domain.value.ButtonVariant
import com.libs.flex.ui.core.domain.value.ComponentId
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

class ProcessUserActionHandlerTest {

    private fun sampleDocumentWithButton(btnId: ComponentId, actionId: ActionId): LayoutDocument {
        val root = VerticalLayoutNode(
            id = ComponentId.of("root").fold({ throw IllegalStateException() }, { it }),
            children = listOf(
                ButtonNode(
                    id = btnId,
                    text = "Submit",
                    actionId = actionId,
                    buttonVariant = ButtonVariant.Primary,
                    isEnabled = true,
                    requiresValidation = false
                )
            )
        )
        return LayoutDocument.create(SchemaVersion("1.0.0"), root).fold(
            { throw IllegalStateException() },
            { it }
        )
    }

    @Test
    fun `when sessionId is invalid, returns InvalidIdentifier without executing useCase`() = runTest {
        val sessionPort = FakeFormSessionRepository()
        val hostHandler = FakeHostActionHandler()
        val eventPublisher = FakeEventPublisher()
        val clock = FakeClock()
        val useCase = ProcessUserActionUseCase(sessionPort, hostHandler, eventPublisher, clock)
        val handler = ProcessUserActionHandler(useCase)

        val command = ProcessUserActionCommand(sessionId = "", componentId = "btn-1")
        val result = handler.handle(command)

        assertIs<Either.Left<DomainError>>(result)
        assertIs<DomainError.Value.InvalidIdentifier>(result.value)
    }

    @Test
    fun `when componentId is invalid, returns InvalidIdentifier without executing useCase`() = runTest {
        val sessionPort = FakeFormSessionRepository()
        val hostHandler = FakeHostActionHandler()
        val eventPublisher = FakeEventPublisher()
        val clock = FakeClock()
        val useCase = ProcessUserActionUseCase(sessionPort, hostHandler, eventPublisher, clock)
        val handler = ProcessUserActionHandler(useCase)

        val command = ProcessUserActionCommand(sessionId = "session-1", componentId = "")
        val result = handler.handle(command)

        assertIs<Either.Left<DomainError>>(result)
        assertIs<DomainError.Value.InvalidIdentifier>(result.value)
    }

    @Test
    fun `when command is valid, constructs Value Objects and delegates to useCase`() = runTest {
        val sessionPort = FakeFormSessionRepository()
        val hostHandler = FakeHostActionHandler(defaultResult = ActionResult.Handled)
        val eventPublisher = FakeEventPublisher()
        val clock = FakeClock(1000L)

        val sid = SessionId.of("session-1").fold({ throw IllegalStateException() }, { it })
        val btnId = ComponentId.of("btn-submit").fold({ throw IllegalStateException() }, { it })
        val aid = ActionId.of("action-save").fold({ throw IllegalStateException() }, { it })
        val doc = sampleDocumentWithButton(btnId, aid)
        val initialOutcome = FormSession.start(sid, doc, clock)
        sessionPort.save(initialOutcome.session)

        val useCase = ProcessUserActionUseCase(sessionPort, hostHandler, eventPublisher, clock)
        val handler = ProcessUserActionHandler(useCase)

        val command = ProcessUserActionCommand(sessionId = "session-1", componentId = "btn-submit", instanceIndex = 1)
        val result = handler.handle(command)

        val actionResult = assertIs<Either.Right<ActionResult>>(result).value
        assertEquals(ActionResult.Handled, actionResult)
        assertEquals(1, hostHandler.recordedActions.size)
        val recordedAction = hostHandler.recordedActions.first()
        assertEquals(aid, recordedAction.actionId)
        assertEquals(NodeKey(btnId, 1), recordedAction.sourceKey)
    }
}

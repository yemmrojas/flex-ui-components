package com.libs.flex.ui.core.application.handler

import com.libs.flex.ui.core.application.command.StartFormSessionCommand
import com.libs.flex.ui.core.application.usecase.StartFormSessionUseCase
import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.model.FormSession
import com.libs.flex.ui.core.domain.model.LayoutDocument
import com.libs.flex.ui.core.domain.model.node.TextNode
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.LayoutId
import com.libs.flex.ui.core.domain.value.SchemaVersion
import com.libs.flex.ui.core.domain.value.SessionId
import com.libs.flex.ui.core.fakes.FakeClock
import com.libs.flex.ui.core.fakes.FakeEventPublisher
import com.libs.flex.ui.core.fakes.FakeFormSessionRepository
import com.libs.flex.ui.core.fakes.FakeLayoutSource
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class StartFormSessionHandlerTest {

    private fun sampleDocument(): LayoutDocument {
        val root = TextNode(
            id = ComponentId.of("text-1").fold({ throw IllegalStateException() }, { it }),
            text = "Welcome"
        )
        return LayoutDocument.create(SchemaVersion("1.0.0"), root).fold(
            { throw IllegalStateException() },
            { it }
        )
    }

    @Test
    fun `when sessionId is invalid, returns InvalidIdentifier error without executing useCase`() = runTest {
        val layoutSource = FakeLayoutSource()
        val sessionPort = FakeFormSessionRepository()
        val eventPublisher = FakeEventPublisher()
        val clock = FakeClock()
        val useCase = StartFormSessionUseCase(layoutSource, sessionPort, eventPublisher, clock)

        val handler = StartFormSessionHandler(useCase)
        val command = StartFormSessionCommand(sessionId = "", layoutId = "layout-home")

        val result = handler.handle(command)

        assertIs<Either.Left<DomainError>>(result)
        assertIs<DomainError.Value.InvalidIdentifier>(result.value)
    }

    @Test
    fun `when layoutId is invalid, returns InvalidIdentifier error without executing useCase`() = runTest {
        val layoutSource = FakeLayoutSource()
        val sessionPort = FakeFormSessionRepository()
        val eventPublisher = FakeEventPublisher()
        val clock = FakeClock()
        val useCase = StartFormSessionUseCase(layoutSource, sessionPort, eventPublisher, clock)

        val handler = StartFormSessionHandler(useCase)
        val command = StartFormSessionCommand(sessionId = "valid-session", layoutId = "")

        val result = handler.handle(command)

        assertIs<Either.Left<DomainError>>(result)
        assertIs<DomainError.Value.InvalidIdentifier>(result.value)
    }

    @Test
    fun `when command has valid parameters, delegates to useCase successfully`() = runTest {
        val layoutSource = FakeLayoutSource()
        val sessionPort = FakeFormSessionRepository()
        val eventPublisher = FakeEventPublisher()
        val clock = FakeClock()

        val layoutId = LayoutId.of("layout-home").fold({ throw IllegalStateException() }, { it })
        layoutSource.register(layoutId, sampleDocument())

        val useCase = StartFormSessionUseCase(layoutSource, sessionPort, eventPublisher, clock)
        val handler = StartFormSessionHandler(useCase)
        val command = StartFormSessionCommand(sessionId = "session-123", layoutId = "layout-home")

        val result = handler.handle(command)

        val session = assertIs<Either.Right<FormSession>>(result).value
        val expectedSessionId = SessionId.of("session-123").fold({ throw IllegalStateException() }, { it })
        assertEquals(expectedSessionId, session.sessionId)
        assertEquals(session, sessionPort.load(expectedSessionId))
    }
}

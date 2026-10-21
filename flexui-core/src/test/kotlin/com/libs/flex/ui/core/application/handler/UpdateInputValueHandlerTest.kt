package com.libs.flex.ui.core.application.handler

import com.libs.flex.ui.core.application.command.UpdateInputValueCommand
import com.libs.flex.ui.core.application.usecase.UpdateInputValueUseCase
import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.model.FormSession
import com.libs.flex.ui.core.domain.model.LayoutDocument
import com.libs.flex.ui.core.domain.model.node.InputNode
import com.libs.flex.ui.core.domain.model.node.VerticalLayoutNode
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.FieldValue
import com.libs.flex.ui.core.domain.value.InputVariant
import com.libs.flex.ui.core.domain.value.NodeKey
import com.libs.flex.ui.core.domain.value.SchemaVersion
import com.libs.flex.ui.core.domain.value.SessionId
import com.libs.flex.ui.core.fakes.FakeClock
import com.libs.flex.ui.core.fakes.FakeEventPublisher
import com.libs.flex.ui.core.fakes.FakeFormSessionRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class UpdateInputValueHandlerTest {

    private fun sampleDocumentWithInput(inputId: ComponentId): LayoutDocument {
        val root = VerticalLayoutNode(
            id = ComponentId.of("root").fold({ throw IllegalStateException() }, { it }),
            children = listOf(
                InputNode(
                    id = inputId,
                    label = "Full Name",
                    placeholder = "Enter name",
                    inputVariant = InputVariant.Outlined,
                    isSensitive = false,
                    validationRules = emptyList()
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
        val eventPublisher = FakeEventPublisher()
        val clock = FakeClock()
        val useCase = UpdateInputValueUseCase(sessionPort, eventPublisher, clock)
        val handler = UpdateInputValueHandler(useCase)

        val command = UpdateInputValueCommand(sessionId = "", componentId = "input-1", rawValue = "John")
        val result = handler.handle(command)

        assertIs<Either.Left<DomainError>>(result)
        assertIs<DomainError.Value.InvalidIdentifier>(result.value)
    }

    @Test
    fun `when componentId is invalid, returns InvalidIdentifier without executing useCase`() = runTest {
        val sessionPort = FakeFormSessionRepository()
        val eventPublisher = FakeEventPublisher()
        val clock = FakeClock()
        val useCase = UpdateInputValueUseCase(sessionPort, eventPublisher, clock)
        val handler = UpdateInputValueHandler(useCase)

        val command = UpdateInputValueCommand(sessionId = "session-1", componentId = "", rawValue = "John")
        val result = handler.handle(command)

        assertIs<Either.Left<DomainError>>(result)
        assertIs<DomainError.Value.InvalidIdentifier>(result.value)
    }

    @Test
    fun `when command is valid, constructs Value Objects and delegates to useCase`() = runTest {
        val sessionPort = FakeFormSessionRepository()
        val eventPublisher = FakeEventPublisher()
        val clock = FakeClock(1000L)

        val sid = SessionId.of("session-1").fold({ throw IllegalStateException() }, { it })
        val inputId = ComponentId.of("input-name").fold({ throw IllegalStateException() }, { it })
        val doc = sampleDocumentWithInput(inputId)
        val initialOutcome = FormSession.start(sid, doc, clock)
        sessionPort.save(initialOutcome.session)

        val useCase = UpdateInputValueUseCase(sessionPort, eventPublisher, clock)
        val handler = UpdateInputValueHandler(useCase)

        val command = UpdateInputValueCommand(
            sessionId = "session-1",
            componentId = "input-name",
            rawValue = "Alice Cooper"
        )
        val result = handler.handle(command)

        val updatedSession = assertIs<Either.Right<FormSession>>(result).value
        val expectedKey = NodeKey(inputId, null)
        assertEquals(FieldValue.Text("Alice Cooper"), updatedSession.formValues[expectedKey])
    }
}

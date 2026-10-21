package com.libs.flex.ui.core.application.usecase

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.event.DomainEvent
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.model.FormSession
import com.libs.flex.ui.core.domain.model.LayoutDocument
import com.libs.flex.ui.core.domain.model.node.InputNode
import com.libs.flex.ui.core.domain.model.node.VerticalLayoutNode
import com.libs.flex.ui.core.domain.validation.RequiredRule
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
import kotlin.test.assertTrue

class UpdateInputValueUseCaseTest {

    private fun componentId(value: String): ComponentId =
        ComponentId.of(value).fold({ throw IllegalStateException(it.toString()) }, { it })

    private fun sessionId(value: String): SessionId =
        SessionId.of(value).fold({ throw IllegalStateException(it.toString()) }, { it })

    private fun createDocumentWithInput(inputId: ComponentId): LayoutDocument {
        val root = VerticalLayoutNode(
            id = componentId("root"),
            children = listOf(
                InputNode(
                    id = inputId,
                    label = "Username",
                    placeholder = "Enter username",
                    inputVariant = InputVariant.Outlined,
                    isSensitive = false,
                    validationRules = listOf(RequiredRule("Username is required"))
                )
            )
        )
        return LayoutDocument.create(SchemaVersion("1.0.0"), root).fold(
            { throw IllegalStateException(it.toString()) },
            { it }
        )
    }

    @Test
    fun `when session does not exist, returns SessionNotFound error without publishing events`() = runTest {
        val sessionPort = FakeFormSessionRepository()
        val eventPublisher = FakeEventPublisher()
        val clock = FakeClock()

        val sid = sessionId("non-existent-session")
        val key = NodeKey(componentId("input-user"))

        val useCase = UpdateInputValueUseCase(sessionPort, eventPublisher, clock)
        val result = useCase.execute(sid, key, FieldValue.Text("John"))

        assertIs<Either.Left<DomainError>>(result)
        val error = result.value
        assertIs<DomainError.Interaction.SessionNotFound>(error)
        assertEquals(sid, error.id)
        assertTrue(eventPublisher.events.isEmpty())
    }

    @Test
    fun `when component does not exist in document, returns ComponentNotFound error`() = runTest {
        val sessionPort = FakeFormSessionRepository()
        val eventPublisher = FakeEventPublisher()
        val clock = FakeClock()

        val sid = sessionId("session-1")
        val inputId = componentId("real-input")
        val doc = createDocumentWithInput(inputId)
        val outcome = FormSession.start(sid, doc, clock)
        sessionPort.save(outcome.session)
        eventPublisher.clear()

        val unknownKey = NodeKey(componentId("unknown-input"))
        val useCase = UpdateInputValueUseCase(sessionPort, eventPublisher, clock)
        val result = useCase.execute(sid, unknownKey, FieldValue.Text("value"))

        assertIs<Either.Left<DomainError>>(result)
        assertIs<DomainError.Interaction.ComponentNotFound>(result.value)
        assertTrue(eventPublisher.events.isEmpty())
    }

    @Test
    fun `when updating value validly, saves session and publishes InputValueChangedEvent`() = runTest {
        val sessionPort = FakeFormSessionRepository()
        val eventPublisher = FakeEventPublisher()
        val clock = FakeClock(2000L)

        val sid = sessionId("session-1")
        val inputId = componentId("username-input")
        val doc = createDocumentWithInput(inputId)
        val outcome = FormSession.start(sid, doc, clock)
        sessionPort.save(outcome.session)
        eventPublisher.clear()

        val key = NodeKey(inputId)
        val newValue = FieldValue.Text("Alice")

        clock.setTime(2500L)
        val useCase = UpdateInputValueUseCase(sessionPort, eventPublisher, clock)
        val result = useCase.execute(sid, key, newValue)

        val updatedSession = assertIs<Either.Right<FormSession>>(result).value
        assertEquals(newValue, updatedSession.formValues[key])

        // Verify session saved
        assertEquals(updatedSession, sessionPort.load(sid))

        // Verify event published
        assertEquals(1, eventPublisher.events.size)
        val event = eventPublisher.events.first()
        assertIs<DomainEvent.InputValueChangedEvent>(event)
        assertEquals(key, event.key)
        assertEquals(newValue, event.value)
        assertEquals(2500L, event.timestamp)
    }

    @Test
    fun `when updating value with validation error, saves error and publishes validation failed event`() = runTest {
        val sessionPort = FakeFormSessionRepository()
        val eventPublisher = FakeEventPublisher()
        val clock = FakeClock(3000L)

        val sid = sessionId("session-1")
        val inputId = componentId("username-input")
        val doc = createDocumentWithInput(inputId)
        val outcome = FormSession.start(sid, doc, clock)
        sessionPort.save(outcome.session)
        eventPublisher.clear()

        val key = NodeKey(inputId)
        val emptyValue = FieldValue.Text("")

        val useCase = UpdateInputValueUseCase(sessionPort, eventPublisher, clock)
        val result = useCase.execute(sid, key, emptyValue)

        val updatedSession = assertIs<Either.Right<FormSession>>(result).value
        assertTrue(!updatedSession.isValid)
        assertTrue(updatedSession.formErrors.containsKey(key))

        // Verify published events: InputValueChangedEvent and InputValidationFailedEvent
        assertEquals(2, eventPublisher.events.size)
        assertIs<DomainEvent.InputValueChangedEvent>(eventPublisher.events[0])
        val failEvent = eventPublisher.events[1]
        assertIs<DomainEvent.InputValidationFailedEvent>(failEvent)
        assertEquals(key, failEvent.key)
        assertEquals("Username is required", failEvent.message)
    }
}

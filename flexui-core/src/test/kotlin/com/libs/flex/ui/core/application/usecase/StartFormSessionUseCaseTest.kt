package com.libs.flex.ui.core.application.usecase

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.event.DomainEvent
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.model.FormSession
import com.libs.flex.ui.core.domain.model.LayoutDocument
import com.libs.flex.ui.core.domain.model.node.CheckNode
import com.libs.flex.ui.core.domain.model.node.VerticalLayoutNode
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.FieldValue
import com.libs.flex.ui.core.domain.value.LayoutId
import com.libs.flex.ui.core.domain.value.NodeKey
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
import kotlin.test.assertTrue

class StartFormSessionUseCaseTest {

    private fun componentId(value: String): ComponentId =
        ComponentId.of(value).fold({ throw IllegalStateException(it.toString()) }, { it })

    private fun sessionId(value: String): SessionId =
        SessionId.of(value).fold({ throw IllegalStateException(it.toString()) }, { it })

    private fun layoutId(value: String): LayoutId =
        LayoutId.of(value).fold({ throw IllegalStateException(it.toString()) }, { it })

    private fun createDocumentWithDefaultCheck(checkId: ComponentId): LayoutDocument {
        val root = VerticalLayoutNode(
            id = componentId("root"),
            children = listOf(
                CheckNode(
                    id = checkId,
                    label = "Accept terms",
                    defaultChecked = true
                )
            )
        )
        return LayoutDocument.create(SchemaVersion("1.0.0"), root).fold(
            { throw IllegalStateException(it.toString()) },
            { it }
        )
    }

    @Test
    fun `when layout source returns error, use case returns Left and does not save session or publish events`() = runTest {
        val layoutSource = FakeLayoutSource()
        val sessionPort = FakeFormSessionRepository()
        val eventPublisher = FakeEventPublisher()
        val clock = FakeClock()

        val lid = layoutId("layout-invalid")
        val sid = sessionId("sess-1")
        val structuralError = DomainError.Structural.UnsupportedSchemaVersion("9.9.9", "1.0.0", "1.0.0")
        layoutSource.registerError(lid, Nel(structuralError))

        val useCase = StartFormSessionUseCase(layoutSource, sessionPort, eventPublisher, clock)
        val result = useCase.execute(sid, lid)

        assertIs<Either.Left<DomainError>>(result)
        assertEquals(structuralError, result.value)
        assertTrue(sessionPort.allSessions().isEmpty())
        assertTrue(eventPublisher.events.isEmpty())
    }

    @Test
    fun `when layout is found, starts session, seeds defaults, saves session and publishes started event`() = runTest {
        val layoutSource = FakeLayoutSource()
        val sessionPort = FakeFormSessionRepository()
        val eventPublisher = FakeEventPublisher()
        val clock = FakeClock(5000L)

        val lid = layoutId("layout-terms")
        val sid = sessionId("sess-terms")
        val checkId = componentId("terms-check")
        val doc = createDocumentWithDefaultCheck(checkId)
        layoutSource.register(lid, doc)

        val useCase = StartFormSessionUseCase(layoutSource, sessionPort, eventPublisher, clock)
        val result = useCase.execute(sid, lid)

        val session = assertIs<Either.Right<FormSession>>(result).value
        assertEquals(sid, session.sessionId)
        assertEquals(FieldValue.Bool(true), session.formValues[NodeKey(checkId)])

        // Verify session was persisted
        assertEquals(session, sessionPort.load(sid))

        // Verify event published
        assertEquals(1, eventPublisher.events.size)
        val event = eventPublisher.events.first()
        assertIs<DomainEvent.FormSessionStartedEvent>(event)
        assertEquals(sid, event.sessionId)
        assertEquals(SchemaVersion("1.0.0"), event.layoutVersion)
        assertEquals(5000L, event.timestamp)
    }
}

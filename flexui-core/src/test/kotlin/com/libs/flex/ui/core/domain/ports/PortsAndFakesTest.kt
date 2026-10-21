package com.libs.flex.ui.core.domain.ports

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.event.DomainEvent
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.model.FormSession
import com.libs.flex.ui.core.domain.model.LayoutDocument
import com.libs.flex.ui.core.domain.model.node.TextNode
import com.libs.flex.ui.core.domain.value.ActionId
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.FieldValue
import com.libs.flex.ui.core.domain.value.LayoutId
import com.libs.flex.ui.core.domain.value.NodeKey
import com.libs.flex.ui.core.domain.value.SchemaVersion
import com.libs.flex.ui.core.domain.value.SessionId
import com.libs.flex.ui.core.fakes.FakeClock
import com.libs.flex.ui.core.fakes.FakeEventPublisher
import com.libs.flex.ui.core.fakes.FakeFormSessionRepository
import com.libs.flex.ui.core.fakes.FakeHostActionHandler
import com.libs.flex.ui.core.fakes.FakeLayoutSource
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PortsAndFakesTest {

    private fun sampleDocument(): LayoutDocument {
        val root = TextNode(
            id = ComponentId.of("text-1").fold({ throw IllegalStateException(it.toString()) }, { it }),
            text = "Hello World"
        )
        return LayoutDocument.create(SchemaVersion("1.0.0"), root).fold(
            { throw IllegalStateException(it.toString()) },
            { it }
        )
    }

    private fun sampleSession(clock: FakeClock): FormSession {
        val sessionId = SessionId.of("session-1").fold({ throw IllegalStateException(it.toString()) }, { it })
        return FormSession.start(sessionId, sampleDocument(), clock).session
    }

    private fun sampleEvent(sessionId: SessionId): DomainEvent {
        return DomainEvent.FormSessionStartedEvent(
            sessionId = sessionId,
            layoutVersion = SchemaVersion("1.0.0"),
            timestamp = 1000L
        )
    }

    @Test
    fun `publishSafely publishes event to event publisher port`() = runTest {
        val publisher = FakeEventPublisher()
        val sessionId = SessionId.of("sess-1").fold({ throw IllegalStateException() }, { it })
        val event = sampleEvent(sessionId)

        publisher.publishSafely(event)

        assertEquals(listOf(event), publisher.events)
    }

    @Test
    fun `publishSafely rethrows CancellationException`() = runTest {
        val publisher = FakeEventPublisher()
        publisher.exceptionToThrow = CancellationException("Coroutines Cancelled")
        val sessionId = SessionId.of("sess-1").fold({ throw IllegalStateException() }, { it })
        val event = sampleEvent(sessionId)

        assertFailsWith<CancellationException> {
            publisher.publishSafely(event)
        }
    }

    @Test
    fun `publishSafely swallows non-critical exceptions`() = runTest {
        val publisher = FakeEventPublisher()
        publisher.exceptionToThrow = RuntimeException("Telemetry failure")
        val sessionId = SessionId.of("sess-1").fold({ throw IllegalStateException() }, { it })
        val event = sampleEvent(sessionId)

        // Should not throw
        publisher.publishSafely(event)
        assertTrue(publisher.events.isEmpty())
    }

    @Test
    fun `publishAllSafely publishes multiple events in order`() = runTest {
        val publisher = FakeEventPublisher()
        val sid1 = SessionId.of("sess-1").fold({ throw IllegalStateException() }, { it })
        val sid2 = SessionId.of("sess-2").fold({ throw IllegalStateException() }, { it })
        val event1 = sampleEvent(sid1)
        val event2 = sampleEvent(sid2)

        publisher.publishAllSafely(listOf(event1, event2))

        assertEquals(listOf(event1, event2), publisher.events)
    }

    @Test
    fun `publishAllSafely rethrows CancellationException`() = runTest {
        val publisher = FakeEventPublisher()
        publisher.exceptionToThrow = CancellationException("Job was cancelled")
        val sid = SessionId.of("sess-1").fold({ throw IllegalStateException() }, { it })
        val events = listOf(sampleEvent(sid))

        assertFailsWith<CancellationException> {
            publisher.publishAllSafely(events)
        }
    }

    @Test
    fun `FakeClock advances time and sets time correctly`() {
        val clock = FakeClock(1000L)
        assertEquals(1000L, clock.currentTimeMillis())

        clock.advanceBy(500L)
        assertEquals(1500L, clock.currentTimeMillis())

        clock.setTime(3000L)
        assertEquals(3000L, clock.currentTimeMillis())
    }

    @Test
    fun `FakeFormSessionRepository stores and loads sessions`() = runTest {
        val repo = FakeFormSessionRepository()
        val clock = FakeClock()
        val session = sampleSession(clock)

        assertNull(repo.load(session.sessionId))
        assertTrue(!repo.contains(session.sessionId))

        repo.save(session)

        assertEquals(session, repo.load(session.sessionId))
        assertTrue(repo.contains(session.sessionId))
        assertEquals(listOf(session), repo.allSessions())

        repo.clear()
        assertNull(repo.load(session.sessionId))
    }

    @Test
    fun `FakeLayoutSource returns registered document or registered error or default error`() = runTest {
        val source = FakeLayoutSource()
        val layoutId = LayoutId.of("layout-profile").fold({ throw IllegalStateException() }, { it })
        val doc = sampleDocument()

        val notFoundRes = source.getLayout(LayoutRequest(layoutId))
        assertIs<Either.Left<*>>(notFoundRes)

        source.register(layoutId, doc)
        val foundRes = source.getLayout(LayoutRequest(layoutId))
        assertIs<Either.Right<LayoutDocument>>(foundRes)
        assertEquals(doc, foundRes.value)

        val errorLayoutId = LayoutId.of("layout-error").fold({ throw IllegalStateException() }, { it })
        val customError = Nel(DomainError.Structural.UnsupportedSchemaVersion("2.0.0", "1.0.0", "1.0.0"))
        source.registerError(errorLayoutId, customError)

        val customErrRes = source.getLayout(LayoutRequest(errorLayoutId))
        assertIs<Either.Left<Nel<DomainError.Structural>>>(customErrRes)
        assertEquals(customError, customErrRes.value)
    }

    @Test
    fun `FakeHostActionHandler records actions and handles action responses`() = runTest {
        val handler = FakeHostActionHandler(defaultResult = ActionResult.Handled)
        val actionId = ActionId.of("submit_form").fold({ throw IllegalStateException() }, { it })
        val key = NodeKey(ComponentId.of("btn-submit").fold({ throw IllegalStateException() }, { it }))
        val action = UserAction(
            actionId = actionId,
            sourceKey = key,
            formValues = mapOf(key to FieldValue.Text("John"))
        )

        val result1 = handler.handleAction(action)
        assertEquals(ActionResult.Handled, result1)
        assertEquals(listOf(action), handler.recordedActions)

        val customActionId = ActionId.of("cancel_form").fold({ throw IllegalStateException() }, { it })
        handler.whenAction(customActionId, ActionResult.Rejected("Host cancelled"))

        val action2 = UserAction(actionId = customActionId, sourceKey = key, formValues = emptyMap())
        val result2 = handler.handleAction(action2)
        assertEquals(ActionResult.Rejected("Host cancelled"), result2)
        assertEquals(listOf(action, action2), handler.recordedActions)
    }
}

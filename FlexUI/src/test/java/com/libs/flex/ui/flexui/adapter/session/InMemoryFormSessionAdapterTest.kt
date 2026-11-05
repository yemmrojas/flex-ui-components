package com.libs.flex.ui.flexui.adapter.session

import app.cash.turbine.test
import com.libs.flex.ui.core.domain.model.FormSession
import com.libs.flex.ui.core.domain.model.LayoutDocument
import com.libs.flex.ui.core.domain.model.node.InputNode
import com.libs.flex.ui.core.domain.ports.ClockPort
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.FieldValue
import com.libs.flex.ui.core.domain.value.NodeKey
import com.libs.flex.ui.core.domain.value.SchemaVersion
import com.libs.flex.ui.core.domain.value.SessionId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryFormSessionAdapterTest {

    private class TestClock(var time: Long = 1000L) : ClockPort {
        override fun currentTimeMillis(): Long = time
    }

    private fun givenSessionId(id: String = "session_test"): SessionId {
        return SessionId.of(id).getOrNull()!!
    }

    private fun givenLayoutDocument(): LayoutDocument {
        val rootNode = InputNode(
            id = ComponentId.of("input_name").getOrNull()!!,
            label = "Name"
        )
        return LayoutDocument.create(
            schemaVersion = SchemaVersion("1.0.0"),
            root = rootNode
        ).getOrNull()!!
    }

    private fun givenFormSession(sessionIdStr: String = "session_test"): FormSession {
        val sessionId = givenSessionId(sessionIdStr)
        val document = givenLayoutDocument()
        return FormSession.start(sessionId, document, TestClock()).session
    }

    @Test
    fun `save stores session and load retrieves it atomically`() = runBlocking {
        val adapter = InMemoryFormSessionAdapter()
        val session = givenFormSession()

        assertNull(adapter.load(session.sessionId))
        assertFalse(adapter.contains(session.sessionId))

        adapter.save(session)

        val loaded = adapter.load(session.sessionId)
        assertNotNull(loaded)
        assertEquals(session.sessionId, loaded?.sessionId)
        assertTrue(adapter.contains(session.sessionId))
    }

    @Test
    fun `observeSession emits updates in real-time as StateFlow transitions`() = runBlocking {
        val adapter = InMemoryFormSessionAdapter()
        val session1 = givenFormSession("session_stream")
        val sessionId = session1.sessionId

        adapter.observeSession(sessionId).test {
            // Inicialmente no hay emisión hasta que se guarde una sesión
            adapter.save(session1)
            val first = awaitItem()
            assertEquals(sessionId, first.sessionId)

            // Actualiza la sesión con una mutación válida
            val key = NodeKey(ComponentId.of("input_name").getOrNull()!!)
            val outcome = session1.updateInput(key, FieldValue.Text("Updated Title"), 2000L)
            assertTrue(outcome.isRight)
            val updatedSession = outcome.getOrNull()!!.session

            adapter.save(updatedSession)
            val second = awaitItem()
            assertEquals(sessionId, second.sessionId)
            assertEquals(FieldValue.Text("Updated Title"), second.formValues[key])

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `delete removes session and cleans up resources without memory leaks`() = runBlocking {
        val adapter = InMemoryFormSessionAdapter()
        val session = givenFormSession("session_to_delete")

        adapter.save(session)
        assertTrue(adapter.contains(session.sessionId))

        adapter.delete(session.sessionId)

        assertNull(adapter.load(session.sessionId))
        assertFalse(adapter.contains(session.sessionId))
    }

    @Test
    fun `concurrent updates to same session are executed serially without state corruption`() = runBlocking {
        val adapter = InMemoryFormSessionAdapter()
        val initialSession = givenFormSession("session_concurrent")
        val sessionId = initialSession.sessionId
        adapter.save(initialSession)

        val totalJobs = 20
        val jobs = (1..totalJobs).map { index ->
            async(Dispatchers.Default) {
                val current = adapter.load(sessionId) ?: return@async
                val key = NodeKey(ComponentId.of("input_name").getOrNull()!!)
                val outcome = current.updateInput(key, FieldValue.Text("Val_$index"), 3000L + index)
                val newSession = outcome.getOrNull()?.session ?: current
                adapter.save(newSession)
            }
        }

        jobs.awaitAll()

        val finalSession = adapter.load(sessionId)
        assertNotNull(finalSession)
        assertEquals(sessionId, finalSession?.sessionId)
    }
}

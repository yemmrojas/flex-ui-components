package com.libs.flex.ui.flexui.adapter

import com.libs.flex.ui.core.domain.event.DomainEvent
import com.libs.flex.ui.core.domain.ports.ActionResult
import com.libs.flex.ui.core.domain.ports.LayoutRequest
import com.libs.flex.ui.core.domain.ports.UserAction
import com.libs.flex.ui.core.domain.value.ActionId
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.FieldValue
import com.libs.flex.ui.core.domain.value.LayoutId
import com.libs.flex.ui.core.domain.value.NodeKey
import com.libs.flex.ui.core.domain.value.SchemaVersion
import com.libs.flex.ui.flexui.adapter.clock.SystemClockAdapter
import com.libs.flex.ui.flexui.adapter.host.SuspendHostActionAdapter
import com.libs.flex.ui.flexui.adapter.publisher.NoOpPublisherAdapter
import com.libs.flex.ui.flexui.adapter.source.DirectStringLayoutSourceAdapter
import com.libs.flex.ui.parser.LayoutParserFactory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptersIntegrationTest {

    @Test
    fun `SystemClockAdapter returns current system timestamp`() {
        val clock = SystemClockAdapter()
        val before = System.currentTimeMillis()
        val current = clock.currentTimeMillis()
        val after = System.currentTimeMillis()

        assertTrue(current >= before)
        assertTrue(current <= after)
    }

    @Test
    fun `NoOpPublisherAdapter publishes event safely without errors`() = runBlocking {
        val publisher = NoOpPublisherAdapter()
        val event = DomainEvent.FormSessionStartedEvent(
            sessionId = com.libs.flex.ui.core.domain.value.SessionId.of("s1").getOrNull()!!,
            layoutVersion = SchemaVersion("1.0.0"),
            timestamp = 1000L
        )

        // No-Op should complete normally without throwing
        publisher.publish(event)
    }

    @Test
    fun `SuspendHostActionAdapter delegates action to host lambda`() = runBlocking {
        var receivedAction: UserAction? = null
        val adapter = SuspendHostActionAdapter { action ->
            receivedAction = action
            ActionResult.Handled
        }

        val action = UserAction(
            actionId = ActionId.of("save").getOrNull()!!,
            sourceKey = NodeKey(ComponentId.of("btn_save").getOrNull()!!),
            formValues = mapOf(
                NodeKey(ComponentId.of("email").getOrNull()!!) to FieldValue.Text("test@example.com")
            )
        )

        val result = adapter.handleAction(action)

        assertEquals(ActionResult.Handled, result)
        assertEquals(action, receivedAction)
    }

    @Test
    fun `DirectStringLayoutSourceAdapter parses valid json layout`() = runBlocking {
        val validJson = """
            {
              "schemaVersion": "1.0.0",
              "layoutId": "login_form",
              "root": {
                "id": "root_text",
                "type": "flexText",
                "text": "Welcome to FlexUI"
              }
            }
        """.trimIndent()

        val parser = LayoutParserFactory.create()
        val adapter = DirectStringLayoutSourceAdapter(validJson, parser)

        val outcome = adapter.getLayout(LayoutRequest(LayoutId.of("login_form").getOrNull()!!))

        assertTrue(outcome.isRight)
        val document = outcome.getOrNull()!!
        assertEquals("1.0.0", document.schemaVersion.value)
        assertNotNull(document.findNode(ComponentId.of("root_text").getOrNull()!!))
    }

    @Test
    fun `DirectStringLayoutSourceAdapter maps invalid json to structural error`() = runBlocking {
        val brokenJson = "{ invalid json }"
        val parser = LayoutParserFactory.create()
        val adapter = DirectStringLayoutSourceAdapter(brokenJson, parser)

        val outcome = adapter.getLayout(LayoutRequest(LayoutId.of("login_form").getOrNull()!!))

        assertTrue(outcome.isLeft)
    }
}

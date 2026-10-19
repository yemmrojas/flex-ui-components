package com.libs.flex.ui.core.domain.event

import com.libs.flex.ui.core.domain.value.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

public class DomainEventTest {

    @Test
    public fun `FormSessionStartedEvent records session, schema and timestamp`() {
        val sessionId = SessionId.of("sess_100").getOrNull()!!
        val event = DomainEvent.FormSessionStartedEvent(
            sessionId = sessionId,
            layoutVersion = SchemaVersion.CURRENT,
            timestamp = 1700000000L
        )

        assertEquals(sessionId, event.sessionId)
        assertEquals(SchemaVersion.CURRENT, event.layoutVersion)
        assertEquals(1700000000L, event.timestamp)
    }

    @Test
    public fun `InputValueChangedEvent stores key, value and timestamp`() {
        val key = NodeKey(ComponentId.of("txt_1").getOrNull()!!)
        val event = DomainEvent.InputValueChangedEvent(
            key = key,
            value = FieldValue.Text("Hello"),
            timestamp = 1700000001L
        )

        assertEquals(key, event.key)
        assertEquals(FieldValue.Text("Hello"), event.value)
        assertEquals(1700000001L, event.timestamp)
    }

    @Test
    public fun `Validation failure and recovery events record exact transition keys`() {
        val key = NodeKey(ComponentId.of("txt_1").getOrNull()!!)
        val failed = DomainEvent.InputValidationFailedEvent(key, "Field is required", 1700000002L)
        val recovered = DomainEvent.InputValidationRecoveredEvent(key, 1700000003L)

        assertEquals("Field is required", failed.message)
        assertEquals(1700000002L, failed.timestamp)
        assertEquals(1700000003L, recovered.timestamp)
    }

    @Test
    public fun `UserAction events record action lifecycle`() {
        val key = NodeKey(ComponentId.of("btn_1").getOrNull()!!)
        val actionId = ActionId.of("submit").getOrNull()!!

        val dispatched = DomainEvent.UserActionDispatchedEvent(actionId, key, 1000L)
        val rejected = DomainEvent.UserActionRejectedEvent(actionId, key, "Network down", 1001L)
        val unhandled = DomainEvent.UserActionUnhandledEvent(actionId, key, 1002L)

        assertEquals(actionId, dispatched.actionId)
        assertEquals("Network down", rejected.reason)
        assertEquals(1002L, unhandled.timestamp)
    }
}

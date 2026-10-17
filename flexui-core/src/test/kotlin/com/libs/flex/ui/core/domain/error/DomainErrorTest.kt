package com.libs.flex.ui.core.domain.error

import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.SessionId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

public class DomainErrorTest {

    @Test
    public fun `Structural errors retain properties`() {
        val compId = ComponentId.of("btn_1").getOrNull()!!
        val error = DomainError.Structural.DuplicateComponentId(compId)
        assertEquals(compId, error.id)
    }

    @Test
    public fun `Value errors model validation failures accurately`() {
        val error = DomainError.Value.InvalidDimension("Negative value")
        assertEquals("Negative value", error.reason)
    }

    @Test
    public fun `Interaction errors model runtime session and state failures`() {
        val sessionId = SessionId.of("session_123").getOrNull()!!
        val error = DomainError.Interaction.SessionNotFound(sessionId)
        assertEquals(sessionId, error.id)

        val validationFail = DomainError.Interaction.FormValidationFailed(3)
        assertEquals(3, validationFail.errorsCount)
    }
}

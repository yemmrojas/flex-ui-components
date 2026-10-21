package com.libs.flex.ui.core.domain.model

import com.libs.flex.ui.core.domain.event.DomainEvent
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.model.node.InputNode
import com.libs.flex.ui.core.domain.model.node.VerticalLayoutNode
import com.libs.flex.ui.core.domain.validation.MinLengthRule
import com.libs.flex.ui.core.domain.validation.RequiredRule
import com.libs.flex.ui.core.domain.validation.ValidationError
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.FieldValue
import com.libs.flex.ui.core.domain.value.InputVariant
import com.libs.flex.ui.core.domain.value.NodeKey
import com.libs.flex.ui.core.domain.value.SchemaVersion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class FormInteractionStateTest {

    private val inputId = ComponentId.of("input_user").fold({ error("invalid id") }, { it })
    private val passwordId = ComponentId.of("input_pass").fold({ error("invalid id") }, { it })
    private val key = NodeKey(inputId)
    private val passKey = NodeKey(passwordId)

    @Test
    fun `initial state is empty and valid`() {
        val state = FormInteractionState.create()

        assertTrue(state.isValid())
        assertTrue(state.values.isEmpty())
        assertTrue(state.errors.isEmpty())
    }

    @Test
    fun `state initialized with errors is not valid`() {
        val error = ValidationError.Required("Field is required")
        val state = FormInteractionState.create(errors = mapOf(key to error))

        assertFalse(state.isValid())
        assertEquals(error, state.errors[key])
    }

    @Test
    fun `update on valid value stores value and emits InputValueChangedEvent`() {
        val node = InputNode(id = inputId, label = "User")
        val state = FormInteractionState.create()

        val outcome = state.update(key, FieldValue.Text("alice"), node, 1000L)

        assertEquals(FieldValue.Text("alice"), outcome.state.values[key])
        assertTrue(outcome.state.isValid())
        assertEquals(1, outcome.events.size)
        val event = outcome.events.first() as DomainEvent.InputValueChangedEvent
        assertEquals(key, event.key)
        assertEquals(FieldValue.Text("alice"), event.value)
        assertEquals(1000L, event.timestamp)
    }

    @Test
    fun `update on sensitive node masks value in telemetry event`() {
        val node = InputNode(id = passwordId, label = "Password", inputVariant = InputVariant.Password)
        val state = FormInteractionState.create()

        val outcome = state.update(passKey, FieldValue.Text("secret-pwd"), node, 1000L)

        assertEquals(FieldValue.Text("secret-pwd"), outcome.state.values[passKey])
        assertEquals(1, outcome.events.size)
        val event = outcome.events.first() as DomainEvent.InputValueChangedEvent
        assertEquals(FieldValue.Text("••••••••"), event.value)
    }

    @Test
    fun `update emits InputValidationFailedEvent only on transition from valid to invalid`() {
        val minRule = MinLengthRule.of(3).fold({ error("rule") }, { it })
        val node = InputNode(id = inputId, label = "User", validationRules = listOf(minRule))
        val state = FormInteractionState.create()

        // 1. Valid to Invalid -> emits failed event
        val outcome1 = state.update(key, FieldValue.Text("ab"), node, 1000L)
        assertEquals(2, outcome1.events.size)
        assertTrue(outcome1.events[0] is DomainEvent.InputValueChangedEvent)
        assertTrue(outcome1.events[1] is DomainEvent.InputValidationFailedEvent)
        assertFalse(outcome1.state.isValid())

        // 2. Invalid to Invalid -> no duplicate failed event
        val outcome2 = outcome1.state.update(key, FieldValue.Text("a"), node, 2000L)
        assertEquals(1, outcome2.events.size)
        assertTrue(outcome2.events[0] is DomainEvent.InputValueChangedEvent)
        assertFalse(outcome2.state.isValid())

        // 3. Invalid to Valid -> emits recovered event
        val outcome3 = outcome2.state.update(key, FieldValue.Text("alice"), node, 3000L)
        assertEquals(2, outcome3.events.size)
        assertTrue(outcome3.events[0] is DomainEvent.InputValueChangedEvent)
        assertTrue(outcome3.events[1] is DomainEvent.InputValidationRecoveredEvent)
        assertTrue(outcome3.state.isValid())
    }

    @Test
    fun `validateAll evaluates all inputs and dynamic keys in document`() {
        val rule = RequiredRule("Required")
        val node1 = InputNode(id = inputId, label = "User", validationRules = listOf(rule))
        val node2 = InputNode(id = passwordId, label = "Password", validationRules = listOf(rule))

        val root = VerticalLayoutNode(
            id = ComponentId.of("root").fold({ error("id") }, { it }),
            children = listOf(node1, node2)
        )
        val doc = LayoutDocument.create(SchemaVersion.CURRENT, root).fold({ error("doc") }, { it })

        // Provide value only for node1, node2 is left empty
        val state = FormInteractionState.create(values = mapOf(key to FieldValue.Text("filled")))
        val validatedState = state.validateAll(doc)

        assertFalse(validatedState.isValid())
        assertNull(validatedState.errors[key])
        assertNotNull(validatedState.errors[passKey])
    }
}

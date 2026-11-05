package com.libs.flex.ui.flexui.model

import com.libs.flex.ui.core.domain.model.node.InputNode
import com.libs.flex.ui.core.domain.model.node.TextNode
import com.libs.flex.ui.core.domain.model.node.VerticalLayoutNode
import com.libs.flex.ui.core.domain.validation.ValidationError
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.FieldValue
import com.libs.flex.ui.core.domain.value.InputVariant
import com.libs.flex.ui.core.domain.value.NodeKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FlexUiContentStateTest {

    private fun givenComponentId(id: String): ComponentId {
        return ComponentId.of(id).getOrNull()!!
    }

    private fun givenSampleTree(): VerticalLayoutNode {
        val emailInput = InputNode(
            id = givenComponentId("email_input"),
            label = "Email",
            inputVariant = InputVariant.Outlined
        )
        val passwordInput = InputNode(
            id = givenComponentId("password_input"),
            label = "Password",
            inputVariant = InputVariant.Password
        )
        val titleText = TextNode(
            id = givenComponentId("title_text"),
            text = "Login"
        )
        return VerticalLayoutNode(
            id = givenComponentId("root_container"),
            children = listOf(titleText, emailInput, passwordInput)
        )
    }

    @Test
    fun `toString sanitizes output and never prints raw values or passwords`() {
        val root = givenSampleTree()
        val emailKey = NodeKey(givenComponentId("email_input"))
        val passwordKey = NodeKey(givenComponentId("password_input"))

        val state = FlexUiContentState(
            layoutId = "login_screen",
            rootNode = root,
            values = mapOf(
                emailKey to FieldValue.Text("user@example.com"),
                passwordKey to FieldValue.Text("superSecretPassword123")
            )
        )

        val stringRepresentation = state.toString()

        // Verifica que no se expongan ni contraseñas ni emails en el log
        assertFalse(stringRepresentation.contains("superSecretPassword123"))
        assertFalse(stringRepresentation.contains("user@example.com"))

        // Verifica la estructura concisa y segura
        assertTrue(stringRepresentation.contains("layoutId='login_screen'"))
        assertTrue(stringRepresentation.contains("valuesCount=2"))
        assertTrue(stringRepresentation.contains("errorsCount=0"))
        assertTrue(stringRepresentation.contains("isSubmitting=false"))
    }

    @Test
    fun `values map retains raw values untouched for form submission`() {
        val root = givenSampleTree()
        val passwordKey = NodeKey(givenComponentId("password_input"))
        val rawPassword = FieldValue.Text("superSecretPassword123")

        val state = FlexUiContentState(
            layoutId = "login_screen",
            rootNode = root,
            values = mapOf(passwordKey to rawPassword)
        )

        // El mapa de valores debe conservar el valor real íntegro para enviar al backend
        assertEquals(rawPassword, state.values[passwordKey])
    }

    @Test
    fun `data class equality and hashCode are consistent with structural property values`() {
        val root = givenSampleTree()
        val key = NodeKey(givenComponentId("email_input"))
        val value = FieldValue.Text("user@example.com")
        val error = ValidationError.Required()

        val state1 = FlexUiContentState(
            layoutId = "form_a",
            rootNode = root,
            values = mapOf(key to value),
            errors = mapOf(key to error),
            isSubmitting = false
        )
        val state2 = FlexUiContentState(
            layoutId = "form_a",
            rootNode = root,
            values = mapOf(key to value),
            errors = mapOf(key to error),
            isSubmitting = false
        )
        val state3 = state1.copy(isSubmitting = true)

        assertEquals(state1, state2)
        assertEquals(state1.hashCode(), state2.hashCode())
        assertNotEquals(state1, state3)
    }

    @Test
    fun `data class generated copy creates new instance preserving unchanged fields`() {
        val root = givenSampleTree()
        val initial = FlexUiContentState(
            layoutId = "form_1",
            rootNode = root
        )

        val updated = initial.copy(isSubmitting = true)

        assertEquals(initial.layoutId, updated.layoutId)
        assertEquals(initial.rootNode, updated.rootNode)
        assertFalse(initial.isSubmitting)
        assertTrue(updated.isSubmitting)
    }
}

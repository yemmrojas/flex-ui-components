package com.libs.flex.ui.core.domain.model.node

import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.value.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

public class NodeAcceptanceTest {

    @Test
    public fun `InputNode accepts Text and rejects Number`() {
        val input = InputNode(
            id = ComponentId.of("txt_1").getOrNull()!!,
            label = "Name"
        )
        assertFalse(input.isSensitive)

        val textVal = FieldValue.Text("John")
        val numVal = FieldValue.Number(123.0)

        assertTrue(input.accept(textVal).isRight)
        assertTrue(input.accept(numVal).isLeft)
    }

    @Test
    public fun `InputNode with Password variant is sensitive`() {
        val pwd = InputNode(
            id = ComponentId.of("pwd_1").getOrNull()!!,
            label = "Password",
            inputVariant = InputVariant.Password
        )
        assertTrue(pwd.isSensitive)
    }

    @Test
    public fun `CheckNode accepts Bool and rejects Text`() {
        val check = CheckNode(
            id = ComponentId.of("chk_1").getOrNull()!!,
            label = "Accept terms",
            defaultChecked = true
        )
        assertTrue(check.accept(FieldValue.Bool(true)).isRight)
        assertTrue(check.accept(FieldValue.Text("true")).isLeft)
    }

    @Test
    public fun `SelectNode accepts valid selection indices and rejects out-of-bounds`() {
        val options = Nel.of(SelectOption("1", "First"), SelectOption("2", "Second"))
        val select = SelectNode.of(
            id = ComponentId.of("sel_1").getOrNull()!!,
            label = "Options",
            options = options
        )

        val validSelection = FieldValue.Selection(listOf(0, 1))
        val invalidSelection = FieldValue.Selection(listOf(2))

        assertTrue(select.accept(validSelection).isRight)
        assertTrue(select.accept(invalidSelection).isLeft)
    }

    @Test
    public fun `SliderNode accepts numbers within range and rejects out of bounds`() {
        val range = NumberRange.of(0.0, 10.0, 1.0).getOrNull()!!
        val slider = SliderNode(
            id = ComponentId.of("sld_1").getOrNull()!!,
            range = range
        )

        assertTrue(slider.accept(FieldValue.Number(5.0)).isRight)
        assertTrue(slider.accept(FieldValue.Number(15.0)).isLeft)
        assertTrue(slider.accept(FieldValue.Text("5")).isLeft)
    }

    @Test
    public fun `ButtonNode exposes InteractiveNode properties`() {
        val button = ButtonNode(
            id = ComponentId.of("btn_submit").getOrNull()!!,
            text = "Submit",
            actionId = ActionId.of("submit_form").getOrNull()!!,
            isEnabled = true,
            requiresValidation = true
        )

        assertEquals("submit_form", button.actionId.value)
        assertTrue(button.isEnabled)
        assertTrue(button.requiresValidation)
    }

    @Test
    public fun `FloatingButtonLayoutNode acts as both LayoutNode and InteractiveNode`() {
        val fab = FloatingButtonLayoutNode(
            id = ComponentId.of("fab_1").getOrNull()!!,
            fabIcon = "add",
            actionId = ActionId.of("create_item").getOrNull()!!,
            requiresValidation = false
        )

        assertTrue((fab as UIComponentNode) is LayoutNode)
        assertTrue((fab as UIComponentNode) is InteractiveNode)
        assertEquals("create_item", fab.actionId.value)
        assertFalse(fab.requiresValidation)
    }
}

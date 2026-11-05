package com.libs.flex.ui.flexui.model

import com.libs.flex.ui.core.domain.ports.ActionResult
import com.libs.flex.ui.core.domain.value.ActionId
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.FieldValue
import com.libs.flex.ui.core.domain.value.NodeKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FlexUiEventTest {

    private fun givenNodeKey(id: String = "btn_submit"): NodeKey {
        return NodeKey(ComponentId.of(id).getOrNull()!!)
    }

    private fun givenActionId(id: String = "submit_action"): ActionId {
        return ActionId.of(id).getOrNull()!!
    }

    @Test
    fun `ActionExecuted holds ActionResult variants correctly`() {
        val handledEvent = FlexUiEvent.ActionExecuted(ActionResult.Handled)
        val rejectedEvent = FlexUiEvent.ActionExecuted(ActionResult.Rejected("Invalid token"))
        val unhandledEvent = FlexUiEvent.ActionExecuted(ActionResult.Unhandled)

        assertEquals(ActionResult.Handled, handledEvent.result)
        assertEquals(ActionResult.Rejected("Invalid token"), rejectedEvent.result)
        assertEquals(ActionResult.Unhandled, unhandledEvent.result)
        assertNotEquals(handledEvent, rejectedEvent)
    }

    @Test
    fun `ToastMessage holds message and supports structural equality`() {
        val event1 = FlexUiEvent.ToastMessage("Operation completed")
        val event2 = FlexUiEvent.ToastMessage("Operation completed")
        val event3 = FlexUiEvent.ToastMessage("Error occurred")

        assertEquals(event1, event2)
        assertEquals(event1.hashCode(), event2.hashCode())
        assertNotEquals(event1, event3)
        assertEquals("Operation completed", event1.message)
    }

    @Test
    fun `NavigateTo holds target route and supports structural equality`() {
        val event1 = FlexUiEvent.NavigateTo("home/dashboard")
        val event2 = FlexUiEvent.NavigateTo("home/dashboard")
        val event3 = FlexUiEvent.NavigateTo("login")

        assertEquals(event1, event2)
        assertNotEquals(event1, event3)
        assertEquals("home/dashboard", event1.target)
    }

    @Test
    fun `FlexUiEvent hierarchy supports exhaustive when pattern matching`() {
        val events: List<FlexUiEvent> = listOf(
            FlexUiEvent.ActionExecuted(ActionResult.Handled),
            FlexUiEvent.ToastMessage("Welcome!"),
            FlexUiEvent.NavigateTo("profile")
        )

        val descriptions = events.map { event ->
            when (event) {
                is FlexUiEvent.ActionExecuted -> "action: ${event.result::class.simpleName}"
                is FlexUiEvent.ToastMessage -> "toast: ${event.message}"
                is FlexUiEvent.NavigateTo -> "navigate: ${event.target}"
            }
        }

        assertEquals(listOf("action: Handled", "toast: Welcome!", "navigate: profile"), descriptions)
    }

    @Test
    fun `UserAction models form payload and retains native FieldValues`() {
        val actionId = givenActionId("checkout")
        val sourceKey = givenNodeKey("btn_pay")
        val values = mapOf(
            givenNodeKey("input_amount") to FieldValue.Number(150.0),
            givenNodeKey("check_terms") to FieldValue.Bool(true),
            givenNodeKey("input_notes") to FieldValue.Text("Express delivery")
        )

        val userAction = UserAction(
            actionId = actionId,
            sourceKey = sourceKey,
            formValues = values
        )

        assertEquals(actionId, userAction.actionId)
        assertEquals(sourceKey, userAction.sourceKey)
        assertEquals(values, userAction.formValues)
        assertTrue(userAction.formValues[givenNodeKey("check_terms")] is FieldValue.Bool)
    }
}


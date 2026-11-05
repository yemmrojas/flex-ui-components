package com.libs.flex.ui.flexui.model

import com.libs.flex.ui.core.domain.model.node.TextNode
import com.libs.flex.ui.core.domain.value.ComponentId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FlexUiViewStateTest {

    private fun givenDummyNode(id: String = "test_text"): TextNode {
        val componentId = ComponentId.of(id).getOrNull()!!
        return TextNode(
            id = componentId,
            text = "Hello FlexUI"
        )
    }

    private fun givenContentState(layoutId: String = "layout_1"): FlexUiContentState {
        return FlexUiContentState(
            layoutId = layoutId,
            rootNode = givenDummyNode()
        )
    }

    @Test
    fun `Loading state behaves as singleton object`() {
        val state1: FlexUiViewState = FlexUiViewState.Loading
        val state2: FlexUiViewState = FlexUiViewState.Loading

        assertEquals(state1, state2)
        assertEquals("Loading", state1.toString())
    }

    @Test
    fun `Content state equality depends on underlying FlexUiContentState`() {
        val content1 = givenContentState("screen_a")
        val content2 = givenContentState("screen_a")
        val content3 = givenContentState("screen_b")

        val state1 = FlexUiViewState.Content(content1)
        val state2 = FlexUiViewState.Content(content2)
        val state3 = FlexUiViewState.Content(content3)

        assertEquals(state1, state2)
        assertNotEquals(state1, state3)
    }

    @Test
    fun `Error state correctly holds all FlexUiError variants`() {
        val errors = listOf(
            FlexUiError.InvalidJson("Malformed syntax", "$.root"),
            FlexUiError.UnsupportedVersion("9.9.9"),
            FlexUiError.ActionTimeout("submit_form"),
            FlexUiError.SessionError("Session expired", "TimeoutException"),
            FlexUiError.LayoutNotFound("unknown_layout")
        )

        errors.forEach { error ->
            val errorState = FlexUiViewState.Error(error)
            assertEquals(error, errorState.error)

            // Test exhaustive handling without exception
            val message = when (val err = errorState.error) {
                is FlexUiError.InvalidJson -> "json: ${err.message}"
                is FlexUiError.UnsupportedVersion -> "version: ${err.version}"
                is FlexUiError.ActionTimeout -> "timeout: ${err.actionId}"
                is FlexUiError.SessionError -> "session: ${err.userMessage}"
                is FlexUiError.LayoutNotFound -> "layout: ${err.layoutId}"
            }
            assertTrue(message.isNotBlank())
        }
    }

    @Test
    fun `FlexUiViewState supports exhaustive pattern matching`() {
        val states: List<FlexUiViewState> = listOf(
            FlexUiViewState.Loading,
            FlexUiViewState.Content(givenContentState()),
            FlexUiViewState.Error(FlexUiError.InvalidJson("syntax error"))
        )

        val descriptions = states.map { state ->
            when (state) {
                is FlexUiViewState.Loading -> "loading"
                is FlexUiViewState.Content -> "content: ${state.state.layoutId}"
                is FlexUiViewState.Error -> "error: ${state.error}"
            }
        }

        assertEquals(listOf("loading", "content: layout_1", "error: InvalidJson(message=syntax error, path=null)"), descriptions)
    }
}


package com.libs.flex.ui.flexui

import androidx.compose.material3.Text
import com.libs.flex.ui.core.domain.ports.ActionResult
import com.libs.flex.ui.core.domain.value.ActionId
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.NodeKey
import com.libs.flex.ui.flexui.model.UserAction
import com.libs.flex.ui.flexui.viewmodel.DefaultDispatcherProvider
import com.libs.flex.ui.flexui.viewmodel.DispatcherProvider
import com.libs.flex.ui.parser.ParserMode
import com.libs.flex.ui.parser.ParserOptions
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FlexUiConfigTest {

    private class TestDispatcherProvider(
        override val main: CoroutineDispatcher = Dispatchers.Unconfined,
        override val mainImmediate: CoroutineDispatcher = Dispatchers.Unconfined,
        override val io: CoroutineDispatcher = Dispatchers.Unconfined,
        override val default: CoroutineDispatcher = Dispatchers.Unconfined
    ) : DispatcherProvider

    @Test
    fun `default config initializes with sensible production defaults`() {
        val config = FlexUiConfig()

        assertEquals(ParserOptions.DEFAULT, config.parserOptions)
        assertTrue(config.dispatcherProvider is DefaultDispatcherProvider)
        assertTrue(config.customNodeRegistry.isEmpty())
        assertFalse(config.isReleaseMode)
        assertEquals(10_000L, config.actionTimeoutMs)
        assertEquals(null, config.hostActionHandler)
    }

    @Test
    fun `custom config allows overriding all parameters`() = runBlocking {
        val testDispatcherProvider = TestDispatcherProvider()
        val customParserOptions = ParserOptions(mode = ParserMode.STRICT)
        var actionHandled = false

        val config = FlexUiConfig(
            parserOptions = customParserOptions,
            dispatcherProvider = testDispatcherProvider,
            customNodeRegistry = mapOf(
                "custom_chart" to { node, modifier ->
                    Text("Chart ${node.id.value}")
                }
            ),
            isReleaseMode = true,
            actionTimeoutMs = 5_000L,
            hostActionHandler = { action ->
                actionHandled = true
                ActionResult.Handled
            }
        )

        assertEquals(ParserMode.STRICT, config.parserOptions.mode)
        assertEquals(testDispatcherProvider, config.dispatcherProvider)
        assertEquals(1, config.customNodeRegistry.size)
        assertTrue(config.isReleaseMode)
        assertEquals(5_000L, config.actionTimeoutMs)
        assertNotNull(config.hostActionHandler)

        val sampleAction = UserAction(
            actionId = ActionId.of("act").getOrNull()!!,
            sourceKey = NodeKey(ComponentId.of("btn").getOrNull()!!),
            formValues = emptyMap()
        )
        val result = config.hostActionHandler?.invoke(sampleAction)

        assertTrue(actionHandled)
        assertEquals(ActionResult.Handled, result)
    }
}


package com.libs.flex.ui.flexui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.libs.flex.ui.core.domain.model.node.CustomNode
import com.libs.flex.ui.core.domain.ports.ActionResult
import com.libs.flex.ui.flexui.model.UserAction
import com.libs.flex.ui.flexui.viewmodel.DefaultDispatcherProvider
import com.libs.flex.ui.flexui.viewmodel.DispatcherProvider
import com.libs.flex.ui.parser.ParserOptions

/**
 * Typealias for custom composable component renderers.
 */
typealias CustomNodeComposable = @Composable (node: CustomNode, modifier: Modifier) -> Unit

/**
 * Composition Root configuration for FlexUI runtime.
 *
 * Allows host applications to configure parsing policies, coroutine dispatchers,
 * interactive timeouts, fallback behavior, custom component factories, and action handlers
 * without coupling the runtime library to a specific dependency injection framework.
 *
 * @property parserOptions Parsing configuration (STRICT or TOLERANT, payload size limits).
 * @property dispatcherProvider Coroutine dispatchers for I/O and UI threads.
 * @property customNodeRegistry Map of custom component renderers keyed by their type discriminator string.
 * @property isReleaseMode Flag indicating whether release diagnostic policies apply (e.g. silent fallbacks).
 * @property actionTimeoutMs Timeout limit in milliseconds for user action execution before emitting ActionTimeout error.
 * @property hostActionHandler Suspending handler to process interactive actions dispatched by user gestures.
 */
data class FlexUiConfig(
    val parserOptions: ParserOptions = ParserOptions.DEFAULT,
    val dispatcherProvider: DispatcherProvider = DefaultDispatcherProvider(),
    val customNodeRegistry: Map<String, CustomNodeComposable> = emptyMap(),
    val isReleaseMode: Boolean = false,
    val actionTimeoutMs: Long = DEFAULT_ACTION_TIMEOUT_MS,
    val hostActionHandler: (suspend (UserAction) -> ActionResult)? = null
) {
    companion object {
        const val DEFAULT_ACTION_TIMEOUT_MS: Long = 10_000L
    }
}


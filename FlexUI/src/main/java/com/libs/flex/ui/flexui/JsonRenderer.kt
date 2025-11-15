package com.libs.flex.ui.flexui

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.libs.flex.ui.flexui.components.DefaultErrorContent
import com.libs.flex.ui.flexui.components.infrastructure.adapter.atomic.LoaderComponent
import com.libs.flex.ui.flexui.di.JsonRendererEntryPoint
import com.libs.flex.ui.flexui.model.AtomicDescriptor
import com.libs.flex.ui.flexui.model.ComponentDescriptor
import com.libs.flex.ui.flexui.model.ComponentEvent
import com.libs.flex.ui.flexui.model.ComponentType
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Main entry point for rendering dynamic UIs from JSON configuration.
 *
 * This composable function is the public API of the FlexUI library. It handles:
 * - JSON parsing with automatic caching for performance
 * - Background processing to avoid blocking the UI thread
 * - State management (loading, error, success)
 * - Component rendering with event handling
 * - Graceful error handling with customizable error UI
 *
 * The implementation follows Jetpack Compose best practices:
 * - Uses Hilt EntryPoint for dependency injection in composables
 * - Uses `remember` for caching dependencies and hash calculation
 * - Uses `LaunchedEffect` for side effects (parsing)
 * - Uses `mutableStateOf` for reactive state management
 * - Provides sensible defaults for all optional parameters
 *
 * **Requirements:**
 * - Your Application class must be annotated with `@HiltAndroidApp`
 * - Hilt must be properly configured in your project
 *
 * Performance optimizations:
 * - MD5 hash-based caching prevents redundant parsing
 * - Background parsing on Dispatchers.IO
 * - Efficient recomposition with remember keys
 * - LRU cache eviction for memory management
 *
 * @param jsonString JSON string describing the UI structure and hierarchy
 * @param onEvent Callback invoked when user interacts with components (clicks, value changes, selections)
 * @param modifier Modifier to be applied to the root component
 * @param errorContent Composable to display when parsing or rendering fails. Receives the error for custom handling.
 *
 * Example usage:
 * ```kotlin
 * @Composable
 * fun MyScreen() {
 *     val jsonConfig = """
 *         {
 *             "id": "main",
 *             "type": "contentVertical",
 *             "children": [
 *                 {
 *                     "id": "title",
 *                     "type": "componentTextView",
 *                     "text": "Hello FlexUI!"
 *                 }
 *             ]
 *         }
 *     """
 *
 *     JsonRenderer(
 *         jsonString = jsonConfig,
 *         onEvent = { event ->
 *             when (event) {
 *                 is ComponentEvent.Click -> handleClick(event)
 *                 is ComponentEvent.ValueChange -> handleValueChange(event)
 *                 is ComponentEvent.Selection -> handleSelection(event)
 *             }
 *         }
 *     )
 * }
 * ```
 *
 * With custom error handling:
 * ```kotlin
 * JsonRenderer(
 *     jsonString = config,
 *     onEvent = { event -> handleEvent(event) },
 *     errorContent = { error ->
 *         CustomErrorScreen(
 *             error = error,
 *             onRetry = { /* retry logic */ }
 *         )
 *     }
 * )
 * ```
 *
 * @see ComponentEvent for available event types
 * @see DefaultErrorContent for the default error UI
 */
@Composable
@SuppressLint("ModifierParameter")
fun JsonRenderer(
    jsonString: String,
    onEvent: (ComponentEvent) -> Unit = {},
    modifier: Modifier = Modifier,
    errorContent: @Composable (Throwable) -> Unit = { DefaultErrorContent(it) }
) {
    // Get dependencies from Hilt using EntryPoint
    val context = LocalContext.current
    val dependencies = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            JsonRendererEntryPoint::class.java
        )
    }

    val cache = remember { dependencies.componentCache() }
    val parser = remember { dependencies.jsonParser() }
    val factory = remember { dependencies.componentFactory() }

    // Calculate hash - recomputes only when jsonString changes
    val jsonHash = remember(jsonString) {
        cache.generateKey(jsonString)
    }

    // State management
    var descriptor by remember { mutableStateOf<ComponentDescriptor?>(null) }
    var error by remember { mutableStateOf<Throwable?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Parse JSON with cache check - runs when jsonHash changes
    LaunchedEffect(jsonHash) {
        isLoading = true
        error = null
        descriptor = null

        try {
            // Check cache first
            val cached = cache.get(jsonHash)

            if (cached != null) {
                // Cache hit - instant rendering
                descriptor = cached
                isLoading = false
            } else {
                // Cache miss - parse JSON on background thread
                withContext(Dispatchers.IO) {
                    val result = parser.parse(jsonString)

                    result.fold(
                        onSuccess = { parsed ->
                            // Store in cache for future use
                            cache.put(jsonHash, parsed)
                            descriptor = parsed
                        },
                        onFailure = { throwable ->
                            error = throwable
                        }
                    )
                }
                isLoading = false
            }
        } catch (e: Exception) {
            error = e
            isLoading = false
        }
    }

    // Render based on current state
    when {
        isLoading -> {
            // Show loader while parsing
            LoaderComponent(
                descriptor = AtomicDescriptor(
                    id = "json_renderer_loader",
                    type = ComponentType.COMPONENT_LOADER,
                    loaderStyle = "circular"
                ),
                modifier = modifier
            )
        }

        error != null -> {
            // Show error content
            errorContent(error!!)
        }

        descriptor != null -> {
            // Render component tree
            factory.CreateComponent(
                descriptor = descriptor!!,
                onEvent = onEvent,
                modifier = modifier
            )
        }
    }
}


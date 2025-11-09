package com.libs.flex.ui.flexui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.libs.flex.ui.flexui.components.domain.ports.ComponentRendererStrategyPort
import com.libs.flex.ui.flexui.model.ComponentDescriptor
import com.libs.flex.ui.flexui.model.ComponentEvent
import com.libs.flex.ui.flexui.styling.domain.ports.StyleResolverPort
import javax.inject.Inject

/**
 * Factory for creating Compose components from descriptors.
 *
 * This factory follows the Strategy Pattern to delegate component rendering
 * to specialized strategies. It applies styling consistently and routes
 * descriptors to the appropriate strategy based on component type.
 *
 * Architecture:
 * - ComponentFactory: Orchestrates rendering (Single Responsibility)
 * - StyleResolverPort: Applies styles (Dependency Inversion)
 * - ComponentRendererStrategyPort: Renders components (Open/Closed Principle)
 *
 * Benefits:
 * - Open/Closed: Add new strategies without modifying factory
 * - Single Responsibility: Factory only orchestrates, strategies render
 * - Testable: Mock strategies independently
 * - Extensible: Easy to add new component types
 *
 * @property styleResolver Port for applying style properties to modifiers
 * @property strategies List of rendering strategies for different component types
 */
class ComponentFactory @Inject constructor(
    private val styleResolver: StyleResolverPort,
    private val strategies: List<@JvmSuppressWildcards ComponentRendererStrategyPort>
) {

    /**
     * Creates a Compose component from a descriptor.
     *
     * This is the main entry point for rendering components. It:
     * 1. Applies styling to the modifier
     * 2. Finds the appropriate strategy for the component type
     * 3. Delegates rendering to the strategy
     *
     * @param descriptor Component descriptor (LayoutDescriptor or AtomicDescriptor)
     * @param onEvent Callback invoked when user interacts with components
     * @param modifier Base modifier to apply to the component
     *
     * Example:
     * ```
     * componentFactory.CreateComponent(
     *     descriptor = buttonDescriptor,
     *     onEvent = { event -> handleEvent(event) },
     *     modifier = Modifier.fillMaxWidth()
     * )
     * ```
     */
    @Composable
    fun CreateComponent(
        descriptor: ComponentDescriptor,
        onEvent: (ComponentEvent) -> Unit,
        modifier: Modifier = Modifier
    ) {
        // Apply styles to modifier before passing to strategy
        val styledModifier = styleResolver.applyStyles(modifier, descriptor.style)
        
        // Find strategy that can render this component type
        val strategy = strategies.firstOrNull { it.canRender(descriptor.type) }
        
        if (strategy != null) {
            strategy.Render(descriptor, onEvent, styledModifier)
        } else {
            ErrorPlaceholder(
                message = "No renderer found for component type: ${descriptor.type}",
                modifier = styledModifier
            )
        }
    }
}

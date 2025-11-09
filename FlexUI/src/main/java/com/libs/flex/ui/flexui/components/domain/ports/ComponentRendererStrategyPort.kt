package com.libs.flex.ui.flexui.components.domain.ports

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.libs.flex.ui.flexui.model.ComponentDescriptor
import com.libs.flex.ui.flexui.model.ComponentEvent
import com.libs.flex.ui.flexui.model.ComponentType

/**
 * Port for component rendering strategies.
 *
 * This interface defines the contract for rendering different types of components.
 * Following the Strategy Pattern, each implementation handles a specific category
 * of components (layouts or atomic components).
 *
 * Benefits:
 * - Open/Closed Principle: Add new strategies without modifying existing code
 * - Single Responsibility: Each strategy handles one category
 * - Testable: Mock strategies independently
 * - Extensible: Easy to add new component types
 */
interface ComponentRendererStrategyPort {
    
    /**
     * Determines if this strategy can render the given component type.
     *
     * @param type Component type to check
     * @return true if this strategy can render the component type
     */
    fun canRender(type: ComponentType): Boolean
    
    /**
     * Renders a component using this strategy.
     *
     * @param descriptor Component descriptor with configuration
     * @param onEvent Callback for component events
     * @param modifier Styled modifier to apply to the component
     */
    @Composable
    fun Render(
        descriptor: ComponentDescriptor,
        onEvent: (ComponentEvent) -> Unit,
        modifier: Modifier
    )
}

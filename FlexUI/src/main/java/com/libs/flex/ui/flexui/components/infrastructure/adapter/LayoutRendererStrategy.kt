package com.libs.flex.ui.flexui.components.infrastructure.adapter

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.libs.flex.ui.flexui.components.ErrorPlaceholder
import com.libs.flex.ui.flexui.components.domain.ports.ComponentRendererStrategyPort
import com.libs.flex.ui.flexui.model.ComponentDescriptor
import com.libs.flex.ui.flexui.model.ComponentEvent
import com.libs.flex.ui.flexui.model.ComponentType
import com.libs.flex.ui.flexui.model.LayoutDescriptor
import javax.inject.Inject

/**
 * Strategy for rendering layout container components.
 *
 * This strategy handles all layout types that can contain child components:
 * - CONTENT_VERTICAL: Vertical column layout
 * - CONTENT_HORIZONTAL: Horizontal row layout
 * - CONTENT_SCROLL: Scrollable container
 * - CONTENT_WITH_FLOATING_BUTTON: Scaffold with FAB
 * - CONTENT_LIST: Lazy list container
 * - CONTENT_SLIDER: Image slider/carousel
 *
 * Following the Strategy Pattern, this class is responsible only for
 * routing layout descriptors to their appropriate implementations.
 */
class LayoutRendererStrategy @Inject constructor() : ComponentRendererStrategyPort {

    override fun canRender(type: ComponentType): Boolean = type.isLayout


    @Composable
    override fun Render(
        descriptor: ComponentDescriptor,
        onEvent: (ComponentEvent) -> Unit,
        modifier: Modifier
    ) {
        require(descriptor is LayoutDescriptor) {
            MESSAGE_ERROR_LAYOUT.format(descriptor::class.simpleName)
        }

        when (descriptor.type) {
            ComponentType.CONTENT_VERTICAL -> {
                // TODO: Implement in task 9.1
                ErrorPlaceholder("VerticalContainer not yet implemented", modifier)
            }

            ComponentType.CONTENT_HORIZONTAL -> {
                // TODO: Implement in task 9.2
                ErrorPlaceholder("HorizontalContainer not yet implemented", modifier)
            }

            ComponentType.CONTENT_SCROLL -> {
                // TODO: Implement in task 9.3
                ErrorPlaceholder("ScrollContainer not yet implemented", modifier)
            }

            ComponentType.CONTENT_WITH_FLOATING_BUTTON -> {
                // TODO: Implement in task 9.4
                ErrorPlaceholder("FloatingButtonContainer not yet implemented", modifier)
            }

            ComponentType.CONTENT_LIST -> {
                // TODO: Implement in task 9.5
                ErrorPlaceholder("ListContainer not yet implemented", modifier)
            }

            ComponentType.CONTENT_SLIDER -> {
                // TODO: Implement in task 9.6
                ErrorPlaceholder("SliderContainer not yet implemented", modifier)
            }

            else -> ErrorPlaceholder(MESSAGE_ERROR_PLACEHOLDER.format(descriptor.type), modifier)
        }
    }

    companion object {
        const val MESSAGE_ERROR_LAYOUT =
            "LayoutRendererStrategy requires LayoutDescriptor but got %s"
        const val MESSAGE_ERROR_PLACEHOLDER = "Unknown layout type: %s"
    }
}

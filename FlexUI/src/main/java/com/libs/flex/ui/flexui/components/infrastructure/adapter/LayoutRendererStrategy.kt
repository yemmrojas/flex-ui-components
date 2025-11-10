package com.libs.flex.ui.flexui.components.infrastructure.adapter

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.libs.flex.ui.flexui.components.ComponentFactory
import com.libs.flex.ui.flexui.components.ErrorPlaceholder
import com.libs.flex.ui.flexui.components.domain.ports.ComponentRendererStrategyPort
import com.libs.flex.ui.flexui.components.infrastructure.adapter.container.FloatingButtonContainer
import com.libs.flex.ui.flexui.components.infrastructure.adapter.container.HorizontalContainer
import com.libs.flex.ui.flexui.components.infrastructure.adapter.container.ListContainer
import com.libs.flex.ui.flexui.components.infrastructure.adapter.container.ScrollContainer
import com.libs.flex.ui.flexui.components.infrastructure.adapter.container.SliderContainer
import com.libs.flex.ui.flexui.components.infrastructure.adapter.container.VerticalContainer
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
 *
 * @property componentFactory Factory for creating child components recursively
 */
class LayoutRendererStrategy @Inject constructor(
    private val componentFactory: ComponentFactory
) : ComponentRendererStrategyPort {

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
            ComponentType.CONTENT_VERTICAL ->
                VerticalContainer(descriptor, onEvent, modifier, componentFactory)

            ComponentType.CONTENT_HORIZONTAL ->
                HorizontalContainer(descriptor, onEvent, modifier, componentFactory)

            ComponentType.CONTENT_SCROLL ->
                ScrollContainer(descriptor, onEvent, modifier, componentFactory)

            ComponentType.CONTENT_WITH_FLOATING_BUTTON ->
                FloatingButtonContainer(descriptor, onEvent, modifier, componentFactory)

            ComponentType.CONTENT_LIST ->
                ListContainer(descriptor, onEvent, modifier, componentFactory)

            ComponentType.CONTENT_SLIDER ->
                SliderContainer(descriptor, onEvent, modifier, componentFactory)

            else -> ErrorPlaceholder(MESSAGE_ERROR_PLACEHOLDER.format(descriptor.type), modifier)
        }
    }

    companion object {
        const val MESSAGE_ERROR_LAYOUT =
            "LayoutRendererStrategy requires LayoutDescriptor but got %s"
        const val MESSAGE_ERROR_PLACEHOLDER = "Unknown layout type: %s"
    }
}

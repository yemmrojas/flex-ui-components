package com.libs.flex.ui.flexui.components.infrastructure.adapter

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.libs.flex.ui.flexui.components.ErrorPlaceholder
import com.libs.flex.ui.flexui.components.domain.ports.ComponentRendererStrategyPort
import com.libs.flex.ui.flexui.components.infrastructure.adapter.atomic.ButtonComponent
import com.libs.flex.ui.flexui.components.infrastructure.adapter.atomic.CheckComponent
import com.libs.flex.ui.flexui.components.infrastructure.adapter.atomic.ImageComponent
import com.libs.flex.ui.flexui.components.infrastructure.adapter.atomic.InputComponent
import com.libs.flex.ui.flexui.components.infrastructure.adapter.atomic.LoaderComponent
import com.libs.flex.ui.flexui.components.infrastructure.adapter.atomic.SelectComponent
import com.libs.flex.ui.flexui.components.infrastructure.adapter.atomic.SliderCheckComponent
import com.libs.flex.ui.flexui.components.infrastructure.adapter.atomic.TextViewComponent
import com.libs.flex.ui.flexui.components.infrastructure.adapter.atomic.ToastComponent
import com.libs.flex.ui.flexui.model.AtomicDescriptor
import com.libs.flex.ui.flexui.model.ComponentDescriptor
import com.libs.flex.ui.flexui.model.ComponentEvent
import com.libs.flex.ui.flexui.model.ComponentType
import javax.inject.Inject

/**
 * Strategy for rendering atomic components.
 *
 * This strategy handles all atomic component types that don't contain children:
 * - COMPONENT_INPUT: Text input field
 * - COMPONENT_TEXT_VIEW: Text display
 * - COMPONENT_CHECK: Checkbox
 * - COMPONENT_SELECT: Dropdown select
 * - COMPONENT_SLIDER_CHECK: Slider control
 * - COMPONENT_BUTTON: Button
 * - COMPONENT_IMAGE: Image display
 * - COMPONENT_LOADER: Loading indicator
 * - COMPONENT_TOAST: Toast notification
 *
 * Following the Strategy Pattern, this class is responsible only for
 * routing atomic descriptors to their appropriate implementations.
 */
class AtomicRendererStrategy @Inject constructor() : ComponentRendererStrategyPort {

    override fun canRender(type: ComponentType): Boolean = !type.isLayout

    @Composable
    override fun Render(
        descriptor: ComponentDescriptor,
        onEvent: (ComponentEvent) -> Unit,
        modifier: Modifier
    ) {
        require(descriptor is AtomicDescriptor) {
            MESSAGE_ERROR_COMPONENTS.format(descriptor::class.simpleName)
        }

        when (descriptor.type) {
            ComponentType.COMPONENT_INPUT ->
                InputComponent(descriptor, onEvent, modifier)

            ComponentType.COMPONENT_TEXT_VIEW ->
                TextViewComponent(descriptor, modifier)

            ComponentType.COMPONENT_CHECK ->
                CheckComponent(descriptor, onEvent, modifier)

            ComponentType.COMPONENT_SELECT ->
                SelectComponent(descriptor, onEvent, modifier)

            ComponentType.COMPONENT_SLIDER_CHECK ->
                SliderCheckComponent(descriptor, onEvent, modifier)

            ComponentType.COMPONENT_BUTTON ->
                ButtonComponent(descriptor, onEvent, modifier)

            ComponentType.COMPONENT_IMAGE ->
                ImageComponent(descriptor, modifier)

            ComponentType.COMPONENT_LOADER ->
                LoaderComponent(descriptor, modifier)

            ComponentType.COMPONENT_TOAST ->
                ToastComponent(descriptor, modifier)

            else -> ErrorPlaceholder(MESSAGE_ERROR_PLACEHOLDER.format(descriptor.type), modifier)
        }
    }

    companion object {
        const val MESSAGE_ERROR_COMPONENTS =
            "AtomicRendererStrategy requires AtomicDescriptor but got %s"
        const val MESSAGE_ERROR_PLACEHOLDER = "Unknown component type: %s"
    }
}

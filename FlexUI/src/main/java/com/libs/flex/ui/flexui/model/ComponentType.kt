package com.libs.flex.ui.flexui.model

import kotlinx.serialization.Serializable

/**
 * Enum representing all available component types in the FlexUI system.
 * Components are divided into layout containers and atomic components.
 */
@Serializable
enum class ComponentType {
    // Layout Containers
    CONTENT_VERTICAL,
    CONTENT_HORIZONTAL,
    CONTENT_SCROLL,
    CONTENT_WITH_FLOATING_BUTTON,
    CONTENT_LIST,
    CONTENT_SLIDER,
    
    // Atomic Components
    COMPONENT_INPUT,
    COMPONENT_TEXT_VIEW,
    COMPONENT_CHECK,
    COMPONENT_SELECT,
    COMPONENT_SLIDER_CHECK,
    COMPONENT_BUTTON,
    COMPONENT_IMAGE,
    COMPONENT_LOADER,
    COMPONENT_TOAST
}

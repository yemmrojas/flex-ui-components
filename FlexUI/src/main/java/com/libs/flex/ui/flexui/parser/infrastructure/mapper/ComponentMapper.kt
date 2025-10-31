package com.libs.flex.ui.flexui.parser.infrastructure.mapper

import com.libs.flex.ui.flexui.exceptions.ComponentTypeNotFoundException
import com.libs.flex.ui.flexui.model.ComponentType

/**
 * Object responsible for mapping component type strings from JSON to ComponentType enum values.
 *
 * This mapper provides a centralized location for type string to enum conversion and
 * distinguishes between layout containers and atomic components.
 */
object ComponentMapper {
    
    /**
     * Map of JSON type strings to ComponentType enum values.
     */
    private val typeMap = mapOf(
        // Layout Containers
        "contentVertical" to ComponentType.CONTENT_VERTICAL,
        "contentHorizontal" to ComponentType.CONTENT_HORIZONTAL,
        "contentScroll" to ComponentType.CONTENT_SCROLL,
        "contentWithFloatingButton" to ComponentType.CONTENT_WITH_FLOATING_BUTTON,
        "contentList" to ComponentType.CONTENT_LIST,
        "contentSlider" to ComponentType.CONTENT_SLIDER,
        
        // Atomic Components
        "componentInput" to ComponentType.COMPONENT_INPUT,
        "componentTextView" to ComponentType.COMPONENT_TEXT_VIEW,
        "componentCheck" to ComponentType.COMPONENT_CHECK,
        "componentSelect" to ComponentType.COMPONENT_SELECT,
        "componentSliderCheck" to ComponentType.COMPONENT_SLIDER_CHECK,
        "componentButton" to ComponentType.COMPONENT_BUTTON,
        "componentImage" to ComponentType.COMPONENT_IMAGE,
        "componentLoader" to ComponentType.COMPONENT_LOADER,
        "componentToast" to ComponentType.COMPONENT_TOAST
    )
    
    /**
     * Set of layout container types for efficient lookup.
     */
    private val layoutTypes = setOf(
        ComponentType.CONTENT_VERTICAL,
        ComponentType.CONTENT_HORIZONTAL,
        ComponentType.CONTENT_SCROLL,
        ComponentType.CONTENT_WITH_FLOATING_BUTTON,
        ComponentType.CONTENT_LIST,
        ComponentType.CONTENT_SLIDER
    )
    
    /**
     * Maps a component type string from JSON to its corresponding ComponentType enum value.
     *
     * @param typeString The component type string from JSON (e.g., "contentVertical", "componentButton")
     * @return The corresponding ComponentType enum value
     * @throws ComponentTypeNotFoundException if the type string is not recognized
     */
    fun mapType(typeString: String): ComponentType {
        return typeMap[typeString] 
            ?: throw ComponentTypeNotFoundException(typeString)
    }
    
    /**
     * Determines whether a ComponentType represents a layout container.
     *
     * Layout containers can contain child components, while atomic components cannot.
     *
     * @param type The ComponentType to check
     * @return true if the type is a layout container, false if it's an atomic component
     */
    fun isLayoutType(type: ComponentType): Boolean {
        return type in layoutTypes
    }
}

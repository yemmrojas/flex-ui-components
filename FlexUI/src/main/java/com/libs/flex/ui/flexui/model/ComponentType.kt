package com.libs.flex.ui.flexui.model

import kotlinx.serialization.Serializable

/**
 * Enum representing all available component types in the FlexUI system.
 * 
 * Each component type has:
 * - jsonKey: The string identifier used in JSON
 * - isLayout: Whether this component can contain children
 * 
 * This design follows the Open/Closed Principle:
 * - Open for extension: Add new types by adding enum values
 * - Closed for modification: No need to modify mapper logic
 */
@Serializable
enum class ComponentType(
    val jsonKey: String,
    val isLayout: Boolean
) {
    // Layout Containers (can contain children)
    CONTENT_VERTICAL("contentVertical", isLayout = true),
    CONTENT_HORIZONTAL("contentHorizontal", isLayout = true),
    CONTENT_SCROLL("contentScroll", isLayout = true),
    CONTENT_WITH_FLOATING_BUTTON("contentWithFloatingButton", isLayout = true),
    CONTENT_LIST("contentList", isLayout = true),
    CONTENT_SLIDER("contentSlider", isLayout = true),
    
    // Atomic Components (leaf nodes)
    COMPONENT_INPUT("componentInput", isLayout = false),
    COMPONENT_TEXT_VIEW("componentTextView", isLayout = false),
    COMPONENT_CHECK("componentCheck", isLayout = false),
    COMPONENT_SELECT("componentSelect", isLayout = false),
    COMPONENT_SLIDER_CHECK("componentSliderCheck", isLayout = false),
    COMPONENT_BUTTON("componentButton", isLayout = false),
    COMPONENT_IMAGE("componentImage", isLayout = false),
    COMPONENT_LOADER("componentLoader", isLayout = false),
    COMPONENT_TOAST("componentToast", isLayout = false);
    
    companion object {
        /**
         * Lazy-initialized map for efficient lookup by JSON key.
         * Built once on first access using all enum values.
         */
        private val jsonKeyMap: Map<String, ComponentType> by lazy {
            ComponentType.entries.associateBy { it.jsonKey }
        }
        
        /**
         * Finds a ComponentType by its JSON key.
         * 
         * @param jsonKey The JSON key to look up (case-sensitive)
         * @return The matching ComponentType, or null if not found
         */
        fun fromJsonKey(jsonKey: String): ComponentType? {
            return jsonKeyMap[jsonKey]
        }
        
        /**
         * Finds a ComponentType by its JSON key (case-insensitive).
         * 
         * @param jsonKey The JSON key to look up
         * @return The matching ComponentType, or null if not found
         */
        fun fromJsonKeyIgnoreCase(jsonKey: String): ComponentType? {
            return jsonKeyMap[jsonKey] 
                ?: ComponentType.entries.firstOrNull { it.jsonKey.equals(jsonKey, ignoreCase = true) }
        }
    }
}

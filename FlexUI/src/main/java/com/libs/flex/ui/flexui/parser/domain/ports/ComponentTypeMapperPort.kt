package com.libs.flex.ui.flexui.parser.domain.ports

import com.libs.flex.ui.flexui.model.ComponentType

/**
 * Port (interface) for mapping component type strings to ComponentType enum values.
 *
 * This port follows the Dependency Inversion Principle:
 * - High-level modules (domain) depend on abstractions (this port)
 * - Low-level modules (infrastructure) implement the abstraction
 *
 * Benefits:
 * - Domain layer doesn't depend on infrastructure implementation
 * - Easy to swap implementations
 * - Easy to mock in tests
 * - Follows hexagonal architecture principles
 */
interface ComponentTypeMapperPort {
    
    /**
     * Maps a component type string from JSON to its corresponding ComponentType enum value.
     *
     * @param typeString The component type string from JSON (e.g., "contentVertical", "componentButton")
     * @return The corresponding ComponentType enum value
     * @throws com.libs.flex.ui.flexui.exceptions.ComponentTypeNotFoundException if the type string is not recognized
     */
    fun mapType(typeString: String): ComponentType
    
    /**
     * Maps a component type string from JSON to its corresponding ComponentType enum value (case-insensitive).
     *
     * @param typeString The component type string from JSON
     * @return The corresponding ComponentType enum value
     * @throws com.libs.flex.ui.flexui.exceptions.ComponentTypeNotFoundException if the type string is not recognized
     */
    fun mapTypeIgnoreCase(typeString: String): ComponentType
    
    /**
     * Determines whether a ComponentType represents a layout container.
     *
     * Layout containers can contain child components, while atomic components cannot.
     *
     * @param type The ComponentType to check
     * @return true if the type is a layout container, false if it's an atomic component
     */
    fun isLayoutType(type: ComponentType): Boolean
}

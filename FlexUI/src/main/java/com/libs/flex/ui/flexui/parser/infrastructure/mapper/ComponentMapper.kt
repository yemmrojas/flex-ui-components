package com.libs.flex.ui.flexui.parser.infrastructure.mapper

import com.libs.flex.ui.flexui.exceptions.ComponentTypeNotFoundException
import com.libs.flex.ui.flexui.model.ComponentType
import com.libs.flex.ui.flexui.parser.domain.ports.ComponentTypeMapperPort
import javax.inject.Inject

/**
 * Infrastructure adapter that implements ComponentTypeMapperPort.
 *
 * This implementation follows hexagonal architecture and SOLID principles:
 * - Single Responsibility: Only handles type mapping
 * - Open/Closed: Extensible through ComponentType enum, closed for modification
 * - Liskov Substitution: Can be replaced with any ComponentTypeMapperPort implementation
 * - Interface Segregation: Implements focused ComponentTypeMapperPort interface
 * - Dependency Inversion: Domain depends on port (interface), not this implementation
 *
 * Benefits of implementing an interface:
 * - Domain layer doesn't depend on infrastructure
 * - Easy to swap implementations (e.g., database-based mapper, remote config)
 * - Easy to mock in tests
 * - Follows hexagonal architecture (ports and adapters)
 * - No global state coupling
 *
 * The actual mapping logic is delegated to ComponentType enum properties,
 * eliminating duplication and ensuring compile-time safety.
 */
class ComponentMapper @Inject constructor() : ComponentTypeMapperPort {

    /**
     * Maps a component type string from JSON to its corresponding ComponentType enum value.
     *
     * This method delegates to ComponentType.fromJsonKey() which uses the enum's
     * jsonKey property, ensuring single source of truth.
     *
     * @param typeString The component type string from JSON (e.g., "contentVertical", "componentButton")
     * @return The corresponding ComponentType enum value
     * @throws ComponentTypeNotFoundException if the type string is not recognized
     */
    override fun mapType(typeString: String): ComponentType {
        return ComponentType.fromJsonKey(typeString)
            ?: throw ComponentTypeNotFoundException(typeString)
    }

    /**
     * Maps a component type string from JSON to its corresponding ComponentType enum value (case-insensitive).
     *
     * Useful for more lenient parsing when JSON keys might have inconsistent casing.
     *
     * @param typeString The component type string from JSON
     * @return The corresponding ComponentType enum value
     * @throws ComponentTypeNotFoundException if the type string is not recognized
     */
    override fun mapTypeIgnoreCase(typeString: String): ComponentType {
        return ComponentType.fromJsonKeyIgnoreCase(typeString)
            ?: throw ComponentTypeNotFoundException(typeString)
    }

    /**
     * Determines whether a ComponentType represents a layout container.
     *
     * This method delegates to the ComponentType.isLayout property,
     * ensuring the information is maintained in a single place.
     *
     * Layout containers can contain child components, while atomic components cannot.
     *
     * @param type The ComponentType to check
     * @return true if the type is a layout container, false if it's an atomic component
     */
    override fun isLayoutType(type: ComponentType): Boolean {
        return type.isLayout
    }
}

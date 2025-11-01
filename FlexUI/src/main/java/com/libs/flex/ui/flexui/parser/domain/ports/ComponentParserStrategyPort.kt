package com.libs.flex.ui.flexui.parser.domain.ports

import com.libs.flex.ui.flexui.model.ComponentDescriptor
import com.libs.flex.ui.flexui.model.ComponentType
import kotlinx.serialization.json.JsonObject

/**
 * Output Port for component parsing strategies.
 *
 * This interface defines the contract for parsing strategies that
 * convert JSON objects into specific ComponentDescriptor types.
 *
 * In Hexagonal Architecture (within parser module):
 * - This is a SECONDARY/DRIVEN port
 * - Defines dependencies that the parser domain needs
 * - Implemented by infrastructure adapters
 */
interface ComponentParserStrategyPort {
    /**
     * Determines if this strategy can handle the given component type.
     *
     * @param type The component type
     * @return true if this strategy can parse the type, false otherwise
     */
    fun canParse(type: ComponentType): Boolean

    /**
     * Parses a JsonObject into a ComponentDescriptor.
     *
     * @param jsonObject The JSON object to parse
     * @param type The component type
     * @param recursiveParser Function for parsing nested components
     * @return The parsed ComponentDescriptor
     */
    fun parse(
        jsonObject: JsonObject,
        type: ComponentType,
        recursiveParser: (JsonObject) -> ComponentDescriptor
    ): ComponentDescriptor
}

package com.libs.flex.ui.flexui.parser.domain.ports

import com.libs.flex.ui.flexui.model.ComponentDescriptor

/**
 * Input Port for parsing JSON into ComponentDescriptor.
 *
 * This interface defines the contract for the parser module's main use case.
 * It represents what the parser module can do from the outside perspective.
 *
 * In Hexagonal Architecture (within parser module):
 * - This is a PRIMARY/DRIVING port
 * - Defines the parser module's public API
 * - Independent of implementation details
 */
interface ParseComponentPort {
    /**
     * Parses a JSON string into a ComponentDescriptor hierarchy.
     *
     * @param jsonString The JSON string to parse
     * @return Result containing the parsed ComponentDescriptor on success, or an exception on failure
     */
    suspend fun parse(jsonString: String): Result<ComponentDescriptor>
}

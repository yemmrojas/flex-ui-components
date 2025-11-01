package com.libs.flex.ui.flexui.parser.application

import com.libs.flex.ui.flexui.model.ComponentDescriptor
import com.libs.flex.ui.flexui.parser.domain.ports.ComponentParserStrategyPort
import com.libs.flex.ui.flexui.parser.domain.ports.ParseComponentPort
import com.libs.flex.ui.flexui.parser.domain.service.JsonParserService
import com.libs.flex.ui.flexui.parser.infrastructure.adapter.AtomicParserStrategy
import com.libs.flex.ui.flexui.parser.infrastructure.adapter.LayoutParserStrategy
import javax.inject.Inject

/**
 * Application Facade for the Parser module.
 *
 * This facade provides a simplified interface for JSON parsing,
 * hiding the complexity of the hexagonal architecture within the parser module.
 *
 * In Hexagonal Architecture (within parser module):
 * - This is part of the APPLICATION layer
 * - Wires together domain and infrastructure
 * - Provides dependency injection
 * - Maintains backward compatibility
 *
 * @property parserService The parsing service implementation
 */
class JsonParserFacade @Inject constructor(
    private val parserService: ParseComponentPort
) {

    /**
     * Parses a JSON string into a ComponentDescriptor hierarchy.
     *
     * @param jsonString The JSON string to parse
     * @return Result containing the parsed ComponentDescriptor on success, or an exception on failure
     */
    suspend fun parse(jsonString: String): Result<ComponentDescriptor> {
        return parserService.parse(jsonString)
    }
}

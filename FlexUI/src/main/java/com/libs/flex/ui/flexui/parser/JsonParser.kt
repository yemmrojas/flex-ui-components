package com.libs.flex.ui.flexui.parser

import com.libs.flex.ui.flexui.model.ComponentDescriptor
import com.libs.flex.ui.flexui.parser.application.JsonParserFacade
import com.libs.flex.ui.flexui.parser.domain.ports.ComponentParserStrategyPort

/**
 * Main entry point for JSON parsing in the FlexUI library.
 *
 * This class provides a simple, backward-compatible API while internally
 * using hexagonal architecture within the parser module.
 *
 * Module Architecture (Hexagonal within parser package):
 * ```
 * parser/
 * ├── JsonParser (Public API - this class)
 * ├── application/
 * │   └── JsonParserFacade (Wiring & DI)
 * ├── domain/
 * │   ├── ports/ (Interfaces)
 * │   └── service/ (Business Logic)
 * └── infrastructure/
 *     ├── adapter/ (Strategy Implementations)
 *     ├── property/ (Property Parsers)
 *     ├── mapper/ (Type Mapping)
 *     └── util/ (Extensions & Utilities)
 * ```
 *
 * Benefits of this modular hexagonal architecture:
 * - Each module (parser, renderer, validator) has its own architecture
 * - Easy to understand and maintain
 * - Clear boundaries between modules
 * - Testable in isolation
 *
 * Usage:
 * ```kotlin
 * val parser = JsonParser()
 * val result = parser.parse(jsonString)
 *
 * result.fold(
 *     onSuccess = { descriptor -> render(descriptor) },
 *     onFailure = { error -> handleError(error) }
 * )
 * ```
 *
 * @property strategies List of parsing strategies (can be customized for testing)
 */
class JsonParser(
    strategies: List<ComponentParserStrategyPort> = defaultStrategies()
) {
    
    private val facade = JsonParserFacade(strategies)
    
    /**
     * Parses a JSON string into a ComponentDescriptor hierarchy.
     *
     * This is a suspend function that performs parsing on the IO dispatcher to avoid
     * blocking the main thread. The result is wrapped in a Result type for functional
     * error handling.
     *
     * @param jsonString The JSON string to parse
     * @return Result containing the parsed ComponentDescriptor on success, or an exception on failure
     */
    suspend fun parse(jsonString: String): Result<ComponentDescriptor> {
        return facade.parse(jsonString)
    }
    
    companion object {
        /**
         * Provides the default list of parsing strategies.
         *
         * This can be overridden for testing or to add custom strategies.
         */
        fun defaultStrategies(): List<ComponentParserStrategyPort> = 
            JsonParserFacade.defaultStrategies()
    }
}

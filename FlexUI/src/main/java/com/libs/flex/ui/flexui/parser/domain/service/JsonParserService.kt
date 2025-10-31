package com.libs.flex.ui.flexui.parser.domain.service

import com.libs.flex.ui.flexui.exceptions.JsonParseException
import com.libs.flex.ui.flexui.exceptions.MissingPropertyException
import com.libs.flex.ui.flexui.model.ComponentDescriptor
import com.libs.flex.ui.flexui.parser.domain.ports.ComponentParserStrategyPort
import com.libs.flex.ui.flexui.parser.domain.ports.ParseComponentPort
import com.libs.flex.ui.flexui.parser.infrastructure.mapper.ComponentMapper
import com.libs.flex.ui.flexui.parser.infrastructure.util.getRequiredString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * Domain Service that implements the parsing logic.
 *
 * This service orchestrates the parsing process by:
 * 1. Converting JSON string to JsonObject
 * 2. Delegating to appropriate parsing strategies
 * 3. Handling errors and wrapping results
 *
 * In Hexagonal Architecture (within parser module):
 * - This is part of the DOMAIN layer
 * - Contains core parsing business logic
 * - Depends only on ports (interfaces)
 * - Independent of JSON library specifics
 *
 * @property strategies List of parsing strategies
 */
class JsonParserService(
    private val strategies: List<ComponentParserStrategyPort>
) : ParseComponentPort {
    
    /**
     * JSON configuration for parsing with lenient settings.
     */
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }
    
    /**
     * Parses a JSON string into a ComponentDescriptor hierarchy.
     *
     * @param jsonString The JSON string to parse
     * @return Result containing the parsed ComponentDescriptor on success, or an exception on failure
     */
    override suspend fun parse(jsonString: String): Result<ComponentDescriptor> = 
        withContext(Dispatchers.IO) {
            try {
                val jsonElement = json.parseToJsonElement(jsonString)
                val descriptor = parseComponent(jsonElement.jsonObject)
                Result.success(descriptor)
            } catch (e: JsonParseException) {
                Result.failure(e)
            } catch (e: MissingPropertyException) {
                Result.failure(e)
            } catch (e: Exception) {
                Result.failure(JsonParseException("Failed to parse JSON: ${e.message}", e))
            }
        }
    
    /**
     * Recursively parses a JSON object into a ComponentDescriptor.
     *
     * This method uses the Strategy Pattern to delegate parsing to the
     * appropriate strategy based on component type.
     *
     * @param jsonObject The JSON object representing a component
     * @return The parsed ComponentDescriptor
     * @throws JsonParseException if no strategy is found for the component type
     */
    private fun parseComponent(jsonObject: JsonObject): ComponentDescriptor {
        val typeString = jsonObject.getRequiredString("type", "unknown")
        val componentType = ComponentMapper.mapType(typeString)
        
        val strategy = strategies.firstOrNull { it.canParse(componentType) }
            ?: throw JsonParseException("No parser strategy found for component type: $componentType")
        
        return strategy.parse(jsonObject, componentType, ::parseComponent)
    }
}

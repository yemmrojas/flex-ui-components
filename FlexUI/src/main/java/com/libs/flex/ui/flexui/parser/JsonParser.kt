package com.libs.flex.ui.flexui.parser

import com.libs.flex.ui.flexui.exceptions.JsonParseException
import com.libs.flex.ui.flexui.exceptions.MissingPropertyException
import com.libs.flex.ui.flexui.model.AtomicDescriptor
import com.libs.flex.ui.flexui.model.ComponentDescriptor
import com.libs.flex.ui.flexui.model.ComponentType
import com.libs.flex.ui.flexui.model.LayoutDescriptor
import com.libs.flex.ui.flexui.model.PaddingValues
import com.libs.flex.ui.flexui.model.SelectOption
import com.libs.flex.ui.flexui.model.StyleProperties
import com.libs.flex.ui.flexui.model.ValidationRules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.float
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull

/**
 * Parser for converting JSON strings into ComponentDescriptor hierarchies.
 *
 * This class handles the interpretation of JSON configuration into strongly-typed
 * component descriptors that can be rendered by the Visual Components Module.
 */
class JsonParser {
    
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
     * This is a suspend function that performs parsing on the IO dispatcher to avoid
     * blocking the main thread. The result is wrapped in a Result type for functional
     * error handling.
     *
     * @param jsonString The JSON string to parse
     * @return Result containing the parsed ComponentDescriptor on success, or an exception on failure
     */
    suspend fun parse(jsonString: String): Result<ComponentDescriptor> = withContext(Dispatchers.IO) {
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
     * This method determines whether the component is a layout container or atomic component
     * and delegates to the appropriate parsing method.
     *
     * @param jsonObject The JSON object representing a component
     * @return The parsed ComponentDescriptor (either LayoutDescriptor or AtomicDescriptor)
     * @throws MissingPropertyException if required properties are missing
     */
    private fun parseComponent(jsonObject: JsonObject): ComponentDescriptor {
        val typeString = jsonObject["type"]?.jsonPrimitive?.content
            ?: throw MissingPropertyException("type", "unknown")
        
        val componentType = ComponentMapper.mapType(typeString)
        
        return when {
            ComponentMapper.isLayoutType(componentType) -> parseLayout(jsonObject, componentType)
            else -> parseAtomic(jsonObject, componentType)
        }
    }
    
    /**
     * Parses a JSON object into a LayoutDescriptor.
     *
     * Layout descriptors represent container components that can have children.
     * This method extracts all layout-specific properties and recursively parses child components.
     *
     * @param jsonObject The JSON object representing a layout container
     * @param type The ComponentType of the layout
     * @return The parsed LayoutDescriptor
     * @throws MissingPropertyException if the id property is missing
     */
    private fun parseLayout(jsonObject: JsonObject, type: ComponentType): LayoutDescriptor {
        val id = jsonObject["id"]?.jsonPrimitive?.content
            ?: throw MissingPropertyException("id", type.name)
        
        val style = jsonObject["style"]?.jsonObject?.let { parseStyleProperties(it) }
        
        val children = jsonObject["children"]?.jsonArray?.map { childElement ->
            parseComponent(childElement.jsonObject)
        } ?: emptyList()
        
        val arrangement = jsonObject["arrangement"]?.jsonPrimitive?.content
        val alignment = jsonObject["alignment"]?.jsonPrimitive?.content
        val scrollDirection = jsonObject["scrollDirection"]?.jsonPrimitive?.content
        val fabIcon = jsonObject["fabIcon"]?.jsonPrimitive?.content
        val fabPosition = jsonObject["fabPosition"]?.jsonPrimitive?.content
        val actionId = jsonObject["actionId"]?.jsonPrimitive?.content
        
        val items = jsonObject["items"]?.jsonArray
        val itemTemplate = jsonObject["itemTemplate"]?.jsonObject?.let { parseComponent(it) }
        
        val autoPlay = jsonObject["autoPlay"]?.jsonPrimitive?.booleanOrNull
        val autoPlayInterval = jsonObject["autoPlayInterval"]?.jsonPrimitive?.longOrNull
        
        return LayoutDescriptor(
            id = id,
            type = type,
            style = style,
            children = children,
            arrangement = arrangement,
            alignment = alignment,
            scrollDirection = scrollDirection,
            fabIcon = fabIcon,
            fabPosition = fabPosition,
            actionId = actionId,
            items = items,
            itemTemplate = itemTemplate,
            autoPlay = autoPlay,
            autoPlayInterval = autoPlayInterval
        )
    }
    
    /**
     * Parses a JSON object into an AtomicDescriptor.
     *
     * Atomic descriptors represent leaf components that don't have children.
     * This method extracts all atomic component properties.
     *
     * @param jsonObject The JSON object representing an atomic component
     * @param type The ComponentType of the atomic component
     * @return The parsed AtomicDescriptor
     * @throws MissingPropertyException if the id property is missing
     */
    private fun parseAtomic(jsonObject: JsonObject, type: ComponentType): AtomicDescriptor {
        val id = jsonObject["id"]?.jsonPrimitive?.content
            ?: throw MissingPropertyException("id", type.name)
        
        val style = jsonObject["style"]?.jsonObject?.let { parseStyleProperties(it) }
        
        val text = jsonObject["text"]?.jsonPrimitive?.content
        val placeholder = jsonObject["placeholder"]?.jsonPrimitive?.content
        val value = jsonObject["value"]
        val inputStyle = jsonObject["inputStyle"]?.jsonPrimitive?.content
        val textStyle = jsonObject["textStyle"]?.jsonPrimitive?.content
        val fontSize = jsonObject["fontSize"]?.jsonPrimitive?.intOrNull
        val color = jsonObject["color"]?.jsonPrimitive?.content
        val maxLines = jsonObject["maxLines"]?.jsonPrimitive?.intOrNull
        val label = jsonObject["label"]?.jsonPrimitive?.content
        val enabled = jsonObject["enabled"]?.jsonPrimitive?.booleanOrNull ?: true
        val checked = jsonObject["checked"]?.jsonPrimitive?.booleanOrNull
        
        val options = jsonObject["options"]?.jsonArray?.map { optionElement ->
            parseSelectOption(optionElement.jsonObject)
        }
        
        val min = jsonObject["min"]?.jsonPrimitive?.floatOrNull
        val max = jsonObject["max"]?.jsonPrimitive?.floatOrNull
        val buttonStyle = jsonObject["buttonStyle"]?.jsonPrimitive?.content
        val imageUrl = jsonObject["imageUrl"]?.jsonPrimitive?.content
        val contentScale = jsonObject["contentScale"]?.jsonPrimitive?.content
        val loaderStyle = jsonObject["loaderStyle"]?.jsonPrimitive?.content
        val size = jsonObject["size"]?.jsonPrimitive?.intOrNull
        val actionId = jsonObject["actionId"]?.jsonPrimitive?.content
        
        val validation = jsonObject["validation"]?.jsonObject?.let { parseValidationRules(it) }
        
        return AtomicDescriptor(
            id = id,
            type = type,
            style = style,
            text = text,
            placeholder = placeholder,
            value = value,
            inputStyle = inputStyle,
            textStyle = textStyle,
            fontSize = fontSize,
            color = color,
            maxLines = maxLines,
            label = label,
            enabled = enabled,
            checked = checked,
            options = options,
            min = min,
            max = max,
            buttonStyle = buttonStyle,
            imageUrl = imageUrl,
            contentScale = contentScale,
            loaderStyle = loaderStyle,
            size = size,
            actionId = actionId,
            validation = validation
        )
    }
    
    /**
     * Parses a JSON object into StyleProperties.
     *
     * @param jsonObject The JSON object containing style properties
     * @return The parsed StyleProperties
     */
    private fun parseStyleProperties(jsonObject: JsonObject): StyleProperties {
        val padding = jsonObject["padding"]?.jsonObject?.let { parsePaddingValues(it) }
        val margin = jsonObject["margin"]?.jsonObject?.let { parsePaddingValues(it) }
        val backgroundColor = jsonObject["backgroundColor"]?.jsonPrimitive?.content
        val borderRadius = jsonObject["borderRadius"]?.jsonPrimitive?.intOrNull
        val elevation = jsonObject["elevation"]?.jsonPrimitive?.intOrNull
        val width = jsonObject["width"]?.jsonPrimitive?.content
        val height = jsonObject["height"]?.jsonPrimitive?.content
        
        return StyleProperties(
            padding = padding,
            margin = margin,
            backgroundColor = backgroundColor,
            borderRadius = borderRadius,
            elevation = elevation,
            width = width,
            height = height
        )
    }
    
    /**
     * Parses a JSON object into PaddingValues.
     *
     * @param jsonObject The JSON object containing padding values
     * @return The parsed PaddingValues
     */
    private fun parsePaddingValues(jsonObject: JsonObject): PaddingValues {
        val start = jsonObject["start"]?.jsonPrimitive?.intOrNull ?: 0
        val top = jsonObject["top"]?.jsonPrimitive?.intOrNull ?: 0
        val end = jsonObject["end"]?.jsonPrimitive?.intOrNull ?: 0
        val bottom = jsonObject["bottom"]?.jsonPrimitive?.intOrNull ?: 0
        
        return PaddingValues(
            start = start,
            top = top,
            end = end,
            bottom = bottom
        )
    }
    
    /**
     * Parses a JSON object into a SelectOption.
     *
     * @param jsonObject The JSON object containing select option data
     * @return The parsed SelectOption
     */
    private fun parseSelectOption(jsonObject: JsonObject): SelectOption {
        val label = jsonObject["label"]?.jsonPrimitive?.content ?: ""
        val value = jsonObject["value"]?.jsonPrimitive?.content ?: ""
        
        return SelectOption(
            label = label,
            value = value
        )
    }
    
    /**
     * Parses a JSON object into ValidationRules.
     *
     * @param jsonObject The JSON object containing validation rules
     * @return The parsed ValidationRules
     */
    private fun parseValidationRules(jsonObject: JsonObject): ValidationRules {
        val required = jsonObject["required"]?.jsonPrimitive?.booleanOrNull ?: false
        val minLength = jsonObject["minLength"]?.jsonPrimitive?.intOrNull
        val maxLength = jsonObject["maxLength"]?.jsonPrimitive?.intOrNull
        val pattern = jsonObject["pattern"]?.jsonPrimitive?.content
        val errorMessage = jsonObject["errorMessage"]?.jsonPrimitive?.content
        
        return ValidationRules(
            required = required,
            minLength = minLength,
            maxLength = maxLength,
            pattern = pattern,
            errorMessage = errorMessage
        )
    }
}

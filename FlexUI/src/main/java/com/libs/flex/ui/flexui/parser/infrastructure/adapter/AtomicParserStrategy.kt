package com.libs.flex.ui.flexui.parser.infrastructure.adapter

import com.libs.flex.ui.flexui.model.AtomicDescriptor
import com.libs.flex.ui.flexui.model.ComponentDescriptor
import com.libs.flex.ui.flexui.model.ComponentType
import com.libs.flex.ui.flexui.parser.domain.ports.ComponentParserStrategyPort
import com.libs.flex.ui.flexui.parser.infrastructure.mapper.ComponentMapper
import com.libs.flex.ui.flexui.parser.infrastructure.property.SelectOptionParser
import com.libs.flex.ui.flexui.parser.infrastructure.property.StylePropertiesParser
import com.libs.flex.ui.flexui.parser.infrastructure.property.ValidationRulesParser
import com.libs.flex.ui.flexui.parser.infrastructure.util.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * Infrastructure Adapter for parsing AtomicDescriptor components.
 *
 * This adapter implements the ComponentParserStrategyPort for atomic (leaf) components.
 *
 * In Hexagonal Architecture (within parser module):
 * - This is a SECONDARY/DRIVEN adapter
 * - Implements the ComponentParserStrategyPort
 * - Contains JSON-specific parsing logic
 * - Can be replaced with other implementations
 */
class AtomicParserStrategy : ComponentParserStrategyPort {
    
    override fun canParse(type: ComponentType): Boolean {
        return !ComponentMapper.isLayoutType(type)
    }
    
    override fun parse(
        jsonObject: JsonObject,
        type: ComponentType,
        recursiveParser: (JsonObject) -> ComponentDescriptor
    ): ComponentDescriptor {
        val id = jsonObject.getRequiredString("id", type.name)
        val style = jsonObject.getOptionalObject("style")?.let { StylePropertiesParser.parse(it) }
        
        return AtomicDescriptor(
            id = id,
            type = type,
            style = style,
            text = jsonObject.getOptionalString("text"),
            placeholder = jsonObject.getOptionalString("placeholder"),
            value = jsonObject.getOptionalElement("value"),
            inputStyle = jsonObject.getOptionalString("inputStyle"),
            textStyle = jsonObject.getOptionalString("textStyle"),
            fontSize = jsonObject.getOptionalInt("fontSize"),
            color = jsonObject.getOptionalString("color"),
            maxLines = jsonObject.getOptionalInt("maxLines"),
            label = jsonObject.getOptionalString("label"),
            enabled = jsonObject.getOptionalBoolean("enabled", true),
            checked = jsonObject.getOptionalBooleanOrNull("checked"),
            options = parseOptions(jsonObject),
            min = jsonObject.getOptionalFloat("min"),
            max = jsonObject.getOptionalFloat("max"),
            buttonStyle = jsonObject.getOptionalString("buttonStyle"),
            imageUrl = jsonObject.getOptionalString("imageUrl"),
            contentScale = jsonObject.getOptionalString("contentScale"),
            loaderStyle = jsonObject.getOptionalString("loaderStyle"),
            size = jsonObject.getOptionalInt("size"),
            actionId = jsonObject.getOptionalString("actionId"),
            validation = parseValidation(jsonObject)
        )
    }
    
    /**
     * Parses the options array for select components.
     */
    private fun parseOptions(jsonObject: JsonObject) =
        jsonObject.getOptionalArray("options")?.map { optionElement ->
            SelectOptionParser.parse(optionElement.jsonObject)
        }
    
    /**
     * Parses validation rules for input components.
     */
    private fun parseValidation(jsonObject: JsonObject) =
        jsonObject.getOptionalObject("validation")?.let { validationObject ->
            ValidationRulesParser.parse(validationObject)
        }
}

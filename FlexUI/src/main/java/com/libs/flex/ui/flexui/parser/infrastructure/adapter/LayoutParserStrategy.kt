package com.libs.flex.ui.flexui.parser.infrastructure.adapter

import com.libs.flex.ui.flexui.model.ComponentDescriptor
import com.libs.flex.ui.flexui.model.ComponentType
import com.libs.flex.ui.flexui.model.LayoutDescriptor
import com.libs.flex.ui.flexui.parser.domain.ports.ComponentParserStrategyPort
import com.libs.flex.ui.flexui.parser.domain.ports.ComponentTypeMapperPort
import com.libs.flex.ui.flexui.parser.infrastructure.property.StylePropertiesParser
import com.libs.flex.ui.flexui.parser.infrastructure.util.*
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import javax.inject.Inject

/**
 * Infrastructure Adapter for parsing LayoutDescriptor components.
 *
 * This adapter implements the ComponentParserStrategyPort for layout containers.
 *
 * In Hexagonal Architecture (within parser module):
 * - This is a SECONDARY/DRIVEN adapter
 * - Implements the ComponentParserStrategyPort
 * - Contains JSON-specific parsing logic
 * - Can be replaced with other implementations
 */
class LayoutParserStrategy @Inject constructor(
    private val componentTypeMapper: ComponentTypeMapperPort
) : ComponentParserStrategyPort {
    
    override fun canParse(type: ComponentType): Boolean {
        return componentTypeMapper.isLayoutType(type)
    }
    
    override fun parse(
        jsonObject: JsonObject,
        type: ComponentType,
        recursiveParser: (JsonObject) -> ComponentDescriptor
    ): ComponentDescriptor {
        val id = jsonObject.getRequiredString("id", type.name)
        val style = jsonObject.getOptionalObject("style")?.let { StylePropertiesParser.parse(it) }
        
        val children = parseChildren(jsonObject, recursiveParser)
        val itemTemplate = parseItemTemplate(jsonObject, recursiveParser)
        
        return LayoutDescriptor(
            id = id,
            type = type,
            style = style,
            children = children,
            arrangement = jsonObject.getOptionalString("arrangement"),
            alignment = jsonObject.getOptionalString("alignment"),
            scrollDirection = jsonObject.getOptionalString("scrollDirection"),
            fabIcon = jsonObject.getOptionalString("fabIcon"),
            fabPosition = jsonObject.getOptionalString("fabPosition"),
            actionId = jsonObject.getOptionalString("actionId"),
            items = jsonObject.getOptionalArray("items"),
            itemTemplate = itemTemplate,
            autoPlay = jsonObject.getOptionalBooleanOrNull("autoPlay"),
            autoPlayInterval = jsonObject.getOptionalLong("autoPlayInterval")
        )
    }
    
    /**
     * Parses the children array recursively.
     */
    private fun parseChildren(
        jsonObject: JsonObject,
        recursiveParser: (JsonObject) -> ComponentDescriptor
    ): List<ComponentDescriptor> {
        return jsonObject.getOptionalArray("children")?.map { childElement ->
            recursiveParser(childElement.jsonObject)
        } ?: emptyList()
    }
    
    /**
     * Parses the item template for list/slider components.
     */
    private fun parseItemTemplate(
        jsonObject: JsonObject,
        recursiveParser: (JsonObject) -> ComponentDescriptor
    ): ComponentDescriptor? {
        return jsonObject.getOptionalObject("itemTemplate")?.let { templateObject ->
            recursiveParser(templateObject)
        }
    }
}

package com.libs.flex.ui.flexui.parser.infrastructure.property

import com.libs.flex.ui.flexui.model.PaddingValues
import com.libs.flex.ui.flexui.model.SelectOption
import com.libs.flex.ui.flexui.model.StyleProperties
import com.libs.flex.ui.flexui.model.ValidationRules
import com.libs.flex.ui.flexui.parser.infrastructure.util.*
import kotlinx.serialization.json.JsonObject

/**
 * Interface for parsing specific property types from JSON.
 *
 * This follows the Single Responsibility Principle by separating
 * parsing logic for different property types.
 */
interface PropertyParser<T> {
    /**
     * Parses a JsonObject into a specific property type.
     *
     * @param jsonObject The JSON object to parse
     * @return The parsed property object
     */
    fun parse(jsonObject: JsonObject): T
}

/**
 * Parser for StyleProperties.
 */
object StylePropertiesParser : PropertyParser<StyleProperties> {
    override fun parse(jsonObject: JsonObject): StyleProperties {
        return StyleProperties(
            padding = jsonObject.getOptionalObject("padding")?.let { PaddingValuesParser.parse(it) },
            margin = jsonObject.getOptionalObject("margin")?.let { PaddingValuesParser.parse(it) },
            backgroundColor = jsonObject.getOptionalString("backgroundColor"),
            borderRadius = jsonObject.getOptionalInt("borderRadius"),
            elevation = jsonObject.getOptionalInt("elevation"),
            width = jsonObject.getOptionalString("width"),
            height = jsonObject.getOptionalString("height")
        )
    }
}

/**
 * Parser for PaddingValues.
 */
object PaddingValuesParser : PropertyParser<PaddingValues> {
    override fun parse(jsonObject: JsonObject): PaddingValues {
        return PaddingValues(
            start = jsonObject.getOptionalInt("start") ?: 0,
            top = jsonObject.getOptionalInt("top") ?: 0,
            end = jsonObject.getOptionalInt("end") ?: 0,
            bottom = jsonObject.getOptionalInt("bottom") ?: 0
        )
    }
}

/**
 * Parser for SelectOption.
 */
object SelectOptionParser : PropertyParser<SelectOption> {
    override fun parse(jsonObject: JsonObject): SelectOption {
        return SelectOption(
            label = jsonObject.getOptionalString("label") ?: "",
            value = jsonObject.getOptionalString("value") ?: ""
        )
    }
}

/**
 * Parser for ValidationRules.
 */
object ValidationRulesParser : PropertyParser<ValidationRules> {
    override fun parse(jsonObject: JsonObject): ValidationRules {
        return ValidationRules(
            required = jsonObject.getOptionalBoolean("required", false),
            minLength = jsonObject.getOptionalInt("minLength"),
            maxLength = jsonObject.getOptionalInt("maxLength"),
            pattern = jsonObject.getOptionalString("pattern"),
            errorMessage = jsonObject.getOptionalString("errorMessage")
        )
    }
}

package com.libs.flex.ui.parser.internal.reader

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.value.Dimension
import com.libs.flex.ui.core.domain.value.PaddingValues
import com.libs.flex.ui.core.domain.value.StyleProperties
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.ParserWarning
import com.libs.flex.ui.parser.internal.model.CommonProperties
import com.libs.flex.ui.parser.internal.util.IdSanitizer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull

private const val KEY_ID = "id"
private const val KEY_STYLE = "style"
private const val KEY_STYLES = "styles"
private const val KEY_PADDING = "padding"
private const val KEY_MARGIN = "margin"

/**
 * Implementación predeterminada e inmutable de [StyleReader].
 */
internal class DefaultStyleReader : StyleReader {

    override fun readCommonProperties(
        json: JsonObject,
        path: String,
        policy: ErrorPolicy,
        onWarn: (ParserWarning) -> Unit
    ): Either<Nel<ParserError>, CommonProperties> {
        val rawId = (json[KEY_ID] as? JsonPrimitive)?.contentOrNull
        val componentId = IdSanitizer.sanitizeToComponentId(rawId, fallbackPrefix = "auto_$path")

        val styleElement = json[KEY_STYLE] ?: json[KEY_STYLES]
        val styleProperties = if (styleElement is JsonObject) {
            val styleReader = JsonPropertyReader(styleElement, "$path.$KEY_STYLE", policy, onWarn)
            readStyleProperties(styleReader)
        } else {
            StyleProperties()
        }

        return Either.Right(CommonProperties(id = componentId, style = styleProperties))
    }

    override fun readStyleProperties(styleReader: PropertyReader): StyleProperties {
        return StyleProperties(
            padding = readPaddingValues(styleReader, KEY_PADDING),
            margin = readPaddingValues(styleReader, KEY_MARGIN),
            backgroundColor = styleReader.optionalColor("backgroundColor", "bgColor").getOrNull(),
            borderRadius = styleReader.optionalDimensionDp("borderRadius", "cornerRadius").getOrNull(),
            elevation = styleReader.optionalDimensionDp("elevation").getOrNull(),
            width = styleReader.optionalDimension("width").getOrNull(),
            height = styleReader.optionalDimension("height").getOrNull(),
            weight = styleReader.optionalFloat("weight").getOrNull()
        )
    }

    private fun readPaddingValues(reader: PropertyReader, key: String): PaddingValues? {
        val jsonReader = reader as? JsonPropertyReader ?: return null
        val element = jsonReader.json[key] ?: return null

        return when (element) {
            is JsonPrimitive -> {
                val floatVal = element.floatOrNull ?: element.contentOrNull?.toFloatOrNull()
                floatVal?.let { Dimension.Dp.of(it).getOrNull() }?.let { dp ->
                    PaddingValues(start = dp, top = dp, end = dp, bottom = dp)
                }
            }
            is JsonObject -> {
                val pReader = JsonPropertyReader(
                    element,
                    "${reader.path}.$key", jsonReader.policy, jsonReader.onWarn
                )
                PaddingValues(
                    start = pReader.optionalDimensionDp("start", "left").getOrNull(),
                    top = pReader.optionalDimensionDp("top").getOrNull(),
                    end = pReader.optionalDimensionDp("end", "right").getOrNull(),
                    bottom = pReader.optionalDimensionDp("bottom").getOrNull()
                )
            }
            else -> null
        }
    }
}

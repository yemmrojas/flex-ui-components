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
        val styleResult: Either<Nel<ParserError>, StyleProperties> = if (styleElement is JsonObject) {
            val styleReader = JsonPropertyReader(styleElement, "$path.$KEY_STYLE", policy, onWarn)
            readStyleProperties(styleReader)
        } else {
            Either.Right(StyleProperties())
        }

        return styleResult.map { styleProperties ->
            CommonProperties(id = componentId, style = styleProperties)
        }
    }

    override fun readStyleProperties(styleReader: PropertyReader): Either<Nel<ParserError>, StyleProperties> {
        val errors = mutableListOf<ParserError>()

        fun <T> Either<Nel<ParserError>, T>.extract(): T? = when (this) {
            is Either.Left -> {
                errors.addAll(value.toList())
                null
            }

            is Either.Right -> value
        }

        val padding = readPaddingValues(styleReader, KEY_PADDING).extract()
        val margin = readPaddingValues(styleReader, KEY_MARGIN).extract()
        val backgroundColor = styleReader.optionalColor("backgroundColor", "bgColor").extract()
        val borderRadius = styleReader.optionalDimensionDp("borderRadius", "cornerRadius").extract()
        val elevation = styleReader.optionalDimensionDp("elevation").extract()
        val width = styleReader.optionalDimension("width").extract()
        val height = styleReader.optionalDimension("height").extract()
        val weight = styleReader.optionalFloat("weight").extract()

        return if (errors.isEmpty()) {
            Either.Right(
                StyleProperties(
                    padding = padding,
                    margin = margin,
                    backgroundColor = backgroundColor,
                    borderRadius = borderRadius,
                    elevation = elevation,
                    width = width,
                    height = height,
                    weight = weight
                )
            )
        } else {
            Either.Left(Nel.fromListOrNull(errors)!!)
        }
    }

    private fun readPaddingValues(
        reader: PropertyReader,
        key: String
    ): Either<Nel<ParserError>, PaddingValues?> {
        val jsonReader = reader as? JsonPropertyReader ?: return Either.Right(null)
        val element = jsonReader.json[key] ?: return Either.Right(null)

        return when (element) {
            is JsonPrimitive -> {
                val floatVal = element.floatOrNull ?: element.contentOrNull?.toFloatOrNull()
                if (floatVal == null) {
                    val error = ParserError.InvalidPropertyValue(
                        path = "${reader.path}.$key",
                        property = key,
                        reason = "Expected numeric padding value"
                    )
                    jsonReader.policy.handleOptionalError("${reader.path}.$key", key, error, null, jsonReader.onWarn)
                } else {
                    Dimension.Dp.of(floatVal).fold(
                        onLeft = { domainError ->
                            val error = ParserError.InvalidPropertyValue(
                                path = "${reader.path}.$key",
                                property = key,
                                reason = domainError.reason,
                                domainError = domainError
                            )
                            jsonReader.policy.handleOptionalError("${reader.path}.$key", key, error, null, jsonReader.onWarn)
                        },
                        onRight = { dp ->
                            Either.Right(PaddingValues(start = dp, top = dp, end = dp, bottom = dp))
                        }
                    )
                }
            }

            is JsonObject -> {
                val pReader = JsonPropertyReader(
                    element,
                    "${reader.path}.$key",
                    jsonReader.policy,
                    jsonReader.onWarn
                )
                val errors = mutableListOf<ParserError>()

                fun <T> Either<Nel<ParserError>, T>.extract(): T? = when (this) {
                    is Either.Left -> {
                        errors.addAll(value.toList())
                        null
                    }

                    is Either.Right -> value
                }

                val start = pReader.optionalDimensionDp("start", "left").extract()
                val top = pReader.optionalDimensionDp("top").extract()
                val end = pReader.optionalDimensionDp("end", "right").extract()
                val bottom = pReader.optionalDimensionDp("bottom").extract()

                if (errors.isEmpty()) {
                    Either.Right(PaddingValues(start = start, top = top, end = end, bottom = bottom))
                } else {
                    Either.Left(Nel.fromListOrNull(errors)!!)
                }
            }

            else -> {
                val error = ParserError.InvalidPropertyValue(
                    path = "${reader.path}.$key",
                    property = key,
                    reason = "Expected number or object for padding"
                )
                jsonReader.policy.handleOptionalError(
                    "${reader.path}.$key",
                    key, error,
                    null,
                    jsonReader.onWarn
                )
            }
        }
    }
}

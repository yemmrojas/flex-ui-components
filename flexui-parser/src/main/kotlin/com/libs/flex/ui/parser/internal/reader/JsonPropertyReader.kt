package com.libs.flex.ui.parser.internal.reader

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.value.Dimension
import com.libs.flex.ui.core.domain.value.HexColor
import com.libs.flex.ui.core.domain.value.PaddingValues
import com.libs.flex.ui.core.domain.value.StyleProperties
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.ParserWarning
import com.libs.flex.ui.parser.internal.model.CommonProperties
import com.libs.flex.ui.parser.internal.util.IdSanitizer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull

/**
 * Implementación de [PropertyReader] que opera directamente sobre un [JsonObject] de `kotlinx.serialization`.
 */
internal class JsonPropertyReader(
    val json: JsonObject,
    override val path: String,
    val policy: ErrorPolicy,
    val onWarn: (ParserWarning) -> Unit
) : PropertyReader {

    private fun findElement(primaryKey: String, vararg aliases: String): Pair<String, JsonElement>? {
        json[primaryKey]?.let { return Pair(primaryKey, it) }
        for (alias in aliases) {
            json[alias]?.let { return Pair(alias, it) }
        }
        return null
    }

    override fun requiredString(primaryKey: String, vararg aliases: String): Either<Nel<ParserError>, String> {
        val entry = findElement(primaryKey, *aliases)
            ?: return Either.Left(Nel.of(ParserError.MissingRequiredProperty(path, primaryKey)))

        val element = entry.second
        if (element !is JsonPrimitive) {
            return Either.Left(
                Nel.of(
                    ParserError.InvalidPropertyValue(
                        path = path,
                        property = entry.first,
                        reason = "Expected string primitive, but got ${element::class.simpleName}"
                    )
                )
            )
        }

        val content = element.contentOrNull
            ?: return Either.Left(Nel.of(ParserError.MissingRequiredProperty(path, entry.first)))

        return Either.Right(content)
    }

    override fun optionalString(
        primaryKey: String,
        vararg aliases: String,
        default: String?
    ): Either<Nel<ParserError>, String?> {
        val entry = findElement(primaryKey, *aliases) ?: return Either.Right(default)
        val element = entry.second

        if (element !is JsonPrimitive || element.contentOrNull == null) {
            val error = ParserError.InvalidPropertyValue(
                path = path,
                property = entry.first,
                reason = "Expected string primitive"
            )
            return policy.handleOptionalError(path, entry.first, error, default, onWarn)
        }

        return Either.Right(element.content)
    }

    override fun optionalBoolean(
        primaryKey: String,
        vararg aliases: String,
        default: Boolean
    ): Either<Nel<ParserError>, Boolean> {
        val entry = findElement(primaryKey, *aliases) ?: return Either.Right(default)
        val element = entry.second

        if (element !is JsonPrimitive) {
            val error = ParserError.InvalidPropertyValue(path, entry.first, "Expected boolean primitive")
            return policy.handleOptionalError(path, entry.first, error, default, onWarn).map { it ?: default }
        }

        val boolValue = element.booleanOrNull ?: element.contentOrNull?.toBooleanStrictOrNull()
        if (boolValue == null) {
            val error = ParserError.InvalidPropertyValue(path, entry.first, "Cannot convert '${element.contentOrNull}' to boolean")
            return policy.handleOptionalError(path, entry.first, error, default, onWarn).map { it ?: default }
        }

        return Either.Right(boolValue)
    }

    override fun optionalInt(
        primaryKey: String,
        vararg aliases: String,
        default: Int?
    ): Either<Nel<ParserError>, Int?> {
        val entry = findElement(primaryKey, *aliases) ?: return Either.Right(default)
        val element = entry.second

        if (element !is JsonPrimitive) {
            val error = ParserError.InvalidPropertyValue(path, entry.first, "Expected number primitive")
            return policy.handleOptionalError(path, entry.first, error, default, onWarn)
        }

        val intValue = element.intOrNull ?: element.contentOrNull?.toIntOrNull()
        if (intValue == null) {
            val error = ParserError.InvalidPropertyValue(path, entry.first, "Cannot convert '${element.contentOrNull}' to int")
            return policy.handleOptionalError(path, entry.first, error, default, onWarn)
        }

        return Either.Right(intValue)
    }

    override fun optionalFloat(
        primaryKey: String,
        vararg aliases: String,
        default: Float?
    ): Either<Nel<ParserError>, Float?> {
        val entry = findElement(primaryKey, *aliases) ?: return Either.Right(default)
        val element = entry.second

        if (element !is JsonPrimitive) {
            val error = ParserError.InvalidPropertyValue(path, entry.first, "Expected float primitive")
            return policy.handleOptionalError(path, entry.first, error, default, onWarn)
        }

        val floatValue = element.floatOrNull ?: element.contentOrNull?.toFloatOrNull()
        if (floatValue == null || !floatValue.isFinite()) {
            val error = ParserError.InvalidPropertyValue(path, entry.first, "Cannot convert '${element.contentOrNull}' to finite float")
            return policy.handleOptionalError(path, entry.first, error, default, onWarn)
        }

        return Either.Right(floatValue)
    }

    override fun optionalColor(
        primaryKey: String,
        vararg aliases: String,
        default: HexColor?
    ): Either<Nel<ParserError>, HexColor?> {
        val entry = findElement(primaryKey, *aliases) ?: return Either.Right(default)
        val raw = entry.second.let { if (it is JsonPrimitive) it.contentOrNull else null }

        if (raw == null) {
            val error = ParserError.InvalidPropertyValue(path, entry.first, "Expected hex color string")
            return policy.handleOptionalError(path, entry.first, error, default, onWarn)
        }

        return HexColor.of(raw).fold(
            onLeft = { domainError ->
                val error = ParserError.InvalidPropertyValue(
                    path = path,
                    property = entry.first,
                    reason = "Invalid hex color format: '$raw'",
                    domainError = domainError
                )
                policy.handleOptionalError(path, entry.first, error, default, onWarn)
            },
            onRight = { Either.Right(it) }
        )
    }

    override fun optionalDimensionDp(
        primaryKey: String,
        vararg aliases: String,
        default: Dimension.Dp?
    ): Either<Nel<ParserError>, Dimension.Dp?> {
        val entry = findElement(primaryKey, *aliases) ?: return Either.Right(default)
        val rawFloat = entry.second.let { element ->
            if (element is JsonPrimitive) element.floatOrNull ?: element.contentOrNull?.toFloatOrNull() else null
        }

        if (rawFloat == null) {
            val error = ParserError.InvalidPropertyValue(path, entry.first, "Expected positive numeric dp value")
            return policy.handleOptionalError(path, entry.first, error, default, onWarn)
        }

        return Dimension.Dp.of(rawFloat).fold(
            onLeft = { domainError ->
                val error = ParserError.InvalidPropertyValue(
                    path = path,
                    property = entry.first,
                    reason = domainError.reason,
                    domainError = domainError
                )
                policy.handleOptionalError(path, entry.first, error, default, onWarn)
            },
            onRight = { Either.Right(it) }
        )
    }

    override fun optionalDimension(
        primaryKey: String,
        vararg aliases: String,
        default: Dimension?
    ): Either<Nel<ParserError>, Dimension?> {
        val entry = findElement(primaryKey, *aliases) ?: return Either.Right(default)
        val element = entry.second

        if (element !is JsonPrimitive) {
            val error = ParserError.InvalidPropertyValue(path, entry.first, "Expected dimension specification")
            return policy.handleOptionalError(path, entry.first, error, default, onWarn)
        }

        val content = element.contentOrNull?.lowercase()
        return when (content) {
            "match_parent", "matchparent", "fill_max_size" -> Either.Right(Dimension.MatchParent)
            "wrap_content", "wrapcontent" -> Either.Right(Dimension.WrapContent)
            else -> {
                val floatVal = element.floatOrNull ?: element.contentOrNull?.toFloatOrNull()
                if (floatVal != null) {
                    Dimension.Dp.of(floatVal).fold(
                        onLeft = { domainError ->
                            val error = ParserError.InvalidPropertyValue(
                                path = path,
                                property = entry.first,
                                reason = domainError.reason,
                                domainError = domainError
                            )
                            policy.handleOptionalError(path, entry.first, error, default, onWarn)
                        },
                        onRight = { Either.Right(it) }
                    )
                } else {
                    val error = ParserError.InvalidPropertyValue(path, entry.first, "Unknown dimension value '$content'")
                    policy.handleOptionalError(path, entry.first, error, default, onWarn)
                }
            }
        }
    }

    override fun readCommonProperties(): Either<Nel<ParserError>, CommonProperties> {
        val rawId = json["id"]?.let { if (it is JsonPrimitive) it.contentOrNull else null }
        val componentId = IdSanitizer.sanitizeToComponentId(rawId, fallbackPrefix = "auto_$path")

        val styleElement = json["style"] ?: json["styles"]
        val styleReader: PropertyReader? = if (styleElement is JsonObject) {
            JsonPropertyReader(styleElement, "$path.style", policy, onWarn)
        } else {
            null
        }

        val styleProperties = if (styleReader != null) {
            readStyleProperties(styleReader)
        } else {
            StyleProperties()
        }

        return Either.Right(CommonProperties(id = componentId, style = styleProperties))
    }

    private fun readStyleProperties(reader: PropertyReader): StyleProperties {
        val padding = readPaddingValues(reader, "padding")
        val margin = readPaddingValues(reader, "margin")
        val backgroundColor = reader.optionalColor("backgroundColor", "bgColor").getOrNull()
        val borderRadius = reader.optionalDimensionDp("borderRadius", "cornerRadius").getOrNull()
        val elevation = reader.optionalDimensionDp("elevation").getOrNull()
        val width = reader.optionalDimension("width").getOrNull()
        val height = reader.optionalDimension("height").getOrNull()
        val weight = reader.optionalFloat("weight").getOrNull()

        return StyleProperties(
            padding = padding,
            margin = margin,
            backgroundColor = backgroundColor,
            borderRadius = borderRadius,
            elevation = elevation,
            width = width,
            height = height,
            weight = weight
        )
    }

    private fun readPaddingValues(reader: PropertyReader, key: String): PaddingValues? {
        val entry = (reader as? JsonPropertyReader)?.findElement(key) ?: return null
        val element = entry.second

        return when (element) {
            is JsonPrimitive -> {
                val floatVal = element.floatOrNull ?: element.contentOrNull?.toFloatOrNull()
                if (floatVal != null) {
                    Dimension.Dp.of(floatVal).fold(
                        onLeft = { null },
                        onRight = { dp -> PaddingValues(start = dp, top = dp, end = dp, bottom = dp) }
                    )
                } else null
            }
            is JsonObject -> {
                val pReader = JsonPropertyReader(element, "${reader.path}.$key", (reader as JsonPropertyReader).policy, reader.onWarn)
                val start = pReader.optionalDimensionDp("start", "left").getOrNull()
                val top = pReader.optionalDimensionDp("top").getOrNull()
                val end = pReader.optionalDimensionDp("end", "right").getOrNull()
                val bottom = pReader.optionalDimensionDp("bottom").getOrNull()
                PaddingValues(start = start, top = top, end = end, bottom = bottom)
            }
            else -> null
        }
    }
}

package com.libs.flex.ui.parser.internal.reader

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.functional.zip
import com.libs.flex.ui.core.domain.value.Dimension
import com.libs.flex.ui.core.domain.value.FontSize
import com.libs.flex.ui.core.domain.value.HexColor
import com.libs.flex.ui.core.domain.value.NumberRange
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.ParserWarning
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull

private val MATCH_PARENT_ALIASES = setOf("match_parent", "matchparent", "fill_max_size")
private val WRAP_CONTENT_ALIASES = setOf("wrap_content", "wrapcontent")
private const val KEY_RANGE = "range"

private fun String.removeSuffixIgnoreCase(suffix: String): String =
    if (endsWith(suffix, ignoreCase = true)) substring(0, length - suffix.length).trim() else this

/**
 * Implementación predeterminada e inmutable de [DomainValueReader].
 */
internal class DefaultDomainValueReader : DomainValueReader {

    override fun readColor(
        entry: Pair<String, JsonElement>?,
        path: String,
        policy: ErrorPolicy,
        onWarn: (ParserWarning) -> Unit,
        default: HexColor?
    ): Either<Nel<ParserError>, HexColor?> {
        if (entry == null) return Either.Right(default)
        val raw = (entry.second as? JsonPrimitive)?.contentOrNull

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

    override fun readDimensionDp(
        entry: Pair<String, JsonElement>?,
        path: String,
        policy: ErrorPolicy,
        onWarn: (ParserWarning) -> Unit,
        default: Dimension.Dp?
    ): Either<Nel<ParserError>, Dimension.Dp?> {
        if (entry == null) return Either.Right(default)
        val rawFloat = (entry.second as? JsonPrimitive)?.let {
            it.floatOrNull ?: it.contentOrNull?.trim()?.removeSuffixIgnoreCase("dp")?.toFloatOrNull()
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

    override fun readFontSize(
        entry: Pair<String, JsonElement>?,
        path: String,
        policy: ErrorPolicy,
        onWarn: (ParserWarning) -> Unit
    ): Either<Nel<ParserError>, FontSize?> {
        if (entry == null) return Either.Right(null)
        val rawFloat = (entry.second as? JsonPrimitive)?.let {
            it.floatOrNull ?: it.contentOrNull?.trim()?.removeSuffixIgnoreCase("sp")?.toFloatOrNull()
        }

        if (rawFloat == null) {
            val error = ParserError.InvalidPropertyValue(path, entry.first, "Expected numeric fontSize")
            return policy.handleOptionalError(path, entry.first, error, null, onWarn)
        }

        return FontSize.of(rawFloat).fold(
            onLeft = { domainError ->
                val error = ParserError.InvalidPropertyValue(
                    path = path,
                    property = entry.first,
                    reason = domainError.reason,
                    domainError = domainError
                )
                policy.handleOptionalError(path, entry.first, error, null, onWarn)
            },
            onRight = { Either.Right(it) }
        )
    }

    override fun readDimension(
        entry: Pair<String, JsonElement>?,
        path: String,
        policy: ErrorPolicy,
        onWarn: (ParserWarning) -> Unit,
        default: Dimension?
    ): Either<Nel<ParserError>, Dimension?> {
        if (entry == null) return Either.Right(default)
        val element = entry.second

        if (element !is JsonPrimitive) {
            val error = ParserError.InvalidPropertyValue(path, entry.first, "Expected dimension specification")
            return policy.handleOptionalError(path, entry.first, error, default, onWarn)
        }

        return when (val content = element.contentOrNull?.trim()?.lowercase()) {
            in MATCH_PARENT_ALIASES -> Either.Right(Dimension.MatchParent)
            in WRAP_CONTENT_ALIASES -> Either.Right(Dimension.WrapContent)
            else -> {
                val floatVal = element.floatOrNull ?: content?.removeSuffixIgnoreCase("dp")?.toFloatOrNull()
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

    override fun readNumberRange(
        json: JsonObject,
        path: String,
        policy: ErrorPolicy,
        onWarn: (ParserWarning) -> Unit,
        minKey: String,
        maxKey: String,
        stepKey: String,
        defaultMin: Double,
        defaultMax: Double,
        defaultStep: Double
    ): Either<Nel<ParserError>, NumberRange> {
        val rangeElement = json[KEY_RANGE]
        if (rangeElement != null && rangeElement !is JsonObject) {
            val error = ParserError.InvalidPropertyValue(path, KEY_RANGE, "Expected JSON object for range")
            return policy.handleOptionalError(path, KEY_RANGE, error, null, onWarn).flatMap {
                NumberRange.of(defaultMin, defaultMax, defaultStep).fold(
                    onLeft = { Either.Left(Nel.of(error)) },
                    onRight = { Either.Right(it) }
                )
            }
        }

        val targetReader = (rangeElement as? JsonObject)?.let {
            JsonPropertyReader(it, "$path.$KEY_RANGE", policy, onWarn)
        }
        val rootReader = JsonPropertyReader(json, path, policy, onWarn)

        fun readBound(key: String, default: Double): Either<Nel<ParserError>, Double> {
            return if (targetReader != null && targetReader.findElement(key) != null) {
                targetReader.optionalFloat(key, default = default.toFloat()).map { (it ?: default.toFloat()).toDouble() }
            } else {
                rootReader.optionalFloat(key, default = default.toFloat()).map { (it ?: default.toFloat()).toDouble() }
            }
        }

        val minRes = readBound(minKey, defaultMin)
        val maxRes = readBound(maxKey, defaultMax)
        val stepRes = readBound(stepKey, defaultStep)

        return minRes.zip(maxRes) { minVal, maxVal -> Pair(minVal, maxVal) }
            .zip(stepRes) { (minVal, maxVal), stepVal -> Triple(minVal, maxVal, stepVal) }
            .flatMap { (minVal, maxVal, stepVal) ->
                NumberRange.of(minVal, maxVal, stepVal).fold(
                    onLeft = { domainError ->
                        val error = ParserError.InvalidPropertyValue(
                            path = targetReader?.path ?: path,
                            property = KEY_RANGE,
                            reason = domainError.reason,
                            domainError = domainError
                        )
                        policy.handleOptionalError(targetReader?.path ?: path, KEY_RANGE, error, null, onWarn).flatMap {
                            NumberRange.of(defaultMin, defaultMax, defaultStep).fold(
                                onLeft = { Either.Left(Nel.of(error)) },
                                onRight = { Either.Right(it) }
                            )
                        }
                    },
                    onRight = { Either.Right(it) }
                )
            }
    }
}

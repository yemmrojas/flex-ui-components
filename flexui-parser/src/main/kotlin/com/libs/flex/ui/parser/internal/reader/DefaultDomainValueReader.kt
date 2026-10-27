package com.libs.flex.ui.parser.internal.reader

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
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
            it.floatOrNull ?: it.contentOrNull?.toFloatOrNull()
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
            it.floatOrNull ?: it.contentOrNull?.toFloatOrNull()
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

        return when (val content = element.contentOrNull?.lowercase()) {
            in MATCH_PARENT_ALIASES -> Either.Right(Dimension.MatchParent)
            in WRAP_CONTENT_ALIASES -> Either.Right(Dimension.WrapContent)
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
        val targetReader = (json[KEY_RANGE] as? JsonObject)?.let {
            JsonPropertyReader(it, "$path.$KEY_RANGE", policy, onWarn)
        }

        val min = targetReader?.optionalFloat(minKey)?.getOrNull()?.toDouble()
            ?: (json[minKey] as? JsonPrimitive)?.floatOrNull?.toDouble()
            ?: defaultMin

        val max = targetReader?.optionalFloat(maxKey)?.getOrNull()?.toDouble()
            ?: (json[maxKey] as? JsonPrimitive)?.floatOrNull?.toDouble()
            ?: defaultMax

        val step = targetReader?.optionalFloat(stepKey)?.getOrNull()?.toDouble()
            ?: (json[stepKey] as? JsonPrimitive)?.floatOrNull?.toDouble()
            ?: defaultStep

        return NumberRange.of(min, max, step).fold(
            onLeft = { domainError ->
                Either.Left(
                    Nel.of(
                        ParserError.InvalidPropertyValue(
                            path = path,
                            property = KEY_RANGE,
                            reason = domainError.reason,
                            domainError = domainError
                        )
                    )
                )
            },
            onRight = { Either.Right(it) }
        )
    }
}

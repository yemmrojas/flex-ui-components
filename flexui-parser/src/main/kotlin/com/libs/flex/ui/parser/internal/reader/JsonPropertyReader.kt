package com.libs.flex.ui.parser.internal.reader

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.model.node.SelectOption
import com.libs.flex.ui.core.domain.validation.ValidationRule
import com.libs.flex.ui.core.domain.value.Dimension
import com.libs.flex.ui.core.domain.value.FontSize
import com.libs.flex.ui.core.domain.value.HexColor
import com.libs.flex.ui.core.domain.value.NumberRange
import com.libs.flex.ui.core.domain.value.PaddingValues
import com.libs.flex.ui.core.domain.value.StyleProperties
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.ParserWarning
import com.libs.flex.ui.parser.internal.mapper.validation.ValidationRuleMapper
import com.libs.flex.ui.parser.internal.model.CommonProperties
import com.libs.flex.ui.parser.internal.util.IdSanitizer
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull

// Constantes de dimensiones especiales
private val MATCH_PARENT_ALIASES = setOf("match_parent", "matchparent", "fill_max_size")
private val WRAP_CONTENT_ALIASES = setOf("wrap_content", "wrapcontent")

// Claves de propiedades comunes y estilos
private const val KEY_ID = "id"
private const val KEY_STYLE = "style"
private const val KEY_STYLES = "styles"
private const val KEY_PADDING = "padding"
private const val KEY_MARGIN = "margin"
private const val KEY_RANGE = "range"

/**
 * Implementación defensiva de [PropertyReader] que opera sobre [JsonObject] de `kotlinx.serialization`.
 *
 * Utiliza funciones de orden superior para eliminar duplicación y aislar el manejo de fallos por política.
 */
internal class JsonPropertyReader(
    val json: JsonObject,
    override val path: String,
    val policy: ErrorPolicy,
    val onWarn: (ParserWarning) -> Unit
) : PropertyReader {

    // =========================================================================
    // 1. Lecturas Primitivas y Cadenas
    // =========================================================================

    override fun requiredString(primaryKey: String, vararg aliases: String): Either<Nel<ParserError>, String> {
        val entry = findElement(primaryKey, *aliases)
            ?: return Either.Left(Nel.of(ParserError.MissingRequiredProperty(path, primaryKey)))

        val content = (entry.second as? JsonPrimitive)?.contentOrNull
        return if (content != null) {
            Either.Right(content)
        } else {
            Either.Left(
                Nel.of(
                    ParserError.InvalidPropertyValue(
                        path = path,
                        property = entry.first,
                        reason = "Expected string primitive, but got ${entry.second::class.simpleName}"
                    )
                )
            )
        }
    }

    override fun optionalString(
        primaryKey: String,
        vararg aliases: String,
        default: String?
    ): Either<Nel<ParserError>, String?> = readOptionalPrimitive(
        primaryKey = primaryKey,
        aliases = aliases,
        default = default,
        expectedType = "string primitive"
    ) { it.contentOrNull }

    override fun optionalBoolean(
        primaryKey: String,
        vararg aliases: String,
        default: Boolean
    ): Either<Nel<ParserError>, Boolean> = readOptionalPrimitive(
        primaryKey = primaryKey,
        aliases = aliases,
        default = default,
        expectedType = "boolean"
    ) { primitive ->
        primitive.booleanOrNull ?: primitive.contentOrNull?.toBooleanStrictOrNull()
    }.map { it ?: default }

    override fun optionalInt(
        primaryKey: String,
        vararg aliases: String,
        default: Int?
    ): Either<Nel<ParserError>, Int?> = readOptionalPrimitive(
        primaryKey = primaryKey,
        aliases = aliases,
        default = default,
        expectedType = "integer"
    ) { primitive ->
        primitive.intOrNull ?: primitive.contentOrNull?.toIntOrNull()
    }

    override fun optionalFloat(
        primaryKey: String,
        vararg aliases: String,
        default: Float?
    ): Either<Nel<ParserError>, Float?> = readOptionalPrimitive(
        primaryKey = primaryKey,
        aliases = aliases,
        default = default,
        expectedType = "finite float"
    ) { primitive ->
        val value = primitive.floatOrNull ?: primitive.contentOrNull?.toFloatOrNull()
        if (value != null && value.isFinite()) value else null
    }

    // =========================================================================
    // 2. Value Objects del Dominio (Color, Dimension, FontSize)
    // =========================================================================

    override fun optionalColor(
        primaryKey: String,
        vararg aliases: String,
        default: HexColor?
    ): Either<Nel<ParserError>, HexColor?> {
        val entry = findElement(primaryKey, *aliases) ?: return Either.Right(default)
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

    override fun optionalDimensionDp(
        primaryKey: String,
        vararg aliases: String,
        default: Dimension.Dp?
    ): Either<Nel<ParserError>, Dimension.Dp?> {
        val entry = findElement(primaryKey, *aliases) ?: return Either.Right(default)
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

    override fun optionalFontSize(
        primaryKey: String,
        vararg aliases: String
    ): Either<Nel<ParserError>, FontSize?> {
        val entry = findElement(primaryKey, *aliases) ?: return Either.Right(null)
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

    // =========================================================================
    // 3. Propiedades Comunes y Estilos
    // =========================================================================

    override fun readCommonProperties(): Either<Nel<ParserError>, CommonProperties> {
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

    private fun readStyleProperties(styleReader: JsonPropertyReader): StyleProperties {
        return StyleProperties(
            padding = styleReader.readPaddingValues(KEY_PADDING),
            margin = styleReader.readPaddingValues(KEY_MARGIN),
            backgroundColor = styleReader.optionalColor("backgroundColor", "bgColor").getOrNull(),
            borderRadius = styleReader.optionalDimensionDp("borderRadius", "cornerRadius").getOrNull(),
            elevation = styleReader.optionalDimensionDp("elevation").getOrNull(),
            width = styleReader.optionalDimension("width").getOrNull(),
            height = styleReader.optionalDimension("height").getOrNull(),
            weight = styleReader.optionalFloat("weight").getOrNull()
        )
    }

    private fun readPaddingValues(key: String): PaddingValues? {
        val entry = findElement(key) ?: return null
        return when (val element = entry.second) {
            is JsonPrimitive -> {
                val floatVal = element.floatOrNull ?: element.contentOrNull?.toFloatOrNull()
                floatVal?.let { Dimension.Dp.of(it).getOrNull() }?.let { dp ->
                    PaddingValues(start = dp, top = dp, end = dp, bottom = dp)
                }
            }
            is JsonObject -> {
                val pReader = JsonPropertyReader(element, "$path.$key", policy, onWarn)
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

    // =========================================================================
    // 4. Reglas de Validación, Opciones de Selección y Rango Numérico
    // =========================================================================

    override fun readValidationRules(
        primaryKey: String,
        vararg aliases: String
    ): Either<Nel<ParserError>, List<ValidationRule>> {
        val entry = findElement(primaryKey, *aliases) ?: return Either.Right(emptyList())
        return ValidationRuleMapper.mapRules(entry.second, "$path.${entry.first}", policy, onWarn)
    }

    override fun readSelectOptions(
        primaryKey: String,
        vararg aliases: String
    ): Either<Nel<ParserError>, Nel<SelectOption>> {
        val entry = findElement(primaryKey, *aliases)
            ?: return Either.Left(Nel.of(ParserError.MissingRequiredProperty(path, primaryKey)))

        val array = entry.second as? JsonArray
            ?: return Either.Left(Nel.of(ParserError.InvalidPropertyValue(path, entry.first, "Expected JSON array of options")))

        if (array.isEmpty()) {
            return Either.Left(Nel.of(ParserError.InvalidPropertyValue(path, entry.first, "Options array cannot be empty")))
        }

        val options = mutableListOf<SelectOption>()
        val errors = mutableListOf<ParserError>()

        array.forEachIndexed { index, optionElement ->
            val optPath = "$path.${entry.first}[$index]"
            if (optionElement !is JsonObject) {
                errors.add(ParserError.InvalidPropertyValue(optPath, "option", "Expected JSON object for option"))
                return@forEachIndexed
            }
            val optReader = JsonPropertyReader(optionElement, optPath, policy, onWarn)
            val valRes = optReader.requiredString("value", "id")
            val labelRes = optReader.optionalString("label", "text", "title")

            when (valRes) {
                is Either.Left -> errors.addAll(valRes.value.toList())
                is Either.Right -> options.add(
                    SelectOption(value = valRes.value, label = labelRes.getOrNull() ?: valRes.value)
                )
            }
        }

        return if (errors.isNotEmpty()) {
            Either.Left(Nel.fromListOrNull(errors)!!)
        } else {
            val nel = Nel.fromListOrNull(options)
            if (nel != null) Either.Right(nel)
            else Either.Left(Nel.of(ParserError.InvalidPropertyValue(path, entry.first, "Options list cannot be empty")))
        }
    }

    override fun readNumberRange(
        minKey: String,
        maxKey: String,
        stepKey: String,
        defaultMin: Double,
        defaultMax: Double,
        defaultStep: Double
    ): Either<Nel<ParserError>, NumberRange> {
        val targetReader = (json[KEY_RANGE] as? JsonObject)?.let {
            JsonPropertyReader(it, "$path.$KEY_RANGE", policy, onWarn)
        } ?: this

        val min = targetReader.optionalFloat(minKey).getOrNull()?.toDouble() ?: defaultMin
        val max = targetReader.optionalFloat(maxKey).getOrNull()?.toDouble() ?: defaultMax
        val step = targetReader.optionalFloat(stepKey).getOrNull()?.toDouble() ?: defaultStep

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

    // =========================================================================
    // 5. Utilidades Internas
    // =========================================================================

    private fun findElement(primaryKey: String, vararg aliases: String): Pair<String, JsonElement>? {
        json[primaryKey]?.let { return Pair(primaryKey, it) }
        aliases.forEach { alias ->
            json[alias]?.let { return Pair(alias, it) }
        }
        return null
    }

    private inline fun <T> readOptionalPrimitive(
        primaryKey: String,
        aliases: Array<out String>,
        default: T?,
        expectedType: String,
        convert: (JsonPrimitive) -> T?
    ): Either<Nel<ParserError>, T?> {
        val entry = findElement(primaryKey, *aliases) ?: return Either.Right(default)
        val primitive = entry.second as? JsonPrimitive

        if (primitive == null) {
            val error = ParserError.InvalidPropertyValue(path, entry.first, "Expected $expectedType")
            return policy.handleOptionalError(path, entry.first, error, default, onWarn)
        }

        val value = convert(primitive)
        if (value == null) {
            val error = ParserError.InvalidPropertyValue(
                path = path,
                property = entry.first,
                reason = "Cannot convert '${primitive.contentOrNull}' to $expectedType"
            )
            return policy.handleOptionalError(path, entry.first, error, default, onWarn)
        }

        return Either.Right(value)
    }
}

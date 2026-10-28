package com.libs.flex.ui.parser.internal.mapper.validation

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.validation.MaxLengthRule
import com.libs.flex.ui.core.domain.validation.MinLengthRule
import com.libs.flex.ui.core.domain.validation.NumberRangeRule
import com.libs.flex.ui.core.domain.validation.RegexRule
import com.libs.flex.ui.core.domain.validation.RequiredRule
import com.libs.flex.ui.core.domain.validation.ValidationRule
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.ParserWarning
import com.libs.flex.ui.parser.internal.reader.ErrorPolicy
import com.libs.flex.ui.parser.internal.reader.JsonPropertyReader
import com.libs.flex.ui.parser.internal.reader.PropertyReader
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

// Nombres de propiedades JSON (constantes privadas a nivel de archivo)
private const val KEY_TYPE = "type"
private const val KEY_MESSAGE = "message"
private const val KEY_CUSTOM_MESSAGE = "customMessage"
private const val KEY_VALIDATIONS = "validations"
private const val KEY_MIN = "min"
private const val KEY_MAX = "max"
private const val KEY_STEP = "step"
private const val KEY_PATTERN = "pattern"
private const val KEY_REGEX = "regex"

// Valores predeterminados numéricos
private const val DEFAULT_MIN_LENGTH = 0
private const val DEFAULT_RANGE_MIN = 0.0
private const val DEFAULT_RANGE_MAX = 100.0
private const val DEFAULT_RANGE_STEP = 1.0

// Conjuntos inmutables para coincidencia O(1)
private val TYPE_REQUIRED = setOf("required", "notempty")
private val TYPE_MIN_LENGTH = setOf("minlength", "min_length")
private val TYPE_MAX_LENGTH = setOf("maxlength", "max_length")
private val TYPE_REGEX = setOf("regex", "pattern")
private val TYPE_NUMBER_RANGE = setOf("numberrange", "number_range", "range")

/**
 * Mapeador interno para transformar especificaciones JSON de validaciones en objetos [ValidationRule] de dominio.
 *
 * Implementa SRP dividiendo el análisis de cada regla en funciones modulares y reutilizando [PropertyReader].
 */
internal object ValidationRuleMapper {

    /**
     * Mapea un array JSON opcional de especificaciones de validación a una lista de [ValidationRule].
     */
    fun mapRules(
        element: JsonElement?,
        path: String,
        policy: ErrorPolicy,
        onWarn: (ParserWarning) -> Unit
    ): Either<Nel<ParserError>, List<ValidationRule>> {
        if (element == null) return Either.Right(emptyList())

        if (element !is JsonArray) {
            val error = ParserError.InvalidPropertyValue(
                path = path,
                property = KEY_VALIDATIONS,
                reason = "Expected JSON array of validation rules"
            )
            return policy.handleOptionalError<List<ValidationRule>>(path, KEY_VALIDATIONS, error, emptyList(), onWarn)
                .map { it ?: emptyList() }
        }

        return parseRulesArray(element, path, policy, onWarn)
    }

    /**
     * Mapea una regla individual a partir de su [JsonObject].
     */
    fun mapRule(
        json: JsonObject,
        path: String,
        policy: ErrorPolicy,
        onWarn: (ParserWarning) -> Unit
    ): Either<Nel<ParserError>, ValidationRule?> {
        val reader = JsonPropertyReader(json, path, policy, onWarn)

        val rawType = reader.requiredString(KEY_TYPE).fold(
            onLeft = { return policy.handleOptionalError(path, KEY_TYPE, it.head, null, onWarn) },
            onRight = { it }
        )

        val message = reader.optionalString(
            KEY_MESSAGE,
            KEY_CUSTOM_MESSAGE
        ).fold(
            onLeft = {
                return policy.handleOptionalError(
                    path, KEY_MESSAGE,
                    it.head,
                    null,
                    onWarn
                )
            },
            onRight = { it }
        )

        return when (rawType.lowercase()) {
            in TYPE_REQUIRED -> Either.Right(RequiredRule(customMessage = message))
            in TYPE_MIN_LENGTH -> mapMinLength(reader, path, policy, onWarn, message)
            in TYPE_MAX_LENGTH -> mapMaxLength(reader, path, policy, onWarn, message)
            in TYPE_REGEX -> mapRegex(reader, path, policy, onWarn, message)
            in TYPE_NUMBER_RANGE -> mapNumberRange(reader, path, policy, onWarn, message)
            else -> {
                val error = ParserError.UnknownComponentType(path, rawType)
                policy.handleOptionalError(path, KEY_TYPE, error, null, onWarn)
            }
        }
    }

    // === MAPPERS ESPECÍFICOS DE REGLA (SRP) ===

    private fun mapMinLength(
        reader: PropertyReader,
        path: String,
        policy: ErrorPolicy,
        onWarn: (ParserWarning) -> Unit,
        message: String?
    ): Either<Nel<ParserError>, ValidationRule?> {
        return reader.optionalInt(KEY_MIN, default = DEFAULT_MIN_LENGTH).flatMap { rawMin ->
            val min = rawMin ?: DEFAULT_MIN_LENGTH
            MinLengthRule.of(min, message).fold(
                onLeft = { domainError ->
                    val error = ParserError.InvalidPropertyValue(path, KEY_MIN, domainError.reason, domainError)
                    policy.handleOptionalError(path, KEY_MIN, error, null, onWarn)
                },
                onRight = { Either.Right(it) }
            )
        }
    }

    private fun mapMaxLength(
        reader: PropertyReader,
        path: String,
        policy: ErrorPolicy,
        onWarn: (ParserWarning) -> Unit,
        message: String?
    ): Either<Nel<ParserError>, ValidationRule?> {
        return reader.optionalInt(KEY_MAX, default = Int.MAX_VALUE).flatMap { rawMax ->
            val max = rawMax ?: Int.MAX_VALUE
            MaxLengthRule.of(max, message).fold(
                onLeft = { domainError ->
                    val error = ParserError.InvalidPropertyValue(path, KEY_MAX, domainError.reason, domainError)
                    policy.handleOptionalError(path, KEY_MAX, error, null, onWarn)
                },
                onRight = { Either.Right(it) }
            )
        }
    }

    private fun mapRegex(
        reader: PropertyReader,
        path: String,
        policy: ErrorPolicy,
        onWarn: (ParserWarning) -> Unit,
        message: String?
    ): Either<Nel<ParserError>, ValidationRule?> {
        val pattern = reader.requiredString(KEY_PATTERN, KEY_REGEX).fold(
            onLeft = { return policy.handleOptionalError(path, KEY_PATTERN, it.head, null, onWarn) },
            onRight = { it }
        )

        return RegexRule.of(pattern, message).fold(
            onLeft = { domainError ->
                val error = ParserError.InvalidPropertyValue(path, KEY_PATTERN, domainError.reason, domainError)
                policy.handleOptionalError(path, KEY_PATTERN, error, null, onWarn)
            },
            onRight = { Either.Right(it) }
        )
    }

    private fun mapNumberRange(
        reader: PropertyReader,
        path: String,
        policy: ErrorPolicy,
        onWarn: (ParserWarning) -> Unit,
        message: String?
    ): Either<Nel<ParserError>, ValidationRule?> {
        return reader.readNumberRange(
            minKey = KEY_MIN,
            maxKey = KEY_MAX,
            stepKey = KEY_STEP,
            defaultMin = DEFAULT_RANGE_MIN,
            defaultMax = DEFAULT_RANGE_MAX,
            defaultStep = DEFAULT_RANGE_STEP
        ).fold(
            onLeft = { errors ->
                policy.handleOptionalError(path, "numberRange", errors.head, null, onWarn)
            },
            onRight = { range ->
                Either.Right(NumberRangeRule(range, message))
            }
        )
    }

    private fun parseRulesArray(
        array: JsonArray,
        path: String,
        policy: ErrorPolicy,
        onWarn: (ParserWarning) -> Unit
    ): Either<Nel<ParserError>, List<ValidationRule>> {
        val rules = mutableListOf<ValidationRule>()
        val errors = mutableListOf<ParserError>()

        array.forEachIndexed { index, ruleElement ->
            val rulePath = "$path[$index]"
            if (ruleElement !is JsonObject) {
                errors.add(ParserError.InvalidPropertyValue(rulePath, "rule", "Expected JSON object for validation rule"))
                return@forEachIndexed
            }

            when (val mappedRule = mapRule(ruleElement, rulePath, policy, onWarn)) {
                is Either.Left -> errors.addAll(mappedRule.value.toList())
                is Either.Right -> mappedRule.value?.let { rules.add(it) }
            }
        }

        return if (errors.isEmpty()) {
            Either.Right(rules)
        } else {
            Either.Left(Nel.fromListOrNull(errors)!!)
        }
    }
}

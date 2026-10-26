package com.libs.flex.ui.parser.fakes

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.model.node.SelectOption
import com.libs.flex.ui.core.domain.validation.ValidationRule
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.Dimension
import com.libs.flex.ui.core.domain.value.FontSize
import com.libs.flex.ui.core.domain.value.HexColor
import com.libs.flex.ui.core.domain.value.NumberRange
import com.libs.flex.ui.core.domain.value.StyleProperties
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.internal.model.CommonProperties
import com.libs.flex.ui.parser.internal.reader.PropertyReader
import com.libs.flex.ui.parser.internal.util.IdSanitizer

/**
 * Fake en memoria de [PropertyReader] para pruebas unitarias instantáneas y desacopladas de JSON.
 */
internal class FakePropertyReader(
    override val path: String = "test.node",
    private val strings: Map<String, String> = emptyMap(),
    private val booleans: Map<String, Boolean> = emptyMap(),
    private val ints: Map<String, Int> = emptyMap(),
    private val floats: Map<String, Float> = emptyMap(),
    private val colors: Map<String, HexColor> = emptyMap(),
    private val dimensions: Map<String, Dimension> = emptyMap(),
    private val fontSizes: Map<String, FontSize> = emptyMap(),
    private val validationRules: List<ValidationRule> = emptyList(),
    private val selectOptions: List<SelectOption> = emptyList(),
    private val numberRange: NumberRange? = null,
    private val id: String? = "test_id",
    private val style: StyleProperties = StyleProperties()
) : PropertyReader {

    override fun requiredString(primaryKey: String, vararg aliases: String): Either<Nel<ParserError>, String> {
        val value = strings[primaryKey] ?: aliases.firstNotNullOfOrNull { strings[it] }
        return if (value != null) Either.Right(value)
        else Either.Left(Nel.of(ParserError.MissingRequiredProperty(path, primaryKey)))
    }

    override fun optionalString(
        primaryKey: String,
        vararg aliases: String,
        default: String?
    ): Either<Nel<ParserError>, String?> {
        val value = strings[primaryKey] ?: aliases.firstNotNullOfOrNull { strings[it] } ?: default
        return Either.Right(value)
    }

    override fun optionalBoolean(
        primaryKey: String,
        vararg aliases: String,
        default: Boolean
    ): Either<Nel<ParserError>, Boolean> {
        val value = booleans[primaryKey] ?: aliases.firstNotNullOfOrNull { booleans[it] } ?: default
        return Either.Right(value)
    }

    override fun optionalInt(
        primaryKey: String,
        vararg aliases: String,
        default: Int?
    ): Either<Nel<ParserError>, Int?> {
        val value = ints[primaryKey] ?: aliases.firstNotNullOfOrNull { ints[it] } ?: default
        return Either.Right(value)
    }

    override fun optionalFloat(
        primaryKey: String,
        vararg aliases: String,
        default: Float?
    ): Either<Nel<ParserError>, Float?> {
        val value = floats[primaryKey] ?: aliases.firstNotNullOfOrNull { floats[it] } ?: default
        return Either.Right(value)
    }

    override fun optionalColor(
        primaryKey: String,
        vararg aliases: String,
        default: HexColor?
    ): Either<Nel<ParserError>, HexColor?> {
        val value = colors[primaryKey] ?: aliases.firstNotNullOfOrNull { colors[it] } ?: default
        return Either.Right(value)
    }

    override fun optionalDimensionDp(
        primaryKey: String,
        vararg aliases: String,
        default: Dimension.Dp?
    ): Either<Nel<ParserError>, Dimension.Dp?> {
        val value = (dimensions[primaryKey] ?: aliases.firstNotNullOfOrNull { dimensions[it] }) as? Dimension.Dp ?: default
        return Either.Right(value)
    }

    override fun optionalDimension(
        primaryKey: String,
        vararg aliases: String,
        default: Dimension?
    ): Either<Nel<ParserError>, Dimension?> {
        val value = dimensions[primaryKey] ?: aliases.firstNotNullOfOrNull { dimensions[it] } ?: default
        return Either.Right(value)
    }

    override fun readCommonProperties(): Either<Nel<ParserError>, CommonProperties> {
        val componentId = IdSanitizer.sanitizeToComponentId(id, fallbackPrefix = "auto_$path")
        return Either.Right(CommonProperties(id = componentId, style = style))
    }

    override fun optionalFontSize(
        primaryKey: String,
        vararg aliases: String
    ): Either<Nel<ParserError>, FontSize?> {
        val value = fontSizes[primaryKey] ?: aliases.firstNotNullOfOrNull { fontSizes[it] }
        return Either.Right(value)
    }

    override fun readValidationRules(
        primaryKey: String,
        vararg aliases: String
    ): Either<Nel<ParserError>, List<ValidationRule>> {
        return Either.Right(validationRules)
    }

    override fun readSelectOptions(
        primaryKey: String,
        vararg aliases: String
    ): Either<Nel<ParserError>, Nel<SelectOption>> {
        val nel = Nel.fromListOrNull(selectOptions)
        return if (nel != null) Either.Right(nel)
        else Either.Left(Nel.of(ParserError.MissingRequiredProperty(path, primaryKey)))
    }

    override fun readNumberRange(
        minKey: String,
        maxKey: String,
        stepKey: String,
        defaultMin: Double,
        defaultMax: Double,
        defaultStep: Double
    ): Either<Nel<ParserError>, NumberRange> {
        return if (numberRange != null) {
            Either.Right(numberRange)
        } else {
            NumberRange.of(defaultMin, defaultMax, defaultStep).fold(
                onLeft = { domainError ->
                    Either.Left(Nel.of(ParserError.InvalidPropertyValue(path, "range", domainError.reason, domainError)))
                },
                onRight = { Either.Right(it) }
            )
        }
    }
}

package com.libs.flex.ui.core.domain.validation

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.value.FieldValue

public class RegexRule private constructor(
    public val pattern: String,
    private val compiledRegex: Regex,
    public val customMessage: String? = null
) : ValidationRule {

    override fun validate(value: FieldValue?): Either<ValidationError, Unit> = when (value) {
        null -> Either.Right(Unit)
        is FieldValue.Text -> if (!compiledRegex.matches(value.value)) {
            Either.Left(ValidationError.PatternMismatch(pattern, customMessage ?: "Value does not match pattern: $pattern"))
        } else {
            Either.Right(Unit)
        }

        else -> Either.Left(ValidationError.InvalidType("Text"))
    }

    public companion object {
        public fun of(
            pattern: String,
            customMessage: String? = null
        ): Either<DomainError.Value.InvalidRegexPattern, RegexRule> = try {
            Either.Right(RegexRule(pattern, Regex(pattern), customMessage))
        } catch (e: Exception) {
            Either.Left(DomainError.Value.InvalidRegexPattern(pattern, e.message ?: "Invalid regex pattern"))
        }
    }
}

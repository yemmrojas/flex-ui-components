package com.libs.flex.ui.core.domain.validation

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.value.FieldValue

public data class RegexRule(
    public val pattern: String,
    public val customMessage: String? = null
) : ValidationRule {
    private val compiledRegex: Regex = Regex(pattern)

    override fun validate(value: FieldValue?): Either<ValidationError, Unit> = when (value) {
        null -> Either.Right(Unit)
        is FieldValue.Text -> if (!compiledRegex.matches(value.value)) {
            Either.Left(ValidationError.PatternMismatch(pattern, customMessage ?: "Value does not match pattern: $pattern"))
        } else {
            Either.Right(Unit)
        }

        else -> Either.Left(ValidationError.InvalidType("Text"))
    }
}

package com.libs.flex.ui.core.domain.validation

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.value.FieldValue

public data class MinLengthRule(
    public val min: Int,
    public val customMessage: String? = null
) : ValidationRule {
    override fun validate(value: FieldValue?): Either<ValidationError, Unit> = when (value) {
        null -> Either.Right(Unit) // Null check is handled by RequiredRule
        is FieldValue.Text -> if (value.value.length < min) {
            Either.Left(
                ValidationError.MinLength(
                    min,
                    value.value.length,
                    customMessage ?: "Length must be at least $min, but was ${value.value.length}"
                )
            )
        } else {
            Either.Right(Unit)
        }

        else -> Either.Left(ValidationError.InvalidType("Text"))
    }
}

package com.libs.flex.ui.core.domain.validation

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.value.FieldValue

public data class MaxLengthRule(
    public val max: Int,
    public val customMessage: String? = null
) : ValidationRule {
    override fun validate(value: FieldValue?): Either<ValidationError, Unit> = when (value) {
        null -> Either.Right(Unit)
        is FieldValue.Text -> if (value.value.length > max) {
            Either.Left(
                ValidationError.MaxLength(
                    max,
                    value.value.length,
                    customMessage ?: "Length must be at most $max, but was ${value.value.length}"
                )
            )
        } else {
            Either.Right(Unit)
        }

        else -> Either.Left(ValidationError.InvalidType("Text"))
    }
}

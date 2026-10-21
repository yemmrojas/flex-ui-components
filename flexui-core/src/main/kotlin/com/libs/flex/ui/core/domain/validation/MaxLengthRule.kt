package com.libs.flex.ui.core.domain.validation

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.value.FieldValue

public class MaxLengthRule private constructor(
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

    public companion object {
        public fun of(
            max: Int,
            customMessage: String? = null
        ): Either<DomainError.Value.InvalidValidationRule, MaxLengthRule> =
            if (max >= 0) {
                Either.Right(MaxLengthRule(max, customMessage))
            } else {
                Either.Left(DomainError.Value.InvalidValidationRule("max length must be >= 0, was: $max"))
            }
    }
}

package com.libs.flex.ui.core.domain.validation

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.value.FieldValue
import com.libs.flex.ui.core.domain.value.NumberRange

public data class NumberRangeRule(
    public val range: NumberRange,
    public val customMessage: String? = null
) : ValidationRule {
    override fun validate(value: FieldValue?): Either<ValidationError, Unit> = when (value) {
        null -> Either.Right(Unit)
        is FieldValue.Number -> if (value.value < range.min || value.value > range.max) {
            Either.Left(
                ValidationError.NumberOutOfRange(
                    range,
                    value.value,
                    customMessage ?: "Number must be between ${range.min} and ${range.max}, but was ${value.value}"
                )
            )
        } else {
            Either.Right(Unit)
        }

        else -> Either.Left(ValidationError.InvalidType("Number"))
    }
}

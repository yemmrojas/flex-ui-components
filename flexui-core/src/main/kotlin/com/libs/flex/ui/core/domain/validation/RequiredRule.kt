package com.libs.flex.ui.core.domain.validation

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.value.FieldValue

public data class RequiredRule(
    public val customMessage: String? = null
) : ValidationRule {
    override fun validate(value: FieldValue?): Either<ValidationError, Unit> =
        when (value) {
            null -> Either.Left(ValidationError.Required(customMessage ?: "Field is required"))
            is FieldValue.Text -> if (value.value.trim().isEmpty()) {
                Either.Left(ValidationError.Required(customMessage ?: "Field is required"))
            } else {
                Either.Right(Unit)
            }

            is FieldValue.Selection -> if (value.selectedIndices.isEmpty()) {
                Either.Left(ValidationError.Required(customMessage ?: "Selection cannot be empty"))
            } else {
                Either.Right(Unit)
            }

            is FieldValue.Bool, is FieldValue.Number -> Either.Right(Unit)
        }
}

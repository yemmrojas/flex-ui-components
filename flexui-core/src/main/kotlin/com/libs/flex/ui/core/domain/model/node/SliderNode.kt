package com.libs.flex.ui.core.domain.model.node

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.validation.ValidationRule
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.FieldValue
import com.libs.flex.ui.core.domain.value.NumberRange
import com.libs.flex.ui.core.domain.value.StyleProperties

public data class SliderNode(
    override val id: ComponentId,
    override val style: StyleProperties? = null,
    public val range: NumberRange,
    public val step: Double = 1.0,
    override val validationRules: List<ValidationRule> = emptyList(),
    override val isSensitive: Boolean = false
) : InputCapableNode {

    override fun accept(value: FieldValue): Either<DomainError.Value, FieldValue> = when (value) {
        is FieldValue.Number -> {
            if (value.value in range.min..range.max) Either.Right(value)
            else Either.Left(DomainError.Value.ValueOutOfRange(value.value, range.min, range.max))
        }
        else -> Either.Left(
            DomainError.Value.ValueTypeMismatch(
                expected = "FieldValue.Number",
                actual = value::class.simpleName ?: "Unknown"
            )
        )
    }
}

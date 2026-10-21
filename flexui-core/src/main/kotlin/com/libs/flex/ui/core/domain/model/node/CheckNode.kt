package com.libs.flex.ui.core.domain.model.node

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.validation.ValidationRule
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.FieldValue
import com.libs.flex.ui.core.domain.value.StyleProperties

public data class CheckNode(
    override val id: ComponentId,
    override val style: StyleProperties? = null,
    public val label: String,
    public val defaultChecked: Boolean = false,
    override val validationRules: List<ValidationRule> = emptyList(),
    override val isSensitive: Boolean = false
) : InputCapableNode {
    override fun accept(value: FieldValue): Either<DomainError.Value, FieldValue> = when (value) {
        is FieldValue.Bool -> Either.Right(value)
        else -> Either.Left(
            DomainError.Value.ValueTypeMismatch(
                expected = "FieldValue.Bool",
                actual = value::class.simpleName ?: "Unknown"
            )
        )
    }
}

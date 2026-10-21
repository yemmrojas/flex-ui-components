package com.libs.flex.ui.core.domain.model.node

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.validation.ValidationRule
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.FieldValue
import com.libs.flex.ui.core.domain.value.InputVariant
import com.libs.flex.ui.core.domain.value.StyleProperties

public data class InputNode(
    override val id: ComponentId,
    override val style: StyleProperties? = null,
    public val label: String,
    public val placeholder: String? = null,
    public val inputVariant: InputVariant = InputVariant.Outlined,
    override val validationRules: List<ValidationRule> = emptyList(),
    override val isSensitive: Boolean = (inputVariant == InputVariant.Password)
) : InputCapableNode {
    override fun accept(value: FieldValue): Either<DomainError.Value, FieldValue> = when (value) {
        is FieldValue.Text -> Either.Right(value)
        else -> Either.Left(
            DomainError.Value.ValueTypeMismatch(
                expected = "FieldValue.Text",
                actual = value::class.simpleName ?: "Unknown"
            )
        )
    }
}

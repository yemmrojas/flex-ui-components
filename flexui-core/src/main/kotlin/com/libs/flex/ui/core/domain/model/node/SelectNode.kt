package com.libs.flex.ui.core.domain.model.node

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.validation.ValidationRule
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.FieldValue
import com.libs.flex.ui.core.domain.value.StyleProperties

public class SelectNode private constructor(
    override val id: ComponentId,
    override val style: StyleProperties? = null,
    public val label: String,
    public val options: Nel<SelectOption>,
    override val validationRules: List<ValidationRule> = emptyList(),
    override val isSensitive: Boolean = false
) : InputCapableNode {

    override fun accept(value: FieldValue): Either<DomainError.Value, FieldValue> = when (value) {
        is FieldValue.Selection -> {
            val valid = value.selectedIndices.all { it in 0 until options.size }
            if (valid) Either.Right(value)
            else Either.Left(DomainError.Value.InvalidOptionSelected("Indices out of range: ${value.selectedIndices}"))
        }
        else -> Either.Left(
            DomainError.Value.ValueTypeMismatch(
                expected = "FieldValue.Selection",
                actual = value::class.simpleName ?: "Unknown"
            )
        )
    }

    public companion object {
        public fun of(
            id: ComponentId,
            style: StyleProperties? = null,
            label: String,
            options: Nel<SelectOption>,
            validationRules: List<ValidationRule> = emptyList()
        ): SelectNode = SelectNode(id, style, label, options, validationRules)
    }
}

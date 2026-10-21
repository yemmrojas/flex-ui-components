package com.libs.flex.ui.core.domain.model.node

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.validation.ValidationRule
import com.libs.flex.ui.core.domain.value.ActionId
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.FieldValue
import com.libs.flex.ui.core.domain.value.StyleProperties

public sealed interface UIComponentNode {
    public val id: ComponentId
    public val style: StyleProperties?
}

public sealed interface InteractiveNode : UIComponentNode {
    public val actionId: ActionId
    public val isEnabled: Boolean
    public val requiresValidation: Boolean
}

public sealed interface InputCapableNode : UIComponentNode {
    public val validationRules: List<ValidationRule>
    public val isSensitive: Boolean
    public fun accept(value: FieldValue): Either<DomainError.Value, FieldValue>
}

public sealed interface LayoutNode : UIComponentNode {
    public val children: List<UIComponentNode>
}

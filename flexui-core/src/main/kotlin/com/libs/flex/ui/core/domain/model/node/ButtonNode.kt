package com.libs.flex.ui.core.domain.model.node

import com.libs.flex.ui.core.domain.value.ActionId
import com.libs.flex.ui.core.domain.value.ButtonVariant
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.StyleProperties

public data class ButtonNode(
    override val id: ComponentId,
    override val style: StyleProperties? = null,
    public val text: String,
    override val actionId: ActionId,
    public val buttonVariant: ButtonVariant = ButtonVariant.Primary,
    override val isEnabled: Boolean = true,
    override val requiresValidation: Boolean = false
) : InteractiveNode

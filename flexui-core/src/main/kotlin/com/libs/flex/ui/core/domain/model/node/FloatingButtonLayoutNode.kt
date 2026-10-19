package com.libs.flex.ui.core.domain.model.node

import com.libs.flex.ui.core.domain.value.ActionId
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.StyleProperties

public data class FloatingButtonLayoutNode(
    override val id: ComponentId,
    override val style: StyleProperties? = null,
    public val fabIcon: String,
    override val actionId: ActionId,
    override val isEnabled: Boolean = true,
    override val requiresValidation: Boolean = false,
    override val children: List<UIComponentNode> = emptyList()
) : LayoutNode, InteractiveNode

package com.libs.flex.ui.core.domain.model.node

import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.StyleProperties

public data class HorizontalLayoutNode(
    override val id: ComponentId,
    override val style: StyleProperties? = null,
    override val children: List<UIComponentNode> = emptyList()
) : LayoutNode

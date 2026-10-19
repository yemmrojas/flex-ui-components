package com.libs.flex.ui.core.domain.model.node

import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.StyleProperties

public data class ListLayoutNode(
    override val id: ComponentId,
    override val style: StyleProperties? = null,
    public val itemTemplateId: ComponentId,
    public val itemsCount: Int = 0,
    override val children: List<UIComponentNode> = emptyList()
) : LayoutNode

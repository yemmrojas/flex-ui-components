package com.libs.flex.ui.core.domain.model.node

import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.StyleProperties

public data class SliderLayoutNode(
    override val id: ComponentId,
    override val style: StyleProperties? = null,
    public val autoPlay: Boolean = false,
    public val intervalMs: Long = 3000L,
    override val children: List<UIComponentNode> = emptyList()
) : LayoutNode

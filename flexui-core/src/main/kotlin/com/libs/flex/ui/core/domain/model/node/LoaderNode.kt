package com.libs.flex.ui.core.domain.model.node

import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.StyleProperties

public data class LoaderNode(
    override val id: ComponentId,
    override val style: StyleProperties? = null,
    public val variant: String = "circular",
    public val size: String = "medium"
) : UIComponentNode

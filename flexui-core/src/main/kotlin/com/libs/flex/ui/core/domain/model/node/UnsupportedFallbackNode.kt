package com.libs.flex.ui.core.domain.model.node

import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.StyleProperties

public data class UnsupportedFallbackNode(
    override val id: ComponentId,
    override val style: StyleProperties? = null,
    public val rawType: String,
    public val reason: String
) : UIComponentNode

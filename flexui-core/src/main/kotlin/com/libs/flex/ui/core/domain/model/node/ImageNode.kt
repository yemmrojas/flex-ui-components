package com.libs.flex.ui.core.domain.model.node

import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.StyleProperties

public data class ImageNode(
    override val id: ComponentId,
    override val style: StyleProperties? = null,
    public val url: String,
    public val contentDescription: String? = null,
    public val scaleMode: String = "fit"
) : UIComponentNode

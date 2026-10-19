package com.libs.flex.ui.core.domain.model.node

import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.FontSize
import com.libs.flex.ui.core.domain.value.StyleProperties

public data class TextNode(
    override val id: ComponentId,
    override val style: StyleProperties? = null,
    public val text: String,
    public val typography: String = "body1",
    public val fontSize: FontSize? = null,
    public val maxLines: Int? = null
) : UIComponentNode

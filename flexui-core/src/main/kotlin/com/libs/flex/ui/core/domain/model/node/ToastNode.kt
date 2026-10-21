package com.libs.flex.ui.core.domain.model.node

import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.StyleProperties

public data class ToastNode(
    override val id: ComponentId,
    override val style: StyleProperties? = null,
    public val message: String,
    public val type: String = "info",
    public val durationMs: Long = 3000L
) : UIComponentNode

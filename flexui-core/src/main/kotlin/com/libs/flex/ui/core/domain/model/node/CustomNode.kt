package com.libs.flex.ui.core.domain.model.node

import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.PropertyValue
import com.libs.flex.ui.core.domain.value.StyleProperties

public data class CustomNode(
    override val id: ComponentId,
    override val style: StyleProperties? = null,
    public val type: String,
    public val properties: Map<String, PropertyValue> = emptyMap()
) : UIComponentNode

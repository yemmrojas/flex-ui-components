package com.libs.flex.ui.core.domain.ports

import com.libs.flex.ui.core.domain.value.ActionId
import com.libs.flex.ui.core.domain.value.FieldValue
import com.libs.flex.ui.core.domain.value.NodeKey

public data class UserAction(
    public val actionId: ActionId,
    public val sourceKey: NodeKey,
    public val formValues: Map<NodeKey, FieldValue>
)

package com.libs.flex.ui.core.domain.model

import com.libs.flex.ui.core.domain.model.node.InputCapableNode
import com.libs.flex.ui.core.domain.validation.ValidationError
import com.libs.flex.ui.core.domain.value.FieldValue
import com.libs.flex.ui.core.domain.value.NodeKey

internal interface FormInteractionState {
    val values: Map<NodeKey, FieldValue>
    val errors: Map<NodeKey, ValidationError>

    fun isValid(): Boolean

    fun update(
        key: NodeKey,
        value: FieldValue,
        node: InputCapableNode,
        occurredAt: Long
    ): InternalStateOutcome

    fun validateAll(document: LayoutDocument): FormInteractionState

    companion object {
        fun create(
            values: Map<NodeKey, FieldValue> = emptyMap(),
            errors: Map<NodeKey, ValidationError> = emptyMap()
        ): FormInteractionState = DefaultFormInteractionState(values, errors)
    }
}

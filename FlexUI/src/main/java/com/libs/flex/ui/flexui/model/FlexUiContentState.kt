package com.libs.flex.ui.flexui.model

import androidx.compose.runtime.Immutable
import com.libs.flex.ui.core.domain.model.node.UIComponentNode
import com.libs.flex.ui.core.domain.validation.ValidationError
import com.libs.flex.ui.core.domain.value.FieldValue
import com.libs.flex.ui.core.domain.value.NodeKey

/**
 * Immutable state representing the currently rendered FlexUI form content.
 *
 * @property layoutId Unique layout identifier.
 * @property rootNode Root node of the parsed layout hierarchy.
 * @property values Validated map of values indexed by [NodeKey].
 * @property errors Map of current validation errors indexed by [NodeKey].
 * @property isSubmitting True while an interactive action is in-flight.
 */
@Immutable
public class FlexUiContentState(
    public val layoutId: String,
    public val rootNode: UIComponentNode,
    public val values: Map<NodeKey, FieldValue> = emptyMap(),
    public val errors: Map<NodeKey, ValidationError> = emptyMap(),
    public val isSubmitting: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FlexUiContentState) return false
        return layoutId == other.layoutId &&
               rootNode == other.rootNode &&
               values == other.values &&
               errors == other.errors &&
               isSubmitting == other.isSubmitting
    }

    override fun hashCode(): Int {
        var result = layoutId.hashCode()
        result = 31 * result + rootNode.hashCode()
        result = 31 * result + values.hashCode()
        result = 31 * result + errors.hashCode()
        result = 31 * result + isSubmitting.hashCode()
        return result
    }

    override fun toString(): String {
        return "FlexUiContentState(layoutId=$layoutId, isSubmitting=$isSubmitting)"
    }
}


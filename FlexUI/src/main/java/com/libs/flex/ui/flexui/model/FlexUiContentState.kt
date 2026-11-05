package com.libs.flex.ui.flexui.model

import androidx.compose.runtime.Immutable
import com.libs.flex.ui.core.domain.model.node.UIComponentNode
import com.libs.flex.ui.core.domain.validation.ValidationError
import com.libs.flex.ui.core.domain.value.FieldValue
import com.libs.flex.ui.core.domain.value.NodeKey

/**
 * Immutable state representing the currently rendered FlexUI form content.
 *
 * It is declared as a clean, idiomatic [data class] ensuring automatic generated [copy], [equals], and [hashCode].
 * Its [toString] implementation is sanitized to prevent credential/password leaks in logs and crash telemetry.
 *
 * @property layoutId Unique layout identifier.
 * @property rootNode Root node of the parsed layout hierarchy.
 * @property values Validated map of values indexed by [NodeKey].
 * @property errors Map of current validation errors indexed by [NodeKey].
 * @property isSubmitting True while an interactive action is in-flight.
 */
@Immutable
data class FlexUiContentState(
    val layoutId: String,
    val rootNode: UIComponentNode,
    val values: Map<NodeKey, FieldValue> = emptyMap(),
    val errors: Map<NodeKey, ValidationError> = emptyMap(),
    val isSubmitting: Boolean = false
) {
    override fun toString(): String {
        return "FlexUiContentState(layoutId='$layoutId', valuesCount=${values.size}, errorsCount=${errors.size}, isSubmitting=$isSubmitting)"
    }
}

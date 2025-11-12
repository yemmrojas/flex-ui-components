package com.libs.flex.ui.flexui.components.infrastructure.adapter.atomic

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.libs.flex.ui.flexui.model.AtomicDescriptor
import com.libs.flex.ui.flexui.model.ComponentEvent

/**
 * Renders a checkbox component with label.
 *
 * This composable creates a Row containing a Checkbox and Text label.
 * The checked state is managed internally and emits ValueChange events
 * when the checkbox state changes.
 *
 * @param descriptor Atomic descriptor containing checkbox properties
 * @param onEvent Callback invoked when checkbox state changes
 * @param modifier Modifier to be applied to the Row
 *
 * Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 18.2
 */
@Composable
fun CheckComponent(
    descriptor: AtomicDescriptor,
    onEvent: (ComponentEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var checkedState by remember {
        mutableStateOf(descriptor.checked ?: false)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checkedState,
            onCheckedChange = { newValue ->
                checkedState = newValue
                onEvent(ComponentEvent.ValueChange(descriptor.id, value = newValue))
            },
            enabled = descriptor.enabled ?: true
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(text = descriptor.label ?: "")
    }
}

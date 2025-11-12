package com.libs.flex.ui.flexui.components.infrastructure.adapter.atomic

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.libs.flex.ui.flexui.model.AtomicDescriptor
import com.libs.flex.ui.flexui.model.ComponentEvent
import kotlinx.serialization.json.jsonPrimitive

/**
 * Renders a text input field component.
 *
 * This composable creates a text input field with state management,
 * supporting both outlined and filled styles. It emits ValueChange events
 * when the text changes.
 *
 * Supported inputStyle values:
 * - "outlined": OutlinedTextField with border
 * - Any other value: Standard TextField (default)
 *
 * @param descriptor Atomic descriptor containing input properties
 * @param onEvent Callback invoked when input value changes
 * @param modifier Modifier to be applied to the TextField
 *
 * Requirements: 9.1, 9.2, 9.3, 9.5, 18.2, 18.4
 */
@Composable
fun InputComponent(
    descriptor: AtomicDescriptor,
    onEvent: (ComponentEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var textState by remember {
        mutableStateOf(descriptor.value?.jsonPrimitive?.content ?: "")
    }

    val onValueChange: (String) -> Unit = { newValue ->
        textState = newValue
        onEvent(ComponentEvent.ValueChange(descriptor.id, value = newValue))
    }

    val label: (@Composable () -> Unit)? = descriptor.label?.let { labelText ->
        { Text(labelText) }
    }

    val placeholder: (@Composable () -> Unit)? = descriptor.placeholder?.let { placeholderText ->
        { Text(placeholderText) }
    }

    when (descriptor.inputStyle) {
        "outlined" -> OutlinedTextField(
            value = textState,
            onValueChange = onValueChange,
            label = label,
            placeholder = placeholder,
            enabled = descriptor.enabled ?: true,
            modifier = modifier
        )

        else -> TextField(
            value = textState,
            onValueChange = onValueChange,
            label = label,
            placeholder = placeholder,
            enabled = descriptor.enabled ?: true,
            modifier = modifier
        )
    }
}

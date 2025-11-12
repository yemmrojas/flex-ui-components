package com.libs.flex.ui.flexui.components.infrastructure.adapter.atomic

import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.libs.flex.ui.flexui.model.AtomicDescriptor
import com.libs.flex.ui.flexui.model.ComponentEvent

/**
 * Renders a button component.
 *
 * This composable creates a button that emits Click events when pressed.
 * Supports both primary (filled) and secondary (outlined) button styles.
 *
 * Supported buttonStyle values:
 * - "secondary": OutlinedButton with border
 * - "primary" or any other value: Standard filled Button (default)
 *
 * @param descriptor Atomic descriptor containing button properties
 * @param onEvent Callback invoked when button is clicked
 * @param modifier Modifier to be applied to the Button
 *
 * Requirements: 14.1, 14.2, 14.3, 14.4, 14.5, 18.2
 */
@Composable
fun ButtonComponent(
    descriptor: AtomicDescriptor,
    onEvent: (ComponentEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val onClick = {
        onEvent(ComponentEvent.Click(descriptor.id, actionId = descriptor.actionId))
    }

    when (descriptor.buttonStyle) {
        "secondary" -> OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = descriptor.enabled ?: true
        ) {
            Text(descriptor.text ?: "Button")
        }

        else -> Button(
            onClick = onClick,
            modifier = modifier,
            enabled = descriptor.enabled ?: true
        ) {
            Text(descriptor.text ?: "Button")
        }
    }
}

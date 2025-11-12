package com.libs.flex.ui.flexui.components.infrastructure.adapter.atomic

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.libs.flex.ui.flexui.model.AtomicDescriptor
import com.libs.flex.ui.flexui.model.ComponentEvent

/**
 * Renders a dropdown select component.
 *
 * This composable creates an ExposedDropdownMenuBox with a text field and
 * dropdown menu populated from the options array. The selected value is
 * managed internally and emits Selection events when changed.
 *
 * @param descriptor Atomic descriptor containing options and placeholder
 * @param onEvent Callback invoked when an option is selected
 * @param modifier Modifier to be applied to the ExposedDropdownMenuBox
 *
 * Requirements: 12.1, 12.2, 12.3, 12.4, 12.5, 18.2, 18.3
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectComponent(
    descriptor: AtomicDescriptor,
    onEvent: (ComponentEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedValue by remember { mutableStateOf("") }

    val displayText = if (selectedValue.isEmpty()) {
        descriptor.placeholder ?: ""
    } else {
        descriptor.options?.find { it.value == selectedValue }?.label ?: selectedValue
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = descriptor.label?.let { { Text(it) } },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            descriptor.options?.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        selectedValue = option.value
                        expanded = false
                        onEvent(ComponentEvent.Selection(descriptor.id, selectedValue = option.value))
                    }
                )
            }
        }
    }
}

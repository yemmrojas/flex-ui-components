package com.libs.flex.ui.flexui.components.infrastructure.adapter.atomic

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.libs.flex.ui.flexui.model.AtomicDescriptor
import com.libs.flex.ui.flexui.model.ComponentEvent
import kotlinx.serialization.json.jsonPrimitive

/**
 * Renders a slider component with label and current value display from an AtomicDescriptor.
 *
 * Supports:
 * - Column layout with Slider and Text showing current value
 * - Slider value state management with remember and mutableStateOf
 * - Value range from min and max properties
 * - ValueChange event emission when slider value changes
 * - Label display from descriptor
 *
 * @param descriptor The atomic descriptor containing slider properties
 * @param onEvent Callback invoked when slider value changes
 * @param modifier Modifier to be applied to the Column
 *
 * Requirements: 13.1, 13.2, 13.3, 13.4, 13.5, 18.2
 */
@Composable
fun SliderCheckComponent(
    descriptor: AtomicDescriptor,
    onEvent: (ComponentEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialValue = descriptor.value?.jsonPrimitive?.content?.toFloatOrNull()
        ?: descriptor.min
        ?: 0f

    var sliderValue by remember { mutableFloatStateOf(initialValue) }

    val minValue = descriptor.min ?: 0f
    val maxValue = descriptor.max ?: 100f

    Column(modifier = modifier) {
        descriptor.label?.let { label ->
            Text(text = label)
        }

        Slider(
            value = sliderValue,
            onValueChange = { newValue ->
                sliderValue = newValue
                onEvent(ComponentEvent.ValueChange(descriptor.id, value = newValue))
            },
            valueRange = minValue..maxValue,
            modifier = Modifier.fillMaxWidth()
        )

        Text(text = "Value: ${sliderValue.toInt()}")
    }
}

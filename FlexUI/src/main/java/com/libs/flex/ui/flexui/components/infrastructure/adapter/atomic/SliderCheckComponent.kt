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
 * Renders a slider component with label and value display.
 *
 * This composable creates a Column containing a label, slider control, and
 * current value display. The slider value is managed internally and emits
 * ValueChange events when adjusted.
 *
 * @param descriptor Atomic descriptor containing slider range and initial value
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
            valueRange = (descriptor.min ?: 0f)..(descriptor.max ?: 100f),
            modifier = Modifier.fillMaxWidth()
        )

        Text(text = "Value: ${sliderValue.toInt()}")
    }
}

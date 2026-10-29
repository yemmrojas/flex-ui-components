package com.libs.flex.ui.flexui

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.libs.flex.ui.flexui.model.ComponentEvent

/**
 * Temporary bridge stub for JsonRenderer while implementing SDD Blocks 1-7.
 */
@Composable
public fun JsonRenderer(
    jsonString: String,
    modifier: Modifier = Modifier,
    onEvent: (ComponentEvent) -> Unit = {}
) {
    Box(modifier = modifier) {
        Text("FlexUI Runtime Initializing...")
    }
}

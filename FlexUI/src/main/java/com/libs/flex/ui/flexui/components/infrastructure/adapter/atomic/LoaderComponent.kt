package com.libs.flex.ui.flexui.components.infrastructure.adapter.atomic

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.libs.flex.ui.flexui.components.infrastructure.util.toColor
import com.libs.flex.ui.flexui.model.AtomicDescriptor

/**
 * Renders a loading indicator component.
 *
 * This composable creates a progress indicator that displays continuous
 * animation to indicate loading state. Supports both circular and linear styles.
 *
 * Supported loaderStyle values:
 * - "linear": LinearProgressIndicator (horizontal bar)
 * - "circular" or any other value: CircularProgressIndicator (default)
 *
 * @param descriptor Atomic descriptor containing loader style and color properties
 * @param modifier Modifier to be applied to the progress indicator
 *
 * Requirements: 16.1, 16.2, 16.3, 16.4, 16.5
 */
@Composable
fun LoaderComponent(
    descriptor: AtomicDescriptor,
    modifier: Modifier = Modifier
) {
    when (descriptor.loaderStyle) {
        "linear" -> LinearProgressIndicator(
            modifier = modifier,
            color = descriptor.color.toColor()
        )

        else -> CircularProgressIndicator(
            modifier = modifier,
            color = descriptor.color.toColor(),
            strokeWidth = descriptor.size?.dp ?: 4.dp
        )
    }
}

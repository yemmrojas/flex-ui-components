package com.libs.flex.ui.flexui.components.infrastructure.adapter.atomic

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.libs.flex.ui.flexui.model.AtomicDescriptor
import com.libs.flex.ui.flexui.styling.infrastructure.adapter.HexColorParser

/**
 * Renders a loading indicator component from an AtomicDescriptor.
 *
 * Supports:
 * - CircularProgressIndicator or LinearProgressIndicator based on loaderStyle
 * - Color application from descriptor if specified
 * - Size application to CircularProgressIndicator if specified
 * - Continuous animation
 *
 * @param descriptor The atomic descriptor containing loader properties
 * @param modifier Modifier to be applied to the progress indicator
 *
 * Requirements: 16.1, 16.2, 16.3, 16.4, 16.5
 */
@Composable
fun LoaderComponent(
    descriptor: AtomicDescriptor,
    modifier: Modifier = Modifier
) {
    val loaderColor = descriptor.color?.let { hexColor ->
        HexColorParser().parse(hexColor)
    } ?: Color.Unspecified

    when (descriptor.loaderStyle) {
        "linear" -> LinearProgressIndicator(
            modifier = modifier,
            color = loaderColor
        )

        else -> {
            val strokeWidth = descriptor.size?.dp ?: 4.dp

            CircularProgressIndicator(
                modifier = modifier,
                color = loaderColor,
                strokeWidth = strokeWidth
            )
        }
    }
}

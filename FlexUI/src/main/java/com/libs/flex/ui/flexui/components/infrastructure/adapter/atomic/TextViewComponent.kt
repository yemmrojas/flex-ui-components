package com.libs.flex.ui.flexui.components.infrastructure.adapter.atomic

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.libs.flex.ui.flexui.model.AtomicDescriptor
import com.libs.flex.ui.flexui.styling.infrastructure.adapter.HexColorParser
import javax.inject.Inject

/**
 * Renders a text display component from an AtomicDescriptor.
 *
 * Supports:
 * - Text content from descriptor.text
 * - Text styles: bold, semiBold, italic, normal
 * - Font size in scalable pixels
 * - Hex color parsing
 * - Max lines with ellipsis overflow
 *
 * @param descriptor The atomic descriptor containing text properties
 * @param modifier Modifier to be applied to the Text composable
 *
 * Requirements: 10.1, 10.2, 10.3, 10.4, 10.5
 */
@Composable
fun TextViewComponent(
    descriptor: AtomicDescriptor,
    modifier: Modifier = Modifier
) {
    val fontWeight = when (descriptor.textStyle) {
        "bold" -> FontWeight.Bold
        "semiBold" -> FontWeight.SemiBold
        else -> FontWeight.Normal
    }

    val fontStyle = when (descriptor.textStyle) {
        "italic" -> FontStyle.Italic
        else -> FontStyle.Normal
    }

    val textColor = descriptor.color?.let { hexColor ->
        HexColorParser().parse(hexColor)
    } ?: Color.Unspecified

    Text(
        text = descriptor.text ?: "",
        modifier = modifier,
        fontWeight = fontWeight,
        fontStyle = fontStyle,
        fontSize = descriptor.fontSize?.sp ?: 14.sp,
        color = textColor,
        maxLines = descriptor.maxLines ?: Int.MAX_VALUE,
        overflow = if (descriptor.maxLines != null) TextOverflow.Ellipsis else TextOverflow.Clip
    )
}

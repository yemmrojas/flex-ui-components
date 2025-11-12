package com.libs.flex.ui.flexui.components.infrastructure.adapter.atomic

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.libs.flex.ui.flexui.components.infrastructure.util.toColor
import com.libs.flex.ui.flexui.components.infrastructure.util.toFontStyle
import com.libs.flex.ui.flexui.components.infrastructure.util.toFontWeight
import com.libs.flex.ui.flexui.model.AtomicDescriptor

/**
 * Renders a text display component.
 *
 * This composable creates a Text that displays content with customizable
 * styling including font weight, style, size, color, and line limits.
 *
 * Supported textStyle values:
 * - "bold": Bold font weight
 * - "semiBold": Semi-bold font weight
 * - "italic": Italic font style
 * - "normal": Normal font weight and style (default)
 *
 * @param descriptor Atomic descriptor containing text and styling properties
 * @param modifier Modifier to be applied to the Text composable
 *
 * Requirements: 10.1, 10.2, 10.3, 10.4, 10.5
 */
@Composable
fun TextViewComponent(
    descriptor: AtomicDescriptor,
    modifier: Modifier = Modifier
) {
    Text(
        text = descriptor.text ?: "",
        modifier = modifier,
        fontWeight = descriptor.textStyle.toFontWeight(),
        fontStyle = descriptor.textStyle.toFontStyle(),
        fontSize = descriptor.fontSize?.sp ?: 14.sp,
        color = descriptor.color.toColor(),
        maxLines = descriptor.maxLines ?: Int.MAX_VALUE,
        overflow = if (descriptor.maxLines != null) TextOverflow.Ellipsis else TextOverflow.Clip
    )
}

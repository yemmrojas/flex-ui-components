package com.libs.flex.ui.flexui.components.infrastructure.adapter.container

import android.util.Log
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.libs.flex.ui.flexui.components.ComponentFactory
import com.libs.flex.ui.flexui.components.infrastructure.util.toHorizontalArrangement
import com.libs.flex.ui.flexui.components.infrastructure.util.toVerticalAlignment
import com.libs.flex.ui.flexui.model.ComponentEvent
import com.libs.flex.ui.flexui.model.LayoutDescriptor

/**
 * Renders a horizontal row layout container.
 *
 * This composable creates a Row that arranges child components horizontally
 * according to the arrangement and alignment properties specified in the descriptor.
 *
 * Supported arrangement values:
 * - "start": Align children to the start (left in LTR)
 * - "center": Center children horizontally
 * - "end": Align children to the end (right in LTR)
 * - "spaceBetween": Space children evenly with no space at edges
 * - "spaceAround": Space children evenly with half space at edges
 * - "spaceEvenly": Space children evenly with equal space at edges
 *
 * Supported alignment values:
 * - "top": Align children to the top
 * - "center": Center children vertically
 * - "bottom": Align children to the bottom
 *
 * @param descriptor Layout descriptor containing arrangement, alignment, and children
 * @param onEvent Callback invoked when child components trigger events
 * @param modifier Modifier to be applied to the Row
 * @param componentFactory Factory for creating child components
 *
 * Requirements: 4.1, 4.2, 4.3, 4.4, 4.5
 */
@Composable
fun HorizontalContainer(
    descriptor: LayoutDescriptor,
    onEvent: (ComponentEvent) -> Unit,
    modifier: Modifier = Modifier,
    componentFactory: ComponentFactory
) {
    Row(
        modifier = modifier,
        horizontalArrangement = descriptor.arrangement.toHorizontalArrangement(),
        verticalAlignment = descriptor.alignment.toVerticalAlignment()
    ) {
        descriptor.children.forEach { child ->
            componentFactory.CreateComponent(
                descriptor = child,
                onEvent = onEvent
            )
        }
    }
}

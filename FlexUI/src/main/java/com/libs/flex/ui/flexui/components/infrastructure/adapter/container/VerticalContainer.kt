package com.libs.flex.ui.flexui.components.infrastructure.adapter.container

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.libs.flex.ui.flexui.components.ComponentFactory
import com.libs.flex.ui.flexui.components.infrastructure.util.toHorizontalAlignment
import com.libs.flex.ui.flexui.components.infrastructure.util.toVerticalArrangement
import com.libs.flex.ui.flexui.model.ComponentEvent
import com.libs.flex.ui.flexui.model.LayoutDescriptor

/**
 * Renders a vertical column layout container.
 *
 * This composable creates a Column that arranges child components vertically
 * according to the arrangement and alignment properties specified in the descriptor.
 *
 * Supported arrangement values:
 * - "top": Align children to the top
 * - "center": Center children vertically
 * - "bottom": Align children to the bottom
 * - "spaceBetween": Space children evenly with no space at edges
 * - "spaceAround": Space children evenly with half space at edges
 * - "spaceEvenly": Space children evenly with equal space at edges
 *
 * Supported alignment values:
 * - "start": Align children to the start (left in LTR)
 * - "center": Center children horizontally
 * - "end": Align children to the end (right in LTR)
 *
 * @param descriptor Layout descriptor containing arrangement, alignment, and children
 * @param onEvent Callback invoked when child components trigger events
 * @param modifier Modifier to be applied to the Column
 * @param componentFactory Factory for creating child components
 *
 * Requirements: 3.1, 3.2, 3.3, 3.4, 3.5
 */
@Composable
fun VerticalContainer(
    descriptor: LayoutDescriptor,
    onEvent: (ComponentEvent) -> Unit,
    modifier: Modifier = Modifier,
    componentFactory: ComponentFactory
) {
    Column(
        modifier = modifier,
        verticalArrangement = descriptor.arrangement.toVerticalArrangement(),
        horizontalAlignment = descriptor.alignment.toHorizontalAlignment()
    ) {
        descriptor.children.forEach { child ->
            componentFactory.CreateComponent(
                descriptor = child,
                onEvent = onEvent
            )
        }
    }
}

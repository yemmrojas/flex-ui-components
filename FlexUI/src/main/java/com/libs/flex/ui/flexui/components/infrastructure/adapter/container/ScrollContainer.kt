package com.libs.flex.ui.flexui.components.infrastructure.adapter.container

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.libs.flex.ui.flexui.components.ComponentFactory
import com.libs.flex.ui.flexui.model.ComponentEvent
import com.libs.flex.ui.flexui.model.LayoutDescriptor

/**
 * Renders a scrollable container.
 *
 * This composable creates a scrollable container that can scroll either vertically
 * or horizontally based on the scrollDirection property. The scroll state is
 * maintained across recompositions.
 *
 * Supported scrollDirection values:
 * - "vertical" (default): Vertical scrolling with Column
 * - "horizontal": Horizontal scrolling with Row
 *
 * @param descriptor Layout descriptor containing scrollDirection and children
 * @param onEvent Callback invoked when child components trigger events
 * @param modifier Modifier to be applied to the scrollable container
 * @param componentFactory Factory for creating child components
 *
 * Requirements: 5.1, 5.2, 5.3, 5.4, 5.5
 */
@Composable
fun ScrollContainer(
    descriptor: LayoutDescriptor,
    onEvent: (ComponentEvent) -> Unit,
    modifier: Modifier = Modifier,
    componentFactory: ComponentFactory
) {
    val scrollState = rememberScrollState()
    val isHorizontal = descriptor.scrollDirection == "horizontal"

    if (isHorizontal) {
        Row(
            modifier = modifier.horizontalScroll(scrollState)
        ) {
            descriptor.children.forEach { child ->
                componentFactory.CreateComponent(
                    descriptor = child,
                    onEvent = onEvent
                )
            }
        }
    } else {
        Column(
            modifier = modifier.verticalScroll(scrollState)
        ) {
            descriptor.children.forEach { child ->
                componentFactory.CreateComponent(
                    descriptor = child,
                    onEvent = onEvent
                )
            }
        }
    }
}

package com.libs.flex.ui.flexui.components.infrastructure.adapter.container

import android.annotation.SuppressLint
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.libs.flex.ui.flexui.R
import com.libs.flex.ui.flexui.components.ComponentFactory
import com.libs.flex.ui.flexui.model.ComponentEvent
import com.libs.flex.ui.flexui.model.LayoutDescriptor

/**
 * Renders a Scaffold with a FloatingActionButton.
 *
 * This composable creates a Scaffold layout with a floating action button (FAB)
 * positioned according to the fabPosition property. The FAB emits a Click event
 * when pressed, using the actionId from the descriptor.
 *
 * Supported fabPosition values:
 * - "end" (default): Position FAB at the end (bottom-right in LTR)
 * - "center": Position FAB at the center bottom
 * - "start": Position FAB at the start (bottom-left in LTR)
 *
 * @param descriptor Layout descriptor containing fabPosition, fabIcon, actionId, and children
 * @param onEvent Callback invoked when FAB is clicked or child components trigger events
 * @param modifier Modifier to be applied to the Scaffold
 * @param componentFactory Factory for creating child components
 *
 * Requirements: 6.1, 6.2, 6.3, 6.4, 6.5
 */
@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun FloatingButtonContainer(
    descriptor: LayoutDescriptor,
    onEvent: (ComponentEvent) -> Unit,
    modifier: Modifier = Modifier,
    componentFactory: ComponentFactory
) {
    val fabPosition = when (descriptor.fabPosition) {
        "center" -> FabPosition.Center
        "start" -> FabPosition.Start
        "end" -> FabPosition.End
        else -> FabPosition.End
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                shape = CircleShape,
                onClick = {
                    onEvent(
                        ComponentEvent.Click(
                            componentId = descriptor.id,
                            actionId = descriptor.actionId
                        )
                    )
                }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_add),
                    contentDescription = descriptor.fabIcon ?: "Action"
                )
            }
        },
        floatingActionButtonPosition = fabPosition
    ) {  paddingValues ->
        // Render children in the content area
        descriptor.children.forEach { child ->
            componentFactory.CreateComponent(
                descriptor = child,
                onEvent = onEvent
            )
        }
    }
}

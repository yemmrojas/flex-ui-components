package com.libs.flex.ui.flexui.components.infrastructure.adapter.atomic

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.libs.flex.ui.flexui.components.infrastructure.util.toSnackbarDuration
import com.libs.flex.ui.flexui.components.infrastructure.util.toToastColor
import com.libs.flex.ui.flexui.model.AtomicDescriptor

/**
 * Renders a toast notification component.
 *
 * This composable creates a Snackbar notification with customizable duration
 * and styling based on toast type. The snackbar is displayed automatically
 * when the component is rendered.
 *
 * Supported duration values:
 * - "short": Brief display (default)
 * - "long": Extended display
 * - "indefinite": Remains until dismissed
 *
 * Supported toastType values:
 * - "success": Green background
 * - "error": Red background
 * - Any other value: Default theme color
 *
 * @param descriptor Atomic descriptor containing message, duration, and type
 * @param modifier Modifier to be applied to the SnackbarHost
 *
 * Requirements: 17.1, 17.2, 17.3, 17.4, 17.5
 */
@Composable
fun ToastComponent(
    descriptor: AtomicDescriptor,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(descriptor.text) {
        descriptor.text?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = descriptor.duration.toSnackbarDuration()
            )
        }
    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = modifier
    ) { snackbarData ->
        Snackbar(
            snackbarData = snackbarData,
            containerColor = descriptor.toastType.toToastColor()
        )
    }
}

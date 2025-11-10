package com.libs.flex.ui.flexui.components.infrastructure.adapter.atomic

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.libs.flex.ui.flexui.model.AtomicDescriptor

/**
 * Renders a toast notification component from an AtomicDescriptor.
 *
 * Supports:
 * - Snackbar with message text
 * - Duration mapping (short, long, indefinite) to SnackbarDuration
 * - Success styling (green accent) when toastType is "success"
 * - Error styling (red accent) when toastType is "error"
 * - SnackbarHost for proper positioning
 *
 * @param descriptor The atomic descriptor containing toast properties
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

    val duration = when (descriptor.duration) {
        "short" -> SnackbarDuration.Short
        "long" -> SnackbarDuration.Long
        "indefinite" -> SnackbarDuration.Indefinite
        else -> SnackbarDuration.Short
    }

    val containerColor = when (descriptor.toastType) {
        "success" -> Color(0xFF4CAF50)
        "error" -> Color(0xFFF44336)
        else -> Color.Unspecified
    }

    LaunchedEffect(descriptor.text) {
        descriptor.text?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = duration
            )
        }
    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = modifier
    ) { snackbarData ->
        Snackbar(
            snackbarData = snackbarData,
            containerColor = containerColor
        )
    }
}

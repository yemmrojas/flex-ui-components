package com.libs.flex.ui.flexui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.libs.flex.ui.flexui.R

/**
 * Displays an inline error placeholder for component rendering failures.
 *
 * This component is used when an individual component fails to render,
 * allowing the rest of the UI to continue functioning. It provides visual
 * feedback about the error without crashing the entire application.
 *
 * Design decisions:
 * - Uses Material 3 Card for consistent styling
 * - Error color scheme for immediate visual feedback
 * - Compact layout to minimize disruption
 * - Clear error icon and message
 *
 * @param message Error message to display
 * @param modifier Modifier to be applied to the error placeholder
 *
 * Example:
 * ```
 * ErrorPlaceholder(
 *     message = "Failed to render ButtonComponent",
 *     modifier = Modifier.fillMaxWidth()
 * )
 * ```
 */
@Composable
fun ErrorPlaceholder(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.ic_outline_error),
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

/**
 * Displays a full-screen error content for critical failures.
 *
 * This component is used when the entire JSON parsing or rendering process fails,
 * providing comprehensive error information to help with debugging. It's designed
 * to be used as the default error content in JsonRenderer.
 *
 * Design decisions:
 * - Centered layout for prominence
 * - Large error icon for immediate recognition
 * - Clear hierarchy: title → message
 * - Accessible text sizing and spacing
 * - Material 3 theming for consistency
 *
 * @param error The throwable that caused the failure
 * @param modifier Modifier to be applied to the error content
 *
 * Example:
 * ```
 * DefaultErrorContent(
 *     error = JsonParseException("Invalid JSON structure"),
 *     modifier = Modifier.fillMaxSize()
 * )
 * ```
 */
@Composable
fun DefaultErrorContent(
    error: Throwable,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_outline_error),
            contentDescription = "Error",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Failed to render UI",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error.message ?: "Unknown error",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

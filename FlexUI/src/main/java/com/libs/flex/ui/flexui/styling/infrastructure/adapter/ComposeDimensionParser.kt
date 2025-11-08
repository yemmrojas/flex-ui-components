package com.libs.flex.ui.flexui.styling.infrastructure.adapter

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.libs.flex.ui.flexui.styling.domain.ports.DimensionParserPort
import javax.inject.Inject

/**
 * Adapter for parsing dimension strings to Compose Dp values.
 *
 * Supports the following formats:
 * - Numeric with "dp" suffix (e.g., "16dp", "100dp")
 * - "match_parent" -> Dp.Infinity
 * - "wrap_content" -> Dp.Unspecified
 * - Plain numeric values (interpreted as dp)
 *
 * This implementation is specific to Jetpack Compose dimension system.
 */
class ComposeDimensionParser @Inject constructor() : DimensionParserPort {

    /**
     * Parses a dimension string to a Compose Dp value.
     *
     * @param dimension Dimension string (e.g., "16dp", "match_parent", "wrap_content")
     * @return Parsed Dp value, or 0.dp if parsing fails
     *
     * Example:
     * ```
     * val padding = parser.parse("16dp")        // 16.dp
     * val fullWidth = parser.parse("match_parent") // Dp.Infinity
     * val autoHeight = parser.parse("wrap_content") // Dp.Unspecified
     * ```
     */
    override fun parse(dimension: String): Dp {
        return when {
            dimension == "match_parent" -> Dp.Infinity
            dimension == "wrap_content" -> Dp.Unspecified
            dimension.endsWith("dp") -> {
                dimension.removeSuffix("dp").toIntOrNull()?.dp ?: 0.dp
            }

            else -> dimension.toIntOrNull()?.dp ?: 0.dp
        }
    }
}

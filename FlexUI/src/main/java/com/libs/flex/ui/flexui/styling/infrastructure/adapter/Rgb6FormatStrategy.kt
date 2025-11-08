package com.libs.flex.ui.flexui.styling.infrastructure.adapter

import androidx.compose.ui.graphics.Color
import com.libs.flex.ui.flexui.styling.domain.model.ColorConstants
import com.libs.flex.ui.flexui.styling.domain.ports.HexFormatStrategyPort
import com.libs.flex.ui.flexui.styling.infrastructure.util.createColor
import javax.inject.Inject

/**
 * Strategy for parsing 6-character RGB hex color format.
 *
 * Handles hex colors in the format "RRGGBB" where:
 * - RR: Red component (00-FF)
 * - GG: Green component (00-FF)
 * - BB: Blue component (00-FF)
 *
 * Examples:
 * - "FF0000" -> Red
 * - "00FF00" -> Green
 * - "0000FF" -> Blue
 * - "FFFFFF" -> White
 *
 * This strategy assumes the input has been preprocessed (trimmed, # removed, validated).
 */
class Rgb6FormatStrategy @Inject constructor() : HexFormatStrategyPort {

    /**
     * Checks if this strategy can parse the given hex string length.
     *
     * @param hexLength The length of the preprocessed hex string
     * @return true if length is 6, false otherwise
     */
    override fun canParse(hexLength: Int): Boolean =
        hexLength == ColorConstants.RGB6_LENGTH

    /**
     * Parses a 6-character hex string to a Compose Color.
     *
     * Extracts RGB components from the hex string and creates a Color
     * with full opacity (alpha = 255).
     *
     * @param hex Preprocessed 6-character hex string (e.g., "FF0000")
     * @return Parsed Color object with full opacity
     * @throws NumberFormatException if hex string cannot be parsed to integers
     *
     * Example:
     * ```
     * val strategy = Rgb6FormatStrategy()
     * val color = strategy.parse("FF0000") // Returns Color.Red
     * ```
     */
    override fun parse(hex: String): Color {
        val r = hex.substring(0, 2).toInt(16)
        val g = hex.substring(2, 4).toInt(16)
        val b = hex.substring(4, 6).toInt(16)
        return createColor(r, g, b)
    }
}

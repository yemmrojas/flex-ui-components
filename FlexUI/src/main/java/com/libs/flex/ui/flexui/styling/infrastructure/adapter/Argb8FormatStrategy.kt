package com.libs.flex.ui.flexui.styling.infrastructure.adapter

import androidx.compose.ui.graphics.Color
import com.libs.flex.ui.flexui.styling.domain.model.ColorConstants
import com.libs.flex.ui.flexui.styling.domain.ports.HexFormatStrategyPort
import com.libs.flex.ui.flexui.styling.infrastructure.util.createColor
import javax.inject.Inject

/**
 * Strategy for parsing 8-character ARGB hex color format.
 *
 * Handles hex colors in the format "AARRGGBB" where:
 * - AA: Alpha component (00-FF)
 * - RR: Red component (00-FF)
 * - GG: Green component (00-FF)
 * - BB: Blue component (00-FF)
 *
 * Examples:
 * - "80FF0000" -> Semi-transparent red (50% opacity)
 * - "FF00FF00" -> Fully opaque green
 * - "000000FF" -> Fully transparent blue
 * - "FFFFFFFF" -> Fully opaque white
 *
 * This strategy assumes the input has been preprocessed (trimmed, # removed, validated).
 */
class Argb8FormatStrategy @Inject constructor() : HexFormatStrategyPort {

    /**
     * Checks if this strategy can parse the given hex string length.
     *
     * @param hexLength The length of the preprocessed hex string
     * @return true if length is 8, false otherwise
     */
    override fun canParse(hexLength: Int): Boolean =
        hexLength == ColorConstants.ARGB8_LENGTH

    /**
     * Parses an 8-character hex string to a Compose Color.
     *
     * Extracts ARGB components from the hex string and creates a Color
     * with the specified alpha transparency.
     *
     * @param hex Preprocessed 8-character hex string (e.g., "80FF0000")
     * @return Parsed Color object with specified alpha transparency
     * @throws NumberFormatException if hex string cannot be parsed to integers
     *
     * Example:
     * ```
     * val strategy = Argb8FormatStrategy()
     * val color = strategy.parse("80FF0000") // Returns semi-transparent red
     * ```
     */
    override fun parse(hex: String): Color {
        val a = hex.substring(0, 2).toInt(16)
        val r = hex.substring(2, 4).toInt(16)
        val g = hex.substring(4, 6).toInt(16)
        val b = hex.substring(6, 8).toInt(16)
        return createColor(r, g, b, a)
    }
}

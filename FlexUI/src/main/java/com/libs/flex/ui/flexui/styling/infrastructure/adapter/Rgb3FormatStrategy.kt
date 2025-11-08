package com.libs.flex.ui.flexui.styling.infrastructure.adapter

import androidx.compose.ui.graphics.Color
import com.libs.flex.ui.flexui.styling.domain.model.ColorConstants
import com.libs.flex.ui.flexui.styling.domain.ports.HexFormatStrategyPort
import com.libs.flex.ui.flexui.styling.infrastructure.util.createColor
import javax.inject.Inject

/**
 * Strategy for parsing 3-character RGB hex color format.
 *
 * Handles hex colors in the format "RGB" where each character is doubled:
 * - R: Red component (0-F, doubled to 00-FF)
 * - G: Green component (0-F, doubled to 00-FF)
 * - B: Blue component (0-F, doubled to 00-FF)
 *
 * Examples:
 * - "F00" -> "FF0000" -> Red
 * - "0F0" -> "00FF00" -> Green
 * - "00F" -> "0000FF" -> Blue
 * - "FFF" -> "FFFFFF" -> White
 *
 * This strategy assumes the input has been preprocessed (trimmed, # removed, validated).
 */
class Rgb3FormatStrategy @Inject constructor() : HexFormatStrategyPort {

    /**
     * Checks if this strategy can parse the given hex string length.
     *
     * @param hexLength The length of the preprocessed hex string
     * @return true if length is 3, false otherwise
     */
    override fun canParse(hexLength: Int): Boolean =
        hexLength == ColorConstants.RGB3_LENGTH

    /**
     * Parses a 3-character hex string to a Compose Color.
     *
     * Each character is doubled to create the full RGB value.
     * For example, "F" becomes "FF" (255 in decimal).
     *
     * @param hex Preprocessed 3-character hex string (e.g., "F00")
     * @return Parsed Color object with full opacity
     * @throws NumberFormatException if hex string cannot be parsed to integers
     *
     * Example:
     * ```
     * val strategy = Rgb3FormatStrategy()
     * val color = strategy.parse("F00") // Returns Color.Red
     * ```
     */
    override fun parse(hex: String): Color {
        val r = hex[0].toString().repeat(2).toInt(16)
        val g = hex[1].toString().repeat(2).toInt(16)
        val b = hex[2].toString().repeat(2).toInt(16)
        return createColor(r, g, b)
    }
}

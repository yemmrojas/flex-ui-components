package com.libs.flex.ui.flexui.styling.domain.ports

import androidx.compose.ui.graphics.Color

/**
 * Port interface for hex color format parsing strategies.
 *
 * This interface defines the contract for parsing different hexadecimal color formats
 * (RGB3, RGB6, ARGB8) following the Strategy Pattern. Each implementation handles
 * a specific format length and parsing logic.
 *
 * Implementations should:
 * - Check format compatibility via [canParse]
 * - Parse preprocessed hex strings (no # prefix, trimmed, validated)
 * - Throw NumberFormatException for invalid hex values
 *
 * Example usage:
 * ```
 * val strategy = Rgb6FormatStrategy()
 * if (strategy.canParse(hex.length)) {
 *     val color = strategy.parse(hex)
 * }
 * ```
 */
interface HexFormatStrategyPort {

    /**
     * Determines if this strategy can handle the given hex string length.
     *
     * @param hexLength The length of the preprocessed hex string (without # prefix)
     * @return true if this strategy supports the given length, false otherwise
     */
    fun canParse(hexLength: Int): Boolean

    /**
     * Parses a preprocessed hex string to a Compose Color.
     *
     * This method assumes the input has been preprocessed:
     * - Trimmed of whitespace
     * - # prefix removed
     * - Validated to contain only hex characters
     *
     * @param hex Preprocessed hex string (e.g., "FF0000", "F00", "80FF0000")
     * @return Parsed Color object with normalized RGBA values
     * @throws NumberFormatException if the hex string cannot be parsed to integers
     */
    fun parse(hex: String): Color
}

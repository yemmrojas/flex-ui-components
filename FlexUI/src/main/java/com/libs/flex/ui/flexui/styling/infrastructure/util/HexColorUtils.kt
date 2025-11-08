package com.libs.flex.ui.flexui.styling.infrastructure.util

import androidx.compose.ui.graphics.Color
import com.libs.flex.ui.flexui.styling.domain.model.ColorConstants

/**
 * Preprocesses a hex color string by trimming whitespace and removing # prefix.
 *
 * @receiver Raw hex color string (e.g., " #FF0000 ", "#F00", "FF0000")
 * @return Preprocessed hex string without # prefix and whitespace (e.g., "FF0000", "F00")
 *
 * Example:
 * ```
 * " #FF0000 ".preprocessHex() // Returns "FF0000"
 * "#F00".preprocessHex()      // Returns "F00"
 * "FF0000".preprocessHex()    // Returns "FF0000"
 * "  ".preprocessHex()        // Returns ""
 * ```
 */
fun String.preprocessHex(): String {
    var hex = this.trim()
    if (hex.startsWith(ColorConstants.HEX_PREFIX)) {
        hex = hex.substring(1)
    }
    return hex
}

/**
 * Validates that a string contains only valid hexadecimal characters.
 *
 * @receiver Hex string to validate (should be preprocessed)
 * @return true if the string is non-empty and contains only valid hex characters (0-9, A-F, a-f)
 *
 * Example:
 * ```
 * "FF0000".isValidHex()  // Returns true
 * "F00".isValidHex()     // Returns true
 * "GG0000".isValidHex()  // Returns false (G is not a hex character)
 * "".isValidHex()        // Returns false (empty string)
 * "FF 00".isValidHex()   // Returns false (contains space)
 * ```
 */
fun String.isValidHex(): Boolean =
    this.isNotEmpty() && this.matches(ColorConstants.HEX_PATTERN)


/**
 * Creates a Compose Color from RGBA integer values with normalization.
 *
 * Automatically coerces values to the valid range (0-255) and normalizes
 * them to the 0.0-1.0 range required by Compose Color.
 *
 * @param r Red component (0-255)
 * @param g Green component (0-255)
 * @param b Blue component (0-255)
 * @param a Alpha component (0-255), defaults to 255 (fully opaque)
 * @return Compose Color object with normalized values
 *
 * Example:
 * ```
 * createColor(255, 0, 0)        // Returns Color.Red
 * createColor(0, 255, 0)        // Returns Color.Green
 * createColor(255, 0, 0, 128)   // Returns semi-transparent red
 * createColor(300, -10, 0)      // Returns Color.Red (values coerced to valid range)
 * ```
 */
fun createColor(
    r: Int,
    g: Int,
    b: Int,
    a: Int = ColorConstants.MAX_COLOR_VALUE
): Color = Color(
    red = r.coerceIn(ColorConstants.MIN_COLOR_VALUE, ColorConstants.MAX_COLOR_VALUE)
            / ColorConstants.COLOR_NORMALIZATION_FACTOR,
    green = g.coerceIn(ColorConstants.MIN_COLOR_VALUE, ColorConstants.MAX_COLOR_VALUE)
            / ColorConstants.COLOR_NORMALIZATION_FACTOR,
    blue = b.coerceIn(ColorConstants.MIN_COLOR_VALUE, ColorConstants.MAX_COLOR_VALUE)
            / ColorConstants.COLOR_NORMALIZATION_FACTOR,
    alpha = a.coerceIn(ColorConstants.MIN_COLOR_VALUE, ColorConstants.MAX_COLOR_VALUE)
            / ColorConstants.COLOR_NORMALIZATION_FACTOR
)

package com.libs.flex.ui.flexui.components.infrastructure.util

import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.libs.flex.ui.flexui.components.domain.model.FlexUIContentScaleImage
import com.libs.flex.ui.flexui.styling.domain.ports.ColorParserPort
import com.libs.flex.ui.flexui.styling.infrastructure.adapter.HexColorParser

private val hexColorParser: ColorParserPort by lazy { HexColorParser() }

private val HEX_COLOR_PATTERN = Regex("^[0-9A-Fa-f]{3}$|^[0-9A-Fa-f]{4}$|^[0-9A-Fa-f]{6}$|^[0-9A-Fa-f]{8}$")

private const val PREFIX = "#"

/**
 * Converts a textStyle string to FontWeight.
 *
 * Supported values:
 * - "bold": FontWeight.Bold
 * - "semiBold": FontWeight.SemiBold
 * - "normal": FontWeight.Normal (default)
 *
 * @receiver Nullable string representing the font weight
 * @return FontWeight value, defaults to Normal if string is null or unrecognized
 *
 * Example:
 * ```
 * "bold".toFontWeight()      // Returns FontWeight.Bold
 * "semiBold".toFontWeight()  // Returns FontWeight.SemiBold
 * null.toFontWeight()        // Returns FontWeight.Normal (default)
 * ```
 */
fun String?.toFontWeight(): FontWeight = when (this) {
    "bold" -> FontWeight.Bold
    "semiBold" -> FontWeight.SemiBold
    else -> FontWeight.Normal
}

/**
 * Converts a textStyle string to FontStyle.
 *
 * Supported values:
 * - "italic": FontStyle.Italic
 * - Any other value: FontStyle.Normal (default)
 *
 * @receiver Nullable string representing the font style
 * @return FontStyle value, defaults to Normal if string is null or not "italic"
 *
 * Example:
 * ```
 * "italic".toFontStyle()  // Returns FontStyle.Italic
 * "bold".toFontStyle()    // Returns FontStyle.Normal
 * null.toFontStyle()      // Returns FontStyle.Normal (default)
 * ```
 */
fun String?.toFontStyle(): FontStyle = when (this) {
    "italic" -> FontStyle.Italic
    else -> FontStyle.Normal
}

/**
 * Converts a hex color string to Compose Color.
 *
 * Parses hex color strings in various formats (RGB, ARGB, RRGGBB, AARRGGBB)
 * and returns a Compose Color. Automatically adds '#' prefix if missing.
 * Validates the hex format before parsing using a pre-compiled regex pattern.
 * Returns Color.Unspecified if invalid.
 *
 * Performance optimizations:
 * - Uses singleton HexColorParser instance (no allocation per call)
 * - Pre-compiled regex pattern (no recompilation per call)
 * - Early return for null/invalid inputs
 * - Minimal string manipulation
 *
 * Supported formats:
 * - 3 characters: RGB (e.g., "F00" or "#F00")
 * - 4 characters: ARGB (e.g., "8F00" or "#8F00")
 * - 6 characters: RRGGBB (e.g., "FF0000" or "#FF0000")
 * - 8 characters: AARRGGBB (e.g., "80FF0000" or "#80FF0000")
 *
 * @receiver Nullable hex color string
 * @return Color value, or Color.Unspecified if string is null or invalid
 *
 * Example:
 * ```
 * "#FF0000".toColor()  // Returns Color.Red
 * "FF0000".toColor()   // Returns Color.Red - adds # automatically
 * "F00".toColor()      // Returns Color.Red - adds # automatically
 * "invalid".toColor()  // Returns Color.Unspecified - invalid format
 * null.toColor()       // Returns Color.Unspecified
 * ```
 */
fun String?.toColor(): Color {
    if (this == null) return Color.Unspecified
    val hexValue = if (this.startsWith(PREFIX)) this.substring(1) else this

    if (!HEX_COLOR_PATTERN.matches(hexValue)) {
        return Color.Unspecified
    }

    val result = hexColorParser.parse("$PREFIX$hexValue")

    return if (result == Color.Transparent) Color.Unspecified else result
}

/**
 * Converts a contentScale string to ContentScale using FlexUIContentScaleImage enum.
 *
 * Supported values:
 * - "fillBounds": ContentScale.FillBounds
 * - "fit": ContentScale.Fit (default)
 * - "crop": ContentScale.Crop
 * - "inside": ContentScale.Inside
 * - "none": ContentScale.None
 *
 * @receiver Nullable string representing the content scale mode
 * @return ContentScale value, defaults to Fit if string is null or unrecognized
 *
 * Example:
 * ```
 * "crop".toContentScale()       // Returns ContentScale.Crop
 * "fillBounds".toContentScale() // Returns ContentScale.FillBounds
 * null.toContentScale()         // Returns ContentScale.Fit (default)
 * ```
 */
fun String?.toContentScale(): ContentScale {
    val scaleType = FlexUIContentScaleImage.fromValue(this)
    return when (scaleType) {
        FlexUIContentScaleImage.FILL_BOUNDS -> ContentScale.FillBounds
        FlexUIContentScaleImage.FIT -> ContentScale.Fit
        FlexUIContentScaleImage.CROP -> ContentScale.Crop
        FlexUIContentScaleImage.INSIDE -> ContentScale.Inside
        FlexUIContentScaleImage.NONE -> ContentScale.None
    }
}

/**
 * Converts a duration string to SnackbarDuration.
 *
 * Supported values:
 * - "short": SnackbarDuration.Short (default)
 * - "long": SnackbarDuration.Long
 * - "indefinite": SnackbarDuration.Indefinite
 *
 * @receiver Nullable string representing the snackbar duration
 * @return SnackbarDuration value, defaults to Short if string is null or unrecognized
 *
 * Example:
 * ```
 * "long".toSnackbarDuration()        // Returns SnackbarDuration.Long
 * "indefinite".toSnackbarDuration()  // Returns SnackbarDuration.Indefinite
 * null.toSnackbarDuration()          // Returns SnackbarDuration.Short (default)
 * ```
 */
fun String?.toSnackbarDuration(): SnackbarDuration = when (this) {
    "long" -> SnackbarDuration.Long
    "indefinite" -> SnackbarDuration.Indefinite
    else -> SnackbarDuration.Short
}

/**
 * Converts a toastType string to Color for toast styling.
 *
 * Supported values:
 * - "success": Green color (0xFF4CAF50)
 * - "error": Red color (0xFFF44336)
 * - Any other value: Color.Unspecified (uses default theme color)
 *
 * @receiver Nullable string representing the toast type
 * @return Color value for the toast container, or Color.Unspecified for default
 *
 * Example:
 * ```
 * "success".toToastColor()  // Returns Color(0xFF4CAF50)
 * "error".toToastColor()    // Returns Color(0xFFF44336)
 * null.toToastColor()       // Returns Color.Unspecified
 * ```
 */
fun String?.toToastColor(): Color = when (this) {
    "success" -> Color(0xFF4CAF50)
    "error" -> Color(0xFFF44336)
    else -> Color.Unspecified
}

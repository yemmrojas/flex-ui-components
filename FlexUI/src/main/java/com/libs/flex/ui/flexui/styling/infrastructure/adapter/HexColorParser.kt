package com.libs.flex.ui.flexui.styling.infrastructure.adapter

import androidx.compose.ui.graphics.Color
import com.libs.flex.ui.flexui.styling.domain.model.ColorConstants
import com.libs.flex.ui.flexui.styling.domain.ports.ColorParserPort
import com.libs.flex.ui.flexui.styling.domain.ports.HexFormatStrategyPort
import com.libs.flex.ui.flexui.styling.infrastructure.util.isValidHex
import com.libs.flex.ui.flexui.styling.infrastructure.util.preprocessHex
import javax.inject.Inject

/**
 * Adapter for parsing hexadecimal color strings to Compose Color objects.
 *
 * Supports hexadecimal color formats:
 * - #RGB (e.g., #F00 -> red)
 * - #RRGGBB (e.g., #FF0000 -> red)
 * - #AARRGGBB (e.g., #80FF0000 -> semi-transparent red)
 *
 * This implementation uses the Strategy Pattern to delegate format-specific
 * parsing to dedicated strategy classes, following the Single Responsibility Principle.
 *
 * This implementation uses pure Kotlin parsing without Android framework dependencies,
 * making it testable in JVM unit tests.
 *
 * ## Error Handling
 * This parser is designed to NEVER crash the application. Any invalid input
 * will gracefully return Color.Transparent instead of throwing exceptions.
 */
class HexColorParser @Inject constructor(
    private val strategies: List<@JvmSuppressWildcards HexFormatStrategyPort>
) : ColorParserPort {

    /**
     * Secondary constructor for testing and standalone usage.
     * Creates a parser with default strategy implementations.
     */
    constructor() : this(
        listOf(
            Rgb3FormatStrategy(),
            Rgb6FormatStrategy(),
            Argb8FormatStrategy()
        )
    )

    /**
     * Parses a hexadecimal color string to a Compose Color.
     *
     * This method is guaranteed to never throw exceptions. Any parsing error
     * will result in Color.Transparent being returned.
     *
     * Uses the Strategy Pattern to delegate format-specific parsing to
     * dedicated strategy implementations based on the hex string length.
     *
     * @param colorString Hexadecimal color string (e.g., "#FF0000", "#F00", "#80FF0000")
     * @return Parsed Color object, or Color.Transparent if parsing fails
     *
     * Example:
     * ```
     * val red = parser.parse("#FF0000")           // Color.Red
     * val transparentBlue = parser.parse("#8000FF") // Semi-transparent blue
     * val fallback = parser.parse("invalid")      // Color.Transparent (no crash)
     * val fallback2 = parser.parse("")            // Color.Transparent (no crash)
     * ```
     */
    override fun parse(colorString: String): Color {
        return try {
            parseWithStrategies(colorString)
        } catch (e: Exception) {
            Color.Transparent
        }
    }

    /**
     * Parses a hex color string using the appropriate strategy.
     *
     * Preprocessing steps:
     * 1. Validates the input is not blank
     * 2. Validates that input starts with # prefix (required)
     * 3. Trims whitespace and removes # prefix
     * 4. Validates hex characters
     * 5. Selects appropriate strategy based on length
     * 6. Delegates parsing to the selected strategy
     *
     * @param colorString Raw hex color string
     * @return Parsed Color object, or Color.Transparent if no strategy matches
     * @throws NumberFormatException if strategy parsing fails
     */
    private fun parseWithStrategies(colorString: String): Color {
        // Early validation: check for blank input
        if (colorString.isBlank()) {
            return Color.Transparent
        }

        // Validate: check for required # prefix (after trimming)
        val trimmed = colorString.trim()
        if (!trimmed.startsWith(ColorConstants.HEX_PREFIX)) {
            return Color.Transparent
        }

        // Preprocess: remove # prefix
        val hex = trimmed.preprocessHex()

        // Validate: check for valid hex characters
        if (!hex.isValidHex()) {
            return Color.Transparent
        }

        // Select strategy based on hex length
        val strategy = strategies.firstOrNull { it.canParse(hex.length) }
            ?: return Color.Transparent

        // Delegate parsing to selected strategy
        return strategy.parse(hex)
    }
}

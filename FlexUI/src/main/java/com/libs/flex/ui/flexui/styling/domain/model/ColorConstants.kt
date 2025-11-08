package com.libs.flex.ui.flexui.styling.domain.model

/**
 * Constants for hex color parsing.
 * Centralizes all magic strings and numbers used in color parsing operations.
 */
object ColorConstants {
    // Format lengths
    const val RGB3_LENGTH = 3
    const val RGB6_LENGTH = 6
    const val ARGB8_LENGTH = 8
    
    // Special characters
    const val HEX_PREFIX = "#"
    
    // Validation patterns
    val HEX_PATTERN = Regex("^[0-9A-Fa-f]+$")
    
    // Color component ranges
    const val MIN_COLOR_VALUE = 0
    const val MAX_COLOR_VALUE = 255
    
    // Normalization
    const val COLOR_NORMALIZATION_FACTOR = 255f
    const val FULL_ALPHA = 1f
}

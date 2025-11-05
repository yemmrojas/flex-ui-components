package com.libs.flex.ui.flexui.parser.domain.model

/**
 * Constants for validation rules.
 *
 * This object centralizes all valid values for component properties,
 * following the DRY principle and providing a single source of truth.
 *
 * Benefits:
 * - Easy to maintain and update
 * - Prevents typos and inconsistencies
 * - Facilitates testing
 * - Clear documentation of valid values
 */
object ValidationConstants {
    
    // Layout arrangement values
    val VALID_ARRANGEMENTS = setOf(
        "top",
        "center",
        "bottom",
        "spaceBetween",
        "spaceAround",
        "spaceEvenly",
        "start",
        "end"
    )
    
    // Layout alignment values
    val VALID_ALIGNMENTS = setOf(
        "start",
        "center",
        "end",
        "top",
        "bottom"
    )
    
    // Scroll direction values
    val VALID_SCROLL_DIRECTIONS = setOf(
        "vertical",
        "horizontal"
    )
    
    // Floating action button position values
    val VALID_FAB_POSITIONS = setOf(
        "start",
        "center",
        "end"
    )
    
    // Text style values
    val VALID_TEXT_STYLES = setOf(
        "bold",
        "semiBold",
        "italic",
        "normal"
    )
    
    // Input style values
    val VALID_INPUT_STYLES = setOf(
        "outlined",
        "filled",
        "standard"
    )
    
    // Button style values
    val VALID_BUTTON_STYLES = setOf(
        "primary",
        "secondary"
    )
    
    // Content scale values for images
    val VALID_CONTENT_SCALES = setOf(
        "fit",
        "crop",
        "fillWidth",
        "fillHeight"
    )
    
    // Loader style values
    val VALID_LOADER_STYLES = setOf(
        "circular",
        "linear"
    )
    
    // Regex for hex color validation (#RRGGBB or #AARRGGBB)
    val HEX_COLOR_REGEX = Regex("^#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})$")
}

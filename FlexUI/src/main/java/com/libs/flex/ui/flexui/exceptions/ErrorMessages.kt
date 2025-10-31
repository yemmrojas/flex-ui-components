package com.libs.flex.ui.flexui.exceptions

/**
 * Centralized error messages for FlexUI exceptions.
 *
 * This object contains all error message templates used throughout the FlexUI library,
 * providing a single source of truth for error messaging and facilitating localization
 * and maintenance.
 */
object ErrorMessages {
    internal const val ERROR_UNKNOWN_COMPONENT_TYPE = "Unknown component type: %s"
    internal const val ERROR_MISSING_PROPERTY = "Missing required property '%s' for component type '%s'"
    internal const val ERROR_VALIDATION_FAILED = "Validation failed: %s"
}

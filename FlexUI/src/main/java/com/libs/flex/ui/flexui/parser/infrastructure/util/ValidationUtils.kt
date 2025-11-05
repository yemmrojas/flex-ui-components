package com.libs.flex.ui.flexui.parser.infrastructure.util

/**
 * Utility functions for validation operations.
 *
 * This object provides reusable validation helpers to avoid code duplication
 * across different validators, following the DRY principle.
 */
object ValidationUtils {
    
    /**
     * Validates that a value is in a set of valid options.
     *
     * @param value The value to validate
     * @param validValues Set of valid values
     * @param propertyName Name of the property being validated
     * @param componentId ID of the component being validated
     * @return Error message if invalid, null if valid
     */
    fun validateEnum(
        value: String,
        validValues: Set<String>,
        propertyName: String,
        componentId: String
    ): String? {
        return if (value !in validValues) {
            "Component '$componentId' has invalid $propertyName value: '$value'. Valid values: ${validValues.joinToString()}"
        } else {
            null
        }
    }
    
    /**
     * Validates that a numeric value is positive.
     *
     * @param value The value to validate
     * @param propertyName Name of the property being validated
     * @param componentId ID of the component being validated
     * @return Error message if invalid, null if valid
     */
    fun validatePositive(
        value: Int,
        propertyName: String,
        componentId: String
    ): String? {
        return if (value <= 0) {
            "Component '$componentId' has invalid $propertyName value: $value. Must be greater than 0"
        } else {
            null
        }
    }
    
    /**
     * Validates that a numeric value is positive.
     *
     * @param value The value to validate
     * @param propertyName Name of the property being validated
     * @param componentId ID of the component being validated
     * @return Error message if invalid, null if valid
     */
    fun validatePositive(
        value: Float,
        propertyName: String,
        componentId: String
    ): String? {
        return if (value <= 0f) {
            "Component '$componentId' has invalid $propertyName value: $value. Must be greater than 0"
        } else {
            null
        }
    }
    
    /**
     * Validates that a color string matches hex format.
     *
     * @param color The color string to validate
     * @param componentId ID of the component being validated
     * @return Error message if invalid, null if valid
     */
    fun validateHexColor(
        color: String,
        componentId: String
    ): String? {
        return if (!color.matches(Regex("^#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})$"))) {
            "Component '$componentId' has invalid color format: '$color'. Expected hex format: #RRGGBB or #AARRGGBB"
        } else {
            null
        }
    }
    
    /**
     * Validates that a range has min < max.
     *
     * @param min Minimum value
     * @param max Maximum value
     * @param componentId ID of the component being validated
     * @return Error message if invalid, null if valid
     */
    fun validateRange(
        min: Float,
        max: Float,
        componentId: String
    ): String? {
        return if (min >= max) {
            "Component '$componentId' has invalid range: min ($min) must be less than max ($max)"
        } else {
            null
        }
    }
    
    /**
     * Validates that a required property is not null.
     *
     * @param value The value to check
     * @param propertyName Name of the property
     * @param componentType Type of the component
     * @param componentId ID of the component
     * @return Error message if null, null if valid
     */
    fun validateRequired(
        value: Any?,
        propertyName: String,
        componentType: String,
        componentId: String
    ): String? {
        return if (value == null) {
            "Component '$componentId' of type '$componentType' requires '$propertyName' property"
        } else {
            null
        }
    }
    
    /**
     * Validates that a list is not null and not empty.
     *
     * @param list The list to check
     * @param propertyName Name of the property
     * @param componentType Type of the component
     * @param componentId ID of the component
     * @return List of error messages (empty if valid)
     */
    fun validateNonEmptyList(
        list: List<*>?,
        propertyName: String,
        componentType: String,
        componentId: String
    ): List<String> {
        return buildList {
            if (list == null) {
                add("Component '$componentId' of type '$componentType' requires '$propertyName' property")
            } else if (list.isEmpty()) {
                add("Component '$componentId' of type '$componentType' has empty '$propertyName' array")
            }
        }
    }
}

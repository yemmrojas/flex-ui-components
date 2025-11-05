package com.libs.flex.ui.flexui.parser.infrastructure.util

/**
 * Extension functions for validation operations.
 *
 * These extension functions provide reusable validation helpers to avoid code duplication
 * across different validators, following the DRY principle and Kotlin idioms.
 *
 * Usage examples:
 * ```kotlin
 * // Validate enum value
 * val error = "primary".validateAsEnum(
 *     validValues = setOf("primary", "secondary"),
 *     propertyName = "buttonStyle",
 *     componentId = "btn1"
 * )
 *
 * // Validate positive number
 * val error = fontSize.validateAsPositive("fontSize", "text1")
 *
 * // Validate hex color
 * val error = "#FF5733".validateAsHexColor("color1")
 *
 * // Validate required property
 * val error = text.validateAsRequired("text", "componentTextView", "text1")
 *
 * // Validate non-empty list
 * val errors = options.validateAsNonEmptyList("options", "componentSelect", "select1")
 *
 * // Validate range
 * val error = minValue.validateAsMinInRange(maxValue, "slider1")
 * ```
 */

/**
 * Validates that this string value is in a set of valid options.
 *
 * @param validValues Set of valid values
 * @param propertyName Name of the property being validated
 * @param componentId ID of the component being validated
 * @return Error message if invalid, null if valid
 */
fun String.validateAsEnum(
    validValues: Set<String>,
    propertyName: String,
    componentId: String
): String? {
    return if (this !in validValues) {
        "Component '$componentId' has invalid $propertyName value: '$this'. Valid values: ${validValues.joinToString()}"
    } else {
        null
    }
}

/**
 * Validates that this Int value is positive (> 0).
 *
 * @param propertyName Name of the property being validated
 * @param componentId ID of the component being validated
 * @return Error message if invalid, null if valid
 */
fun Int.validateAsPositive(
    propertyName: String,
    componentId: String
): String? {
    return if (this <= 0) {
        "Component '$componentId' has invalid $propertyName value: $this. Must be greater than 0"
    } else {
        null
    }
}

/**
 * Validates that this Float value is positive (> 0).
 *
 * @param propertyName Name of the property being validated
 * @param componentId ID of the component being validated
 * @return Error message if invalid, null if valid
 */
fun Float.validateAsPositive(
    propertyName: String,
    componentId: String
): String? {
    return if (this <= 0f) {
        "Component '$componentId' has invalid $propertyName value: $this. Must be greater than 0"
    } else {
        null
    }
}

/**
 * Validates that this string matches hex color format (#RRGGBB or #AARRGGBB).
 *
 * @param componentId ID of the component being validated
 * @return Error message if invalid, null if valid
 */
fun String.validateAsHexColor(
    componentId: String
): String? {
    return if (!this.matches(Regex("^#([0-9A-Fa-f]{6}|[0-9A-Fa-f]{8})$"))) {
        "Component '$componentId' has invalid color format: '$this'. Expected hex format: #RRGGBB or #AARRGGBB"
    } else {
        null
    }
}

/**
 * Validates that this range has min < max.
 *
 * @param max Maximum value to compare against
 * @param componentId ID of the component being validated
 * @return Error message if invalid, null if valid
 */
fun Float.validateAsMinInRange(
    max: Float,
    componentId: String
): String? {
    return if (this >= max) {
        "Component '$componentId' has invalid range: min ($this) must be less than max ($max)"
    } else {
        null
    }
}

/**
 * Validates that this value is not null (required property).
 *
 * @param propertyName Name of the property
 * @param componentType Type of the component
 * @param componentId ID of the component
 * @return Error message if null, null if valid
 */
fun Any?.validateAsRequired(
    propertyName: String,
    componentType: String,
    componentId: String
): String? {
    return if (this == null) {
        "Component '$componentId' of type '$componentType' requires '$propertyName' property"
    } else {
        null
    }
}

/**
 * Validates that this list is not null and not empty.
 *
 * @param propertyName Name of the property
 * @param componentType Type of the component
 * @param componentId ID of the component
 * @return List of error messages (empty if valid)
 */
fun List<*>?.validateAsNonEmptyList(
    propertyName: String,
    componentType: String,
    componentId: String
): List<String> {
    return buildList {
        if (this@validateAsNonEmptyList == null) {
            add("Component '$componentId' of type '$componentType' requires '$propertyName' property")
        } else if (this@validateAsNonEmptyList.isEmpty()) {
            add("Component '$componentId' of type '$componentType' has empty '$propertyName' array")
        }
    }
}

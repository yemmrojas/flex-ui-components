package com.libs.flex.ui.flexui.exceptions

/**
 * Base sealed class for all FlexUI-related exceptions.
 *
 * This hierarchy allows for exhaustive when expressions and type-safe error handling
 * throughout the FlexUI library.
 *
 * @param message Descriptive error message
 * @param cause Optional underlying cause of the exception
 */
sealed class FlexUIException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Exception thrown when JSON parsing fails.
 *
 * This exception is thrown by the Interpretation Module when the JSON string
 * cannot be parsed into a valid component hierarchy.
 *
 * @param message Description of the parsing error
 * @param cause The underlying parsing exception
 */
class JsonParseException(
    message: String,
    cause: Throwable? = null
) : FlexUIException(message, cause)

/**
 * Exception thrown when an unknown component type is encountered.
 *
 * This exception is thrown when the JSON contains a component type that is not
 * recognized by the ComponentMapper.
 *
 * @param type The unknown component type string from JSON
 */
class ComponentTypeNotFoundException(
    val type: String
) : FlexUIException(ErrorMessages.ERROR_UNKNOWN_COMPONENT_TYPE.format(type))

/**
 * Exception thrown when a required property is missing from a component descriptor.
 *
 * This exception is thrown during validation when a component is missing a property
 * that is required for its type.
 *
 * @param property The name of the missing property
 * @param componentType The type of component that requires the property
 */
class MissingPropertyException(
    val property: String,
    val componentType: String
) : FlexUIException(ErrorMessages.ERROR_MISSING_PROPERTY.format(property, componentType))

/**
 * Exception thrown when component validation fails.
 *
 * This exception aggregates multiple validation errors that occurred during
 * the validation phase.
 *
 * @param errors List of validation error messages
 */
class ValidationException(
    val errors: List<String>
) : FlexUIException(ErrorMessages.ERROR_VALIDATION_FAILED.format(errors.joinToString(", ")))

/**
 * Exception thrown when component rendering fails.
 *
 * This exception is thrown when a component cannot be rendered due to runtime errors
 * during the composition phase.
 *
 * @param message Description of the rendering error
 * @param cause The underlying exception that caused the rendering failure
 */
class RenderException(
    message: String,
    cause: Throwable? = null
) : FlexUIException(message, cause)

package com.libs.flex.ui.flexui.parser.domain.model

/**
 * Sealed class representing the result of component validation.
 *
 * This follows the Result pattern to provide type-safe validation outcomes
 * without throwing exceptions during the validation phase.
 */
sealed class ValidationResult {
    /**
     * Indicates successful validation with no errors.
     */
    data object Success : ValidationResult()

    /**
     * Indicates validation failure with a list of error messages.
     *
     * @property errors List of validation error messages describing what failed
     */
    data class Failure(val errors: List<String>) : ValidationResult()

    /**
     * Checks if the validation was successful.
     *
     * @return true if validation succeeded, false otherwise
     */
    fun isSuccess(): Boolean = this is Success

    /**
     * Checks if the validation failed.
     *
     * @return true if validation failed, false otherwise
     */
    fun isFailure(): Boolean = this is Failure

    /**
     * Gets the list of errors if validation failed.
     *
     * @return List of error messages, or empty list if validation succeeded
     */
    fun errorList(): List<String> = when (this) {
        is Success -> emptyList()
        is Failure -> errors
    }
}

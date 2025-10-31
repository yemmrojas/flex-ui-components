package com.libs.flex.ui.flexui.model

import kotlinx.serialization.Serializable

/**
 * Represents validation rules for input components.
 *
 * @property required Whether the field is required
 * @property minLength Minimum length for text input
 * @property maxLength Maximum length for text input
 * @property pattern Regex pattern for validation
 * @property errorMessage Custom error message to display when validation fails
 */
@Serializable
data class ValidationRules(
    val required: Boolean = false,
    val minLength: Int? = null,
    val maxLength: Int? = null,
    val pattern: String? = null,
    val errorMessage: String? = null
)

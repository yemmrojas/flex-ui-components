package com.libs.flex.ui.flexui.model

import kotlinx.serialization.Serializable

/**
 * Represents an option in a select/dropdown component.
 *
 * @property label Display text shown to the user
 * @property value Internal value used when the option is selected
 */
@Serializable
data class SelectOption(
    val label: String,
    val value: String
)

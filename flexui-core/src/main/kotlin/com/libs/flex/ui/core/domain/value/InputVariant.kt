package com.libs.flex.ui.core.domain.value

public enum class InputVariant {
    Outlined, Filled, Password;

    public companion object {
        public fun fromString(raw: String): InputVariant = when (raw.lowercase()) {
            "filled" -> Filled
            "password" -> Password
            else -> Outlined
        }
    }
}

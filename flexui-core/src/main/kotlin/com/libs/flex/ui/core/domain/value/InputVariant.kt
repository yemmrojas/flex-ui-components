package com.libs.flex.ui.core.domain.value

public sealed interface InputVariant {
    public object Outlined : InputVariant {
        override fun toString(): String = "Outlined"
    }
    public object Filled : InputVariant {
        override fun toString(): String = "Filled"
    }
    public object Underlined : InputVariant {
        override fun toString(): String = "Underlined"
    }
    public data class Custom(public val name: String) : InputVariant

    public companion object {
        public fun of(raw: String): InputVariant = when (raw.lowercase()) {
            "outlined" -> Outlined
            "filled" -> Filled
            "underlined" -> Underlined
            else -> Custom(raw)
        }
    }
}

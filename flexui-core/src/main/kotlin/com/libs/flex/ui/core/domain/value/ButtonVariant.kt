package com.libs.flex.ui.core.domain.value

public sealed interface ButtonVariant {
    public object Primary : ButtonVariant {
        override fun toString(): String = "Primary"
    }
    public object Secondary : ButtonVariant {
        override fun toString(): String = "Secondary"
    }
    public object Text : ButtonVariant {
        override fun toString(): String = "Text"
    }
    public data class Custom(public val name: String) : ButtonVariant

    public companion object {
        public fun of(raw: String): ButtonVariant = when (raw.lowercase()) {
            "primary" -> Primary
            "secondary" -> Secondary
            "text" -> Text
            else -> Custom(raw)
        }
    }
}

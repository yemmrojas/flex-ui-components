package com.libs.flex.ui.core.domain.value

public sealed interface FieldValue {
    public data class Text(public val value: String) : FieldValue
    public data class Bool(public val value: Boolean) : FieldValue
    public data class Selection(public val selectedIndices: Set<Int>) : FieldValue
    public data class Number(public val value: Double) : FieldValue
}

package com.libs.flex.ui.core.domain.value

public sealed interface PropertyValue {
    public data class Str(public val value: String) : PropertyValue
    public data class Num(public val value: Double) : PropertyValue
    public data class Bool(public val value: Boolean) : PropertyValue
    public data class ListVal(public val value: List<PropertyValue>) : PropertyValue
    public data class Obj(public val value: Map<String, PropertyValue>) : PropertyValue
}

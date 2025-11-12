package com.libs.flex.ui.flexui.components.domain.model

enum class FexUILayoutOrientation(val value: String) {
    START("start"),
    CENTER("center"),
    END("end"),
    TOP("top"),
    BOTTOM("bottom"),
    SPACE_BETWEEN("spaceBetween"),
    SPACE_AROUND("spaceAround"),
    SPACE_EVENLY("spaceEvenly");

    companion object {
        fun fromValue(value: String?): FexUILayoutOrientation {
            return entries.firstOrNull { it.value == value } ?: START
        }
    }
}
package com.libs.flex.ui.flexui.components.domain.model

enum class FlexUIContentScaleImage(val value: String) {
    FILL_BOUNDS("fillBounds"),
    FIT("fit"),
    CROP("crop"),
    INSIDE("inside"),
    NONE("none");

    companion object {
        fun fromValue(value: String?): FlexUIContentScaleImage {
            return entries.firstOrNull { it.value == value } ?: FIT
        }
    }
}

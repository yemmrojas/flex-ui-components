package com.libs.flex.ui.flexuicomponents.domain.model

/**
 * Represents the different demo types available in the app.
 */
enum class DemoType(
    val route: String,
    val title: String,
    val jsonFileName: String
) {
    FORM(
        route = "form",
        title = "Form Demo",
        jsonFileName = "sample_form.json"
    ),
    LIST(
        route = "list",
        title = "List Demo",
        jsonFileName = "sample_list.json"
    ),
    SLIDER(
        route = "slider",
        title = "Slider Demo",
        jsonFileName = "sample_slider.json"
    ),
    FLOATING_BUTTON(
        route = "fab",
        title = "Floating Button Demo",
        jsonFileName = "sample_floating_button.json"
    ),
    COMPLEX(
        route = "complex",
        title = "Complex UI Demo",
        jsonFileName = "sample_complex.json"
    );

    companion object {
        fun fromRoute(route: String): DemoType? = entries.find { it.route == route }
    }
}

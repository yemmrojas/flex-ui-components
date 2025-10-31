package com.libs.flex.ui.flexui.model

/**
 * Sealed class representing user interaction events with components.
 *
 * This hierarchy defines all possible events that can be triggered by user interactions
 * with UI components in the FlexUI system. Each event contains the component identifier,
 * timestamp, and event-specific data.
 *
 * @property componentId Unique identifier of the component that triggered the event
 * @property timestamp Unix timestamp in milliseconds when the event occurred
 */
sealed class ComponentEvent {
    abstract val componentId: String
    abstract val timestamp: Long

    /**
     * Event triggered when a user clicks on a component (e.g., Button, FloatingActionButton).
     *
     * @property componentId Unique identifier of the clicked component
     * @property timestamp Unix timestamp in milliseconds when the click occurred
     * @property actionId Optional action identifier associated with the click for routing purposes
     */
    data class Click(
        override val componentId: String,
        override val timestamp: Long = System.currentTimeMillis(),
        val actionId: String? = null
    ) : ComponentEvent()

    /**
     * Event triggered when a component's value changes (e.g., TextField, Slider, Checkbox).
     *
     * @property componentId Unique identifier of the component whose value changed
     * @property timestamp Unix timestamp in milliseconds when the value changed
     * @property value The new value of the component (can be String, Number, Boolean, etc.)
     */
    data class ValueChange(
        override val componentId: String,
        override val timestamp: Long = System.currentTimeMillis(),
        val value: Any?
    ) : ComponentEvent()

    /**
     * Event triggered when a user selects an option from a selection component (e.g., Dropdown, Select).
     *
     * @property componentId Unique identifier of the selection component
     * @property timestamp Unix timestamp in milliseconds when the selection occurred
     * @property selectedValue The value of the selected option
     */
    data class Selection(
        override val componentId: String,
        override val timestamp: Long = System.currentTimeMillis(),
        val selectedValue: String
    ) : ComponentEvent()
}

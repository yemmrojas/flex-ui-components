package com.libs.flex.ui.flexui.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Base sealed class for all component descriptors in the FlexUI system.
 * This represents the parsed structure of a component from JSON.
 */
@Serializable
sealed class ComponentDescriptor {
    abstract val id: String
    abstract val type: ComponentType
    abstract val style: StyleProperties?
}

/**
 * Descriptor for layout container components that can contain child components.
 *
 * @property id Unique identifier for the component
 * @property type Type of the layout container
 * @property style Styling properties for the container
 * @property children List of child component descriptors
 * @property arrangement Arrangement strategy for children (e.g., "top", "center", "spaceBetween")
 * @property alignment Alignment strategy for children (e.g., "start", "center", "end")
 * @property scrollDirection Direction of scrolling for scroll containers ("vertical" or "horizontal")
 * @property fabIcon Icon resource for floating action button
 * @property fabPosition Position of floating action button (e.g., "end", "center")
 * @property items Array of items for list or slider containers
 * @property itemTemplate Template descriptor for rendering list items
 * @property autoPlay Whether slider should auto-advance
 * @property autoPlayInterval Interval in milliseconds for auto-play
 */
@Serializable
data class LayoutDescriptor(
    override val id: String,
    override val type: ComponentType,
    override val style: StyleProperties? = null,
    val children: List<ComponentDescriptor> = emptyList(),
    val arrangement: String? = null,
    val alignment: String? = null,
    val scrollDirection: String? = null,
    val fabIcon: String? = null,
    val fabPosition: String? = null,
    val actionId: String? = null,
    val items: List<JsonElement>? = null,
    val itemTemplate: ComponentDescriptor? = null,
    val autoPlay: Boolean? = null,
    val autoPlayInterval: Long? = null
) : ComponentDescriptor()

/**
 * Descriptor for atomic components that don't contain children.
 *
 * @property id Unique identifier for the component
 * @property type Type of the atomic component
 * @property style Styling properties for the component
 * @property text Text content for the component
 * @property placeholder Placeholder text for input components
 * @property value Current value of the component
 * @property inputStyle Style variant for input components (e.g., "outlined", "filled")
 * @property textStyle Text style variant (e.g., "bold", "semiBold", "italic", "normal")
 * @property fontSize Font size in scalable pixels
 * @property color Text color in hexadecimal format
 * @property maxLines Maximum number of lines for text components
 * @property label Label text for the component
 * @property enabled Whether the component is enabled for interaction
 * @property checked Initial checked state for checkbox components
 * @property options List of options for select components
 * @property min Minimum value for slider components
 * @property max Maximum value for slider components
 * @property buttonStyle Button style variant (e.g., "primary", "secondary")
 * @property imageUrl URL for image components
 * @property contentScale Content scale mode for images (e.g., "fit", "crop", "fillWidth", "fillHeight")
 * @property loaderStyle Loader style variant (e.g., "circular", "linear")
 * @property size Size dimension for loader components
 * @property actionId Action identifier for click events
 * @property validation Validation rules for input components
 * @property toastType Toast type variant (e.g., "success", "error")
 * @property duration Toast duration (e.g., "short", "long", "indefinite")
 */
@Serializable
data class AtomicDescriptor(
    override val id: String,
    override val type: ComponentType,
    override val style: StyleProperties? = null,
    val text: String? = null,
    val placeholder: String? = null,
    val value: JsonElement? = null,
    val inputStyle: String? = null,
    val textStyle: String? = null,
    val fontSize: Int? = null,
    val color: String? = null,
    val maxLines: Int? = null,
    val label: String? = null,
    val enabled: Boolean? = true,
    val checked: Boolean? = null,
    val options: List<SelectOption>? = null,
    val min: Float? = null,
    val max: Float? = null,
    val buttonStyle: String? = null,
    val imageUrl: String? = null,
    val contentScale: String? = null,
    val loaderStyle: String? = null,
    val size: Int? = null,
    val actionId: String? = null,
    val validation: ValidationRules? = null,
    val toastType: String? = null,
    val duration: String? = null
) : ComponentDescriptor()

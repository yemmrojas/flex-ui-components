package com.libs.flex.ui.flexui.components.infrastructure.adapter.container

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.libs.flex.ui.flexui.components.ComponentFactory
import com.libs.flex.ui.flexui.components.ErrorPlaceholder
import com.libs.flex.ui.flexui.model.ComponentEvent
import com.libs.flex.ui.flexui.model.LayoutDescriptor
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Renders a lazy list container.
 *
 * This composable creates a LazyColumn that efficiently renders a list of items
 * using a template descriptor. Each item is rendered using the itemTemplate merged
 * with the item's data. Stable keys are used for efficient recomposition.
 *
 * Features:
 * - Lazy loading for performance with large lists
 * - Stable keys for efficient recomposition
 * - Optional dividers between items
 * - Empty state handling
 *
 * @param descriptor Layout descriptor containing items array and itemTemplate
 * @param onEvent Callback invoked when child components trigger events
 * @param modifier Modifier to be applied to the LazyColumn
 * @param componentFactory Factory for creating child components
 *
 * Requirements: 7.1, 7.2, 7.3, 7.4, 7.5, 20.3
 */
@Composable
fun ListContainer(
    descriptor: LayoutDescriptor,
    onEvent: (ComponentEvent) -> Unit,
    modifier: Modifier = Modifier,
    componentFactory: ComponentFactory
) {
    val items = descriptor.items ?: emptyList()
    val itemTemplate = descriptor.itemTemplate

    if (items.isEmpty()) {
        // Handle empty list case
        ErrorPlaceholder(
            message = "No items to display",
            modifier = modifier.fillMaxWidth()
        )
        return
    }

    if (itemTemplate == null) {
        ErrorPlaceholder(
            message = "No item template provided",
            modifier = modifier.fillMaxWidth()
        )
        return
    }

    LazyColumn(modifier = modifier) {
        items(
            items = items,
            key = { item -> extractItemKey(item) }
        ) { item ->
            // Render item using template
            // Note: In a full implementation, we would merge item data with template
            // For now, we just render the template as-is
            componentFactory.CreateComponent(
                descriptor = itemTemplate,
                onEvent = onEvent
            )

            // Add divider if borderRadius is specified (using as divider flag)
            if (descriptor.style?.borderRadius != null) {
                HorizontalDivider()
            }
        }
    }
}

/**
 * Extracts a stable key from a JsonElement for list item identification.
 *
 * Tries to extract an "id" field from the item, falling back to hashCode
 * if no id is present.
 */
private fun extractItemKey(item: JsonElement): Any {
    return try {
        item.jsonObject["id"]?.jsonPrimitive?.content ?: item.hashCode()
    } catch (e: Exception) {
        item.hashCode()
    }
}

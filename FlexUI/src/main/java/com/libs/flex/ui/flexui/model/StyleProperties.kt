package com.libs.flex.ui.flexui.model

import kotlinx.serialization.Serializable

/**
 * Represents padding values for all four sides of a component.
 *
 * @property start Padding at the start edge (left in LTR, right in RTL)
 * @property top Padding at the top edge
 * @property end Padding at the end edge (right in LTR, left in RTL)
 * @property bottom Padding at the bottom edge
 */
@Serializable
data class PaddingValues(
    val start: Int = 0,
    val top: Int = 0,
    val end: Int = 0,
    val bottom: Int = 0
)

/**
 * Represents styling properties that can be applied to any component.
 *
 * @property padding Padding values for the component
 * @property margin Margin values for the component
 * @property backgroundColor Background color in hexadecimal format (e.g., "#FFFFFF")
 * @property borderRadius Corner radius in dp
 * @property elevation Shadow elevation in dp
 * @property width Width dimension (e.g., "100dp", "match_parent", "wrap_content")
 * @property height Height dimension (e.g., "100dp", "match_parent", "wrap_content")
 * @property weight Weight for proportional sizing in Row/Column layouts (accepts Int or Float)
 */
@Serializable
data class StyleProperties(
    val padding: PaddingValues? = null,
    val margin: PaddingValues? = null,
    val backgroundColor: String? = null,
    val borderRadius: Int? = null,
    val elevation: Int? = null,
    val width: String? = null,
    val height: String? = null,
    val weight: Int? = null
)

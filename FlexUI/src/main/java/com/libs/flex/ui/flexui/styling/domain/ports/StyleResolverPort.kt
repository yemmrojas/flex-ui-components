package com.libs.flex.ui.flexui.styling.domain.ports

import androidx.compose.ui.Modifier
import com.libs.flex.ui.flexui.model.StyleProperties

/**
 * Port for applying style properties to Compose modifiers.
 *
 * This interface defines the contract for style resolution operations,
 * allowing different styling strategies to be implemented and composed.
 */
interface StyleResolverPort {

    /**
     * Applies style properties to a Modifier.
     *
     * @param modifier Base modifier to apply styles to
     * @param style StyleProperties object containing style configuration, or null
     * @return Modified Modifier with applied styles
     */
    fun applyStyles(
        modifier: Modifier,
        style: StyleProperties?
    ): Modifier
}

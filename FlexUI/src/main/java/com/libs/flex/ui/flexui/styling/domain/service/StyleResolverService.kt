package com.libs.flex.ui.flexui.styling.domain.service

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.libs.flex.ui.flexui.model.StyleProperties
import com.libs.flex.ui.flexui.styling.domain.ports.ColorParserPort
import com.libs.flex.ui.flexui.styling.domain.ports.DimensionParserPort
import com.libs.flex.ui.flexui.styling.domain.ports.StyleResolverPort
import javax.inject.Inject

/**
 * Domain service for applying style properties to Compose modifiers.
 *
 * This service orchestrates the application of various style properties
 * by delegating to specialized parsers (color, dimension) and applying
 * the results to the modifier chain.
 *
 * @property colorParser Port for parsing color strings
 * @property dimensionParser Port for parsing dimension strings
 */
class StyleResolverService @Inject constructor(
    private val colorParser: ColorParserPort,
    private val dimensionParser: DimensionParserPort
) : StyleResolverPort {

    /**
     * Applies style properties from a StyleProperties object to a Modifier.
     *
     * Handles null styles gracefully by returning the original modifier unchanged.
     * Applies styles in the following order:
     * 1. Padding
     * 2. Background color
     * 3. Width
     * 4. Height
     * 5. Border radius (clip)
     * 6. Elevation (shadow)
     *
     * @param modifier Base modifier to apply styles to
     * @param style StyleProperties object containing style configuration, or null
     * @return Modified Modifier with applied styles
     *
     * Example:
     * ```
     * val style = StyleProperties(
     *     padding = PaddingValues(start = 16, top = 8, end = 16, bottom = 8),
     *     backgroundColor = "#FFFFFF",
     *     borderRadius = 8,
     *     elevation = 4
     * )
     * val styledModifier = styleResolver.applyStyles(Modifier, style)
     * ```
     */
    override fun applyStyles(modifier: Modifier, style: StyleProperties?): Modifier {
        if (style == null) return modifier

        var result = modifier

        // Apply padding
        style.padding?.let { padding ->
            result = result.padding(
                start = padding.start.dp,
                top = padding.top.dp,
                end = padding.end.dp,
                bottom = padding.bottom.dp
            )
        }

        // Apply background color using injected color parser
        style.backgroundColor?.let { colorHex ->
            result = result.background(colorParser.parse(colorHex))
        }

        // Apply width using injected dimension parser
        style.width?.let { width ->
            result = result.width(dimensionParser.parse(width))
        }

        // Apply height using injected dimension parser
        style.height?.let { height ->
            result = result.height(dimensionParser.parse(height))
        }

        // Apply border radius (must be before shadow for proper clipping)
        style.borderRadius?.let { radius ->
            result = result.clip(RoundedCornerShape(radius.dp))
        }

        // Apply elevation (shadow)
        style.elevation?.let { elevation ->
            val shape = RoundedCornerShape(style.borderRadius?.dp ?: 0.dp)
            result = result.shadow(elevation.dp, shape)
        }

        return result
    }
}

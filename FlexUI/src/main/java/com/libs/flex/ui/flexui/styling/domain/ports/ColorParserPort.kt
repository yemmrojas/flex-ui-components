package com.libs.flex.ui.flexui.styling.domain.ports

import androidx.compose.ui.graphics.Color

/**
 * Port for parsing color strings to Compose Color objects.
 *
 * This interface defines the contract for color parsing operations,
 * allowing different implementations (hex parser, rgb parser, named colors, etc.)
 * to be used interchangeably.
 */
interface ColorParserPort {

    /**
     * Parses a color string to a Compose Color.
     *
     * @param colorString Color string in any supported format
     * @return Parsed Color object, or Color.Transparent if parsing fails
     */
    fun parse(colorString: String): Color
}

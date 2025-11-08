package com.libs.flex.ui.flexui.styling.domain.ports

import androidx.compose.ui.unit.Dp

/**
 * Port for parsing dimension strings to Compose Dp values.
 *
 * This interface defines the contract for dimension parsing operations,
 * allowing different implementations (dp parser, percentage parser, etc.)
 * to be used interchangeably.
 */
interface DimensionParserPort {

    /**
     * Parses a dimension string to a Compose Dp value.
     *
     * @param dimension Dimension string in any supported format
     * @return Parsed Dp value, or 0.dp if parsing fails
     */
    fun parse(dimension: String): Dp
}

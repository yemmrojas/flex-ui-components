package com.libs.flex.ui.flexui.components.infrastructure.util

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.FabPosition
import androidx.compose.ui.Alignment
import com.libs.flex.ui.flexui.components.domain.model.LayoutEnum

/**
 * Converts a fabPosition string to FabPosition enum.
 *
 * Supported values:
 * - "center": FabPosition.Center
 * - "start": FabPosition.Start
 * - "end": FabPosition.End (default)
 *
 * @receiver Nullable string representing the FAB position
 * @return FabPosition enum value, defaults to End if string is null or unrecognized
 *
 * Example:
 * ```
 * "center".toFabPosition()  // Returns FabPosition.Center
 * "start".toFabPosition()   // Returns FabPosition.Start
 * null.toFabPosition()      // Returns FabPosition.End (default)
 * ```
 */
fun String?.toFabPosition(): FabPosition = when (this) {
    LayoutEnum.CENTER.value -> FabPosition.Center
    LayoutEnum.START.value -> FabPosition.Start
    LayoutEnum.END.value -> FabPosition.End
    else -> FabPosition.End
}

/**
 * Converts a vertical arrangement string to Arrangement.Vertical.
 *
 * Supported values:
 * - "top": Arrangement.Top
 * - "center": Arrangement.Center
 * - "bottom": Arrangement.Bottom
 * - "spaceBetween": Arrangement.SpaceBetween
 * - "spaceAround": Arrangement.SpaceAround
 * - "spaceEvenly": Arrangement.SpaceEvenly
 *
 * @receiver Nullable string representing the vertical arrangement
 * @return Arrangement.Vertical value, defaults to Top if string is null or unrecognized
 *
 * Example:
 * ```
 * "center".toVerticalArrangement()       // Returns Arrangement.Center
 * "spaceBetween".toVerticalArrangement() // Returns Arrangement.SpaceBetween
 * null.toVerticalArrangement()           // Returns Arrangement.Top (default)
 * ```
 */
fun String?.toVerticalArrangement(): Arrangement.Vertical = when (this) {
    LayoutEnum.TOP.value -> Arrangement.Top
    LayoutEnum.CENTER.value -> Arrangement.Center
    LayoutEnum.BOTTOM.value -> Arrangement.Bottom
    LayoutEnum.SPACE_BETWEEN.value -> Arrangement.SpaceBetween
    LayoutEnum.SPACE_AROUND.value -> Arrangement.SpaceAround
    LayoutEnum.SPACE_EVENLY.value -> Arrangement.SpaceEvenly
    else -> Arrangement.Top
}

/**
 * Converts a horizontal arrangement string to Arrangement.Horizontal.
 *
 * Supported values:
 * - "start": Arrangement.Start
 * - "center": Arrangement.Center
 * - "end": Arrangement.End
 * - "spaceBetween": Arrangement.SpaceBetween
 * - "spaceAround": Arrangement.SpaceAround
 * - "spaceEvenly": Arrangement.SpaceEvenly
 *
 * @receiver Nullable string representing the horizontal arrangement
 * @return Arrangement.Horizontal value, defaults to Start if string is null or unrecognized
 *
 * Example:
 * ```
 * "center".toHorizontalArrangement()       // Returns Arrangement.Center
 * "spaceEvenly".toHorizontalArrangement()  // Returns Arrangement.SpaceEvenly
 * null.toHorizontalArrangement()           // Returns Arrangement.Start (default)
 * ```
 */
fun String?.toHorizontalArrangement(): Arrangement.Horizontal = when (this) {
    LayoutEnum.START.value -> Arrangement.Start
    LayoutEnum.CENTER.value -> Arrangement.Center
    LayoutEnum.END.value -> Arrangement.End
    LayoutEnum.SPACE_BETWEEN.value -> Arrangement.SpaceBetween
    LayoutEnum.SPACE_AROUND.value -> Arrangement.SpaceAround
    LayoutEnum.SPACE_EVENLY.value -> Arrangement.SpaceEvenly
    else -> Arrangement.Start
}

/**
 * Converts a horizontal alignment string to Alignment.Horizontal.
 *
 * Supported values:
 * - "start": Alignment.Start
 * - "center": Alignment.CenterHorizontally
 * - "end": Alignment.End
 *
 * @receiver Nullable string representing the horizontal alignment
 * @return Alignment.Horizontal value, defaults to Start if string is null or unrecognized
 *
 * Example:
 * ```
 * "center".toHorizontalAlignment()  // Returns Alignment.CenterHorizontally
 * "end".toHorizontalAlignment()     // Returns Alignment.End
 * null.toHorizontalAlignment()      // Returns Alignment.Start (default)
 * ```
 */
fun String?.toHorizontalAlignment(): Alignment.Horizontal = when (this) {
    LayoutEnum.START.value -> Alignment.Start
    LayoutEnum.CENTER.value -> Alignment.CenterHorizontally
    LayoutEnum.END.value -> Alignment.End
    else -> Alignment.Start
}

/**
 * Converts a vertical alignment string to Alignment.Vertical.
 *
 * Supported values:
 * - "top": Alignment.Top
 * - "center": Alignment.CenterVertically
 * - "bottom": Alignment.Bottom
 *
 * @receiver Nullable string representing the vertical alignment
 * @return Alignment.Vertical value, defaults to CenterVertically if string is null or unrecognized
 *
 * Example:
 * ```
 * "top".toVerticalAlignment()     // Returns Alignment.Top
 * "center".toVerticalAlignment()  // Returns Alignment.CenterVertically
 * null.toVerticalAlignment()      // Returns Alignment.CenterVertically (default)
 * ```
 */
fun String?.toVerticalAlignment(): Alignment.Vertical = when (this) {
    LayoutEnum.TOP.value -> Alignment.Top
    LayoutEnum.CENTER.value -> Alignment.CenterVertically
    LayoutEnum.BOTTOM.value -> Alignment.Bottom
    else -> Alignment.CenterVertically
}

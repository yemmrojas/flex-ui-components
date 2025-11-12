package com.libs.flex.ui.flexui.components.infrastructure.util

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.FabPosition
import androidx.compose.ui.Alignment
import com.libs.flex.ui.flexui.components.domain.model.FexUILayoutOrientation

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
    FexUILayoutOrientation.CENTER.value -> FabPosition.Center
    FexUILayoutOrientation.START.value -> FabPosition.Start
    FexUILayoutOrientation.END.value -> FabPosition.End
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
fun String?.toVerticalArrangement(): Arrangement.Vertical {
    val layoutOrientation = getLayoutOrientation(this)
    return when (layoutOrientation) {
        FexUILayoutOrientation.TOP -> Arrangement.Top
        FexUILayoutOrientation.CENTER -> Arrangement.Center
        FexUILayoutOrientation.BOTTOM -> Arrangement.Bottom
        FexUILayoutOrientation.SPACE_BETWEEN -> Arrangement.SpaceBetween
        FexUILayoutOrientation.SPACE_AROUND -> Arrangement.SpaceAround
        FexUILayoutOrientation.SPACE_EVENLY -> Arrangement.SpaceEvenly
        else -> Arrangement.Top
    }
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
fun String?.toHorizontalArrangement(): Arrangement.Horizontal {
    val layoutOrientation = getLayoutOrientation(this)
    return when (layoutOrientation) {
        FexUILayoutOrientation.START -> Arrangement.Start
        FexUILayoutOrientation.CENTER -> Arrangement.Center
        FexUILayoutOrientation.END -> Arrangement.End
        FexUILayoutOrientation.SPACE_BETWEEN -> Arrangement.SpaceBetween
        FexUILayoutOrientation.SPACE_AROUND -> Arrangement.SpaceAround
        FexUILayoutOrientation.SPACE_EVENLY -> Arrangement.SpaceEvenly
        else -> Arrangement.Start
    }
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
fun String?.toHorizontalAlignment(): Alignment.Horizontal {
    val layoutOrientation = getLayoutOrientation(this)
    return when (layoutOrientation) {
        FexUILayoutOrientation.START -> Alignment.Start
        FexUILayoutOrientation.CENTER -> Alignment.CenterHorizontally
        FexUILayoutOrientation.END -> Alignment.End
        else -> Alignment.Start
    }
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
fun String?.toVerticalAlignment(): Alignment.Vertical {
    val layoutOrientation = getLayoutOrientation(this)
    return when (layoutOrientation) {
        FexUILayoutOrientation.TOP -> Alignment.Top
        FexUILayoutOrientation.CENTER -> Alignment.CenterVertically
        FexUILayoutOrientation.BOTTOM -> Alignment.Bottom
        else -> Alignment.CenterVertically
    }
}

private fun getLayoutOrientation(value: String?): FexUILayoutOrientation {
    return FexUILayoutOrientation.fromValue(value)
}

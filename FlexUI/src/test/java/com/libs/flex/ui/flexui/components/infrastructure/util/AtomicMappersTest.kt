package com.libs.flex.ui.flexui.components.infrastructure.util

import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.junit.Test

/**
 * Unit tests for AtomicMappers extension functions.
 *
 * Tests the mapping of string values to Compose atomic component properties,
 * ensuring correct conversions and default values.
 */
class AtomicMappersTest {

    // toFontWeight tests
    @Test
    fun `toFontWeight should map bold to FontWeight Bold`() {
        // Given
        val textStyle = "bold"

        // When
        val result = textStyle.toFontWeight()

        // Then
        assert(result == FontWeight.Bold)
    }

    @Test
    fun `toFontWeight should map semiBold to FontWeight SemiBold`() {
        // Given
        val textStyle = "semiBold"

        // When
        val result = textStyle.toFontWeight()

        // Then
        assert(result == FontWeight.SemiBold)
    }

    @Test
    fun `toFontWeight should default to Normal for null`() {
        // Given
        val textStyle: String? = null

        // When
        val result = textStyle.toFontWeight()

        // Then
        assert(result == FontWeight.Normal)
    }

    @Test
    fun `toFontWeight should default to Normal for unknown value`() {
        // Given
        val textStyle = "unknown"

        // When
        val result = textStyle.toFontWeight()

        // Then
        assert(result == FontWeight.Normal)
    }

    // toFontStyle tests
    @Test
    fun `toFontStyle should map italic to FontStyle Italic`() {
        // Given
        val textStyle = "italic"

        // When
        val result = textStyle.toFontStyle()

        // Then
        assert(result == FontStyle.Italic)
    }

    @Test
    fun `toFontStyle should default to Normal for null`() {
        // Given
        val textStyle: String? = null

        // When
        val result = textStyle.toFontStyle()

        // Then
        assert(result == FontStyle.Normal)
    }

    @Test
    fun `toFontStyle should default to Normal for non-italic value`() {
        // Given
        val textStyle = "bold"

        // When
        val result = textStyle.toFontStyle()

        // Then
        assert(result == FontStyle.Normal)
    }

    // toColor tests
    @Test
    fun `toColor should parse valid hex color with hash`() {
        // Given
        val hexColor = "#FF0000"

        // When
        val result = hexColor.toColor()

        // Then
        assert(result == Color.Red)
    }

    @Test
    fun `toColor should parse short hex color`() {
        // Given
        val hexColor = "#F00"

        // When
        val result = hexColor.toColor()

        // Then
        assert(result == Color.Red)
    }

    @Test
    fun `toColor should parse hex with alpha channel`() {
        // Given
        val hexColor = "#80FF0000"

        // When
        val result = hexColor.toColor()

        // Then
        assert(kotlin.math.abs(result.alpha - 0.5f) < 0.01f) { "Expected alpha ~0.5, got ${result.alpha}" }
        assert(kotlin.math.abs(result.red - 1f) < 0.01f) { "Expected red ~1.0, got ${result.red}" }
        assert(kotlin.math.abs(result.green - 0f) < 0.01f) { "Expected green ~0.0, got ${result.green}" }
        assert(kotlin.math.abs(result.blue - 0f) < 0.01f) { "Expected blue ~0.0, got ${result.blue}" }
    }

    @Test
    fun `toColor should return Unspecified for null`() {
        // Given
        val hexColor: String? = null

        // When
        val result = hexColor.toColor()

        // Then
        assert(result == Color.Unspecified)
    }

    @Test
    fun `toColor should parse hex without hash by adding prefix automatically`() {
        // Given
        val hexColor = "FF0000"

        // When
        val result = hexColor.toColor()

        // Then
        assert(result == Color.Red)
    }

    @Test
    fun `toColor should parse short hex without hash`() {
        // Given
        val hexColor = "F00"

        // When
        val result = hexColor.toColor()

        // Then
        assert(result == Color.Red)
    }

    @Test
    fun `toColor should return Unspecified for invalid hex`() {
        // Given
        val hexColor = "invalid"

        // When
        val result = hexColor.toColor()

        // Then
        assert(result == Color.Unspecified)
    }

    // toContentScale tests
    @Test
    fun `toContentScale should map fillBounds to ContentScale FillBounds`() {
        // Given
        val contentScale = "fillBounds"

        // When
        val result = contentScale.toContentScale()

        // Then
        assert(result == ContentScale.FillBounds)
    }

    @Test
    fun `toContentScale should map fit to ContentScale Fit`() {
        // Given
        val contentScale = "fit"

        // When
        val result = contentScale.toContentScale()

        // Then
        assert(result == ContentScale.Fit)
    }

    @Test
    fun `toContentScale should map crop to ContentScale Crop`() {
        // Given
        val contentScale = "crop"

        // When
        val result = contentScale.toContentScale()

        // Then
        assert(result == ContentScale.Crop)
    }

    @Test
    fun `toContentScale should map inside to ContentScale Inside`() {
        // Given
        val contentScale = "inside"

        // When
        val result = contentScale.toContentScale()

        // Then
        assert(result == ContentScale.Inside)
    }

    @Test
    fun `toContentScale should map none to ContentScale None`() {
        // Given
        val contentScale = "none"

        // When
        val result = contentScale.toContentScale()

        // Then
        assert(result == ContentScale.None)
    }

    @Test
    fun `toContentScale should default to Fit for null`() {
        // Given
        val contentScale: String? = null

        // When
        val result = contentScale.toContentScale()

        // Then
        assert(result == ContentScale.Fit)
    }

    @Test
    fun `toContentScale should default to Fit for unknown value`() {
        // Given
        val contentScale = "unknown"

        // When
        val result = contentScale.toContentScale()

        // Then
        assert(result == ContentScale.Fit)
    }

    // toSnackbarDuration tests
    @Test
    fun `toSnackbarDuration should map short to SnackbarDuration Short`() {
        // Given
        val duration = "short"

        // When
        val result = duration.toSnackbarDuration()

        // Then
        assert(result == SnackbarDuration.Short)
    }

    @Test
    fun `toSnackbarDuration should map long to SnackbarDuration Long`() {
        // Given
        val duration = "long"

        // When
        val result = duration.toSnackbarDuration()

        // Then
        assert(result == SnackbarDuration.Long)
    }

    @Test
    fun `toSnackbarDuration should map indefinite to SnackbarDuration Indefinite`() {
        // Given
        val duration = "indefinite"

        // When
        val result = duration.toSnackbarDuration()

        // Then
        assert(result == SnackbarDuration.Indefinite)
    }

    @Test
    fun `toSnackbarDuration should default to Short for null`() {
        // Given
        val duration: String? = null

        // When
        val result = duration.toSnackbarDuration()

        // Then
        assert(result == SnackbarDuration.Short)
    }

    @Test
    fun `toSnackbarDuration should default to Short for unknown value`() {
        // Given
        val duration = "unknown"

        // When
        val result = duration.toSnackbarDuration()

        // Then
        assert(result == SnackbarDuration.Short)
    }

    // toToastColor tests
    @Test
    fun `toToastColor should map success to green color`() {
        // Given
        val toastType = "success"

        // When
        val result = toastType.toToastColor()

        // Then
        assert(result == Color(0xFF4CAF50))
    }

    @Test
    fun `toToastColor should map error to red color`() {
        // Given
        val toastType = "error"

        // When
        val result = toastType.toToastColor()

        // Then
        assert(result == Color(0xFFF44336))
    }

    @Test
    fun `toToastColor should return Unspecified for null`() {
        // Given
        val toastType: String? = null

        // When
        val result = toastType.toToastColor()

        // Then
        assert(result == Color.Unspecified)
    }

    @Test
    fun `toToastColor should return Unspecified for unknown value`() {
        // Given
        val toastType = "unknown"

        // When
        val result = toastType.toToastColor()

        // Then
        assert(result == Color.Unspecified)
    }
}

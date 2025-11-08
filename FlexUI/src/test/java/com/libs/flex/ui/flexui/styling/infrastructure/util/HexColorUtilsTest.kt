package com.libs.flex.ui.flexui.styling.infrastructure.util

import androidx.compose.ui.graphics.Color
import com.libs.flex.ui.flexui.styling.domain.model.ColorConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for hex color utility extension functions.
 *
 * Tests the shared utility functions for hex color parsing including:
 * - Hex string preprocessing (trim, remove #) - String.preprocessHex()
 * - Hex string validation - String.isValidHex()
 * - Color creation from RGBA values with normalization - createColor()
 */
class HexColorUtilsTest {
    
    // String.preprocessHex() Tests
    
    @Test
    fun `preprocessHex should remove hash prefix`() {
        // Given
        val input = provideHexWithHash()
        
        // When
        val result = input.preprocessHex()
        
        // Then
        assertEquals("FF0000", result)
    }
    
    @Test
    fun `preprocessHex should trim leading whitespace`() {
        // Given
        val input = provideHexWithLeadingWhitespace()
        
        // When
        val result = input.preprocessHex()
        
        // Then
        assertEquals("FF0000", result)
    }
    
    @Test
    fun `preprocessHex should trim trailing whitespace`() {
        // Given
        val input = provideHexWithTrailingWhitespace()
        
        // When
        val result = input.preprocessHex()
        
        // Then
        assertEquals("FF0000", result)
    }
    
    @Test
    fun `preprocessHex should trim whitespace and remove hash`() {
        // Given
        val input = provideHexWithWhitespaceAndHash()
        
        // When
        val result = input.preprocessHex()
        
        // Then
        assertEquals("FF0000", result)
    }
    
    @Test
    fun `preprocessHex should handle hex without hash`() {
        // Given
        val input = provideHexWithoutHash()
        
        // When
        val result = input.preprocessHex()
        
        // Then
        assertEquals("FF0000", result)
    }
    
    @Test
    fun `preprocessHex should handle short hex with hash`() {
        // Given
        val input = provideShortHexWithHash()
        
        // When
        val result = input.preprocessHex()
        
        // Then
        assertEquals("F00", result)
    }
    
    @Test
    fun `preprocessHex should handle 8-char hex with hash`() {
        // Given
        val input = provideLongHexWithHash()
        
        // When
        val result = input.preprocessHex()
        
        // Then
        assertEquals("80FF0000", result)
    }
    
    @Test
    fun `preprocessHex should return empty string for whitespace only`() {
        // Given
        val input = provideWhitespaceOnly()
        
        // When
        val result = input.preprocessHex()
        
        // Then
        assertEquals("", result)
    }
    
    @Test
    fun `preprocessHex should return empty string for hash only`() {
        // Given
        val input = provideHashOnly()
        
        // When
        val result = input.preprocessHex()
        
        // Then
        assertEquals("", result)
    }
    
    @Test
    fun `preprocessHex should handle empty string`() {
        // Given
        val input = provideEmptyString()
        
        // When
        val result = input.preprocessHex()
        
        // Then
        assertEquals("", result)
    }
    
    @Test
    fun `preprocessHex should handle lowercase hex`() {
        // Given
        val input = provideLowercaseHexWithHash()
        
        // When
        val result = input.preprocessHex()
        
        // Then
        assertEquals("ff0000", result)
    }
    
    @Test
    fun `preprocessHex should handle mixed case hex`() {
        // Given
        val input = provideMixedCaseHexWithHash()
        
        // When
        val result = input.preprocessHex()
        
        // Then
        assertEquals("FfAaBb", result)
    }
    
    // String.isValidHex() Tests
    
    @Test
    fun `isValidHex should return true for valid 6-char hex`() {
        // Given
        val hex = provideValidSixCharHex()
        
        // When
        val result = hex.isValidHex()
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `isValidHex should return true for valid 3-char hex`() {
        // Given
        val hex = provideValidThreeCharHex()
        
        // When
        val result = hex.isValidHex()
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `isValidHex should return true for valid 8-char hex`() {
        // Given
        val hex = provideValidEightCharHex()
        
        // When
        val result = hex.isValidHex()
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `isValidHex should return true for lowercase hex`() {
        // Given
        val hex = provideValidLowercaseHex()
        
        // When
        val result = hex.isValidHex()
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `isValidHex should return true for mixed case hex`() {
        // Given
        val hex = provideValidMixedCaseHex()
        
        // When
        val result = hex.isValidHex()
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `isValidHex should return true for all zeros`() {
        // Given
        val hex = provideAllZerosHex()
        
        // When
        val result = hex.isValidHex()
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `isValidHex should return true for all F`() {
        // Given
        val hex = provideAllFHex()
        
        // When
        val result = hex.isValidHex()
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `isValidHex should return false for empty string`() {
        // Given
        val hex = provideEmptyString()
        
        // When
        val result = hex.isValidHex()
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `isValidHex should return false for hex with hash`() {
        // Given
        val hex = provideHexWithHashNotPreprocessed()
        
        // When
        val result = hex.isValidHex()
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `isValidHex should return false for hex with spaces`() {
        // Given
        val hex = provideHexWithSpaces()
        
        // When
        val result = hex.isValidHex()
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `isValidHex should return false for invalid characters G`() {
        // Given
        val hex = provideHexWithInvalidCharG()
        
        // When
        val result = hex.isValidHex()
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `isValidHex should return false for invalid characters Z`() {
        // Given
        val hex = provideHexWithInvalidCharZ()
        
        // When
        val result = hex.isValidHex()
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `isValidHex should return false for special characters`() {
        // Given
        val hex = provideHexWithSpecialChars()
        
        // When
        val result = hex.isValidHex()
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `isValidHex should return false for whitespace only`() {
        // Given
        val hex = provideWhitespaceOnlyForValidation()
        
        // When
        val result = hex.isValidHex()
        
        // Then
        assertFalse(result)
    }
    
    // createColor() Tests
    
    @Test
    fun `createColor should create red color`() {
        // Given
        val r = provideMaxColorValue()
        val g = provideMinColorValue()
        val b = provideMinColorValue()
        
        // When
        val result = createColor(r, g, b)
        
        // Then
        assertEquals(Color.Red, result)
    }
    
    @Test
    fun `createColor should create green color`() {
        // Given
        val r = provideMinColorValue()
        val g = provideMaxColorValue()
        val b = provideMinColorValue()
        
        // When
        val result = createColor(r, g, b)
        
        // Then
        assertEquals(Color.Green, result)
    }
    
    @Test
    fun `createColor should create blue color`() {
        // Given
        val r = provideMinColorValue()
        val g = provideMinColorValue()
        val b = provideMaxColorValue()
        
        // When
        val result = createColor(r, g, b)
        
        // Then
        assertEquals(Color.Blue, result)
    }
    
    @Test
    fun `createColor should create black color`() {
        // Given
        val r = provideMinColorValue()
        val g = provideMinColorValue()
        val b = provideMinColorValue()
        
        // When
        val result = createColor(r, g, b)
        
        // Then
        assertEquals(Color.Black, result)
    }
    
    @Test
    fun `createColor should create white color`() {
        // Given
        val r = provideMaxColorValue()
        val g = provideMaxColorValue()
        val b = provideMaxColorValue()
        
        // When
        val result = createColor(r, g, b)
        
        // Then
        assertEquals(Color.White, result)
    }
    
    @Test
    fun `createColor should default alpha to full opacity`() {
        // Given
        val r = provideMaxColorValue()
        val g = provideMinColorValue()
        val b = provideMinColorValue()
        
        // When
        val result = createColor(r, g, b)
        
        // Then
        assertEquals(ColorConstants.FULL_ALPHA, result.alpha, 0.01f)
    }
    
    @Test
    fun `createColor should handle custom alpha value`() {
        // Given
        val r = provideMaxColorValue()
        val g = provideMinColorValue()
        val b = provideMinColorValue()
        val a = provideHalfAlphaValue()
        
        // When
        val result = createColor(r, g, b, a)
        
        // Then
        assertEquals(0.5f, result.alpha, 0.01f)
    }
    
    @Test
    fun `createColor should handle zero alpha`() {
        // Given
        val r = provideMaxColorValue()
        val g = provideMinColorValue()
        val b = provideMinColorValue()
        val a = provideMinColorValue()
        
        // When
        val result = createColor(r, g, b, a)
        
        // Then
        assertEquals(0f, result.alpha, 0.01f)
    }
    
    @Test
    fun `createColor should normalize color values correctly`() {
        // Given
        val r = 128
        val g = 64
        val b = 192
        
        // When
        val result = createColor(r, g, b)
        
        // Then
        assertEquals(0.502f, result.red, 0.01f)
        assertEquals(0.251f, result.green, 0.01f)
        assertEquals(0.753f, result.blue, 0.01f)
    }
    
    @Test
    fun `createColor should coerce red value above max to max`() {
        // Given
        val r = provideValueAboveMax()
        val g = provideMinColorValue()
        val b = provideMinColorValue()
        
        // When
        val result = createColor(r, g, b)
        
        // Then
        assertEquals(1f, result.red, 0.01f)
    }
    
    @Test
    fun `createColor should coerce green value above max to max`() {
        // Given
        val r = provideMinColorValue()
        val g = provideValueAboveMax()
        val b = provideMinColorValue()
        
        // When
        val result = createColor(r, g, b)
        
        // Then
        assertEquals(1f, result.green, 0.01f)
    }
    
    @Test
    fun `createColor should coerce blue value above max to max`() {
        // Given
        val r = provideMinColorValue()
        val g = provideMinColorValue()
        val b = provideValueAboveMax()
        
        // When
        val result = createColor(r, g, b)
        
        // Then
        assertEquals(1f, result.blue, 0.01f)
    }
    
    @Test
    fun `createColor should coerce alpha value above max to max`() {
        // Given
        val r = provideMaxColorValue()
        val g = provideMinColorValue()
        val b = provideMinColorValue()
        val a = provideValueAboveMax()
        
        // When
        val result = createColor(r, g, b, a)
        
        // Then
        assertEquals(1f, result.alpha, 0.01f)
    }
    
    @Test
    fun `createColor should coerce red value below min to min`() {
        // Given
        val r = provideValueBelowMin()
        val g = provideMinColorValue()
        val b = provideMinColorValue()
        
        // When
        val result = createColor(r, g, b)
        
        // Then
        assertEquals(0f, result.red, 0.01f)
    }
    
    @Test
    fun `createColor should coerce green value below min to min`() {
        // Given
        val r = provideMinColorValue()
        val g = provideValueBelowMin()
        val b = provideMinColorValue()
        
        // When
        val result = createColor(r, g, b)
        
        // Then
        assertEquals(0f, result.green, 0.01f)
    }
    
    @Test
    fun `createColor should coerce blue value below min to min`() {
        // Given
        val r = provideMinColorValue()
        val g = provideMinColorValue()
        val b = provideValueBelowMin()
        
        // When
        val result = createColor(r, g, b)
        
        // Then
        assertEquals(0f, result.blue, 0.01f)
    }
    
    @Test
    fun `createColor should coerce alpha value below min to min`() {
        // Given
        val r = provideMaxColorValue()
        val g = provideMinColorValue()
        val b = provideMinColorValue()
        val a = provideValueBelowMin()
        
        // When
        val result = createColor(r, g, b, a)
        
        // Then
        assertEquals(0f, result.alpha, 0.01f)
    }
    
    @Test
    fun `createColor should handle all values at boundary max`() {
        // Given
        val r = provideMaxColorValue()
        val g = provideMaxColorValue()
        val b = provideMaxColorValue()
        val a = provideMaxColorValue()
        
        // When
        val result = createColor(r, g, b, a)
        
        // Then
        assertEquals(1f, result.red, 0.01f)
        assertEquals(1f, result.green, 0.01f)
        assertEquals(1f, result.blue, 0.01f)
        assertEquals(1f, result.alpha, 0.01f)
    }
    
    @Test
    fun `createColor should handle all values at boundary min`() {
        // Given
        val r = provideMinColorValue()
        val g = provideMinColorValue()
        val b = provideMinColorValue()
        val a = provideMinColorValue()
        
        // When
        val result = createColor(r, g, b, a)
        
        // Then
        assertEquals(0f, result.red, 0.01f)
        assertEquals(0f, result.green, 0.01f)
        assertEquals(0f, result.blue, 0.01f)
        assertEquals(0f, result.alpha, 0.01f)
    }
    
    @Test
    fun `createColor should handle mid-range values`() {
        // Given
        val r = provideMidRangeValue()
        val g = provideMidRangeValue()
        val b = provideMidRangeValue()
        
        // When
        val result = createColor(r, g, b)
        
        // Then
        assertEquals(0.5f, result.red, 0.01f)
        assertEquals(0.5f, result.green, 0.01f)
        assertEquals(0.5f, result.blue, 0.01f)
    }
    
    // Provider Functions - preprocessHex inputs
    
    private fun provideHexWithHash() = "#FF0000"
    private fun provideHexWithLeadingWhitespace() = "  FF0000"
    private fun provideHexWithTrailingWhitespace() = "FF0000  "
    private fun provideHexWithWhitespaceAndHash() = "  #FF0000  "
    private fun provideHexWithoutHash() = "FF0000"
    private fun provideShortHexWithHash() = "#F00"
    private fun provideLongHexWithHash() = "#80FF0000"
    private fun provideWhitespaceOnly() = "   "
    private fun provideHashOnly() = "#"
    private fun provideEmptyString() = ""
    private fun provideLowercaseHexWithHash() = "#ff0000"
    private fun provideMixedCaseHexWithHash() = "#FfAaBb"
    
    // Provider Functions - isValidHex inputs
    
    private fun provideValidSixCharHex() = "FF0000"
    private fun provideValidThreeCharHex() = "F00"
    private fun provideValidEightCharHex() = "80FF0000"
    private fun provideValidLowercaseHex() = "ff0000"
    private fun provideValidMixedCaseHex() = "FfAaBb"
    private fun provideAllZerosHex() = "000000"
    private fun provideAllFHex() = "FFFFFF"
    private fun provideHexWithHashNotPreprocessed() = "#FF0000"
    private fun provideHexWithSpaces() = "FF 00 00"
    private fun provideHexWithInvalidCharG() = "GG0000"
    private fun provideHexWithInvalidCharZ() = "ZZ0000"
    private fun provideHexWithSpecialChars() = "@!$%^&"
    private fun provideWhitespaceOnlyForValidation() = "   "
    
    // Provider Functions - createColor inputs
    
    private fun provideMinColorValue() = ColorConstants.MIN_COLOR_VALUE
    private fun provideMaxColorValue() = ColorConstants.MAX_COLOR_VALUE
    private fun provideHalfAlphaValue() = 128
    private fun provideMidRangeValue() = 128
    private fun provideValueAboveMax() = 300
    private fun provideValueBelowMin() = -10
}

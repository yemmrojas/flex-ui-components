package com.libs.flex.ui.flexui.styling.infrastructure.adapter

import androidx.compose.ui.graphics.Color
import com.libs.flex.ui.flexui.styling.domain.model.ColorConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for Argb8FormatStrategy.
 *
 * Tests the 8-character ARGB hex format parsing strategy including:
 * - Strategy selection based on hex length
 * - Valid 8-character hex color parsing with alpha channel
 * - Exception handling for invalid input
 */
class Argb8FormatStrategyTest {

    @Test
    fun `canParse should return true for length 8`() {
        // Given
        val strategy = provideStrategy()
        val hexLength = ColorConstants.ARGB8_LENGTH
        
        // When
        val result = strategy.canParse(hexLength)
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `canParse should return false for length 3`() {
        // Given
        val strategy = provideStrategy()
        val hexLength = ColorConstants.RGB3_LENGTH
        
        // When
        val result = strategy.canParse(hexLength)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `canParse should return false for length 6`() {
        // Given
        val strategy = provideStrategy()
        val hexLength = ColorConstants.RGB6_LENGTH
        
        // When
        val result = strategy.canParse(hexLength)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `canParse should return false for length 0`() {
        // Given
        val strategy = provideStrategy()
        val hexLength = 0
        
        // When
        val result = strategy.canParse(hexLength)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `canParse should return false for length 7`() {
        // Given
        val strategy = provideStrategy()
        val hexLength = 7
        
        // When
        val result = strategy.canParse(hexLength)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `canParse should return false for length 9`() {
        // Given
        val strategy = provideStrategy()
        val hexLength = 9
        
        // When
        val result = strategy.canParse(hexLength)
        
        // Then
        assertFalse(result)
    }
    
    // Valid Color Parsing Tests with Alpha
    
    @Test
    fun `parse should convert 80FF0000 to semi-transparent red`() {
        // Given
        val strategy = provideStrategy()
        val hexString = provideSemiTransparentRedHex()
        
        // When
        val result = strategy.parse(hexString)
        
        // Then
        // 80FF0000 = ARGB(128, 255, 0, 0) = semi-transparent red
        assertEquals(1f, result.red, 0.01f)
        assertEquals(0f, result.green, 0.01f)
        assertEquals(0f, result.blue, 0.01f)
        assertEquals(0.502f, result.alpha, 0.01f)
    }
    
    @Test
    fun `parse should convert FF00FF00 to fully opaque green`() {
        // Given
        val strategy = provideStrategy()
        val hexString = provideOpaqueGreenHex()
        
        // When
        val result = strategy.parse(hexString)
        
        // Then
        // FF00FF00 = ARGB(255, 0, 255, 0) = fully opaque green
        assertEquals(0f, result.red, 0.01f)
        assertEquals(1f, result.green, 0.01f)
        assertEquals(0f, result.blue, 0.01f)
        assertEquals(1f, result.alpha, 0.01f)
    }
    
    @Test
    fun `parse should convert 000000FF to fully transparent blue`() {
        // Given
        val strategy = provideStrategy()
        val hexString = provideTransparentBlueHex()
        
        // When
        val result = strategy.parse(hexString)
        
        // Then
        // 000000FF = ARGB(0, 0, 0, 255) = fully transparent blue
        assertEquals(0f, result.red, 0.01f)
        assertEquals(0f, result.green, 0.01f)
        assertEquals(1f, result.blue, 0.01f)
        assertEquals(0f, result.alpha, 0.01f)
    }
    
    @Test
    fun `parse should convert FFFFFFFF to fully opaque white`() {
        // Given
        val strategy = provideStrategy()
        val hexString = provideOpaqueWhiteHex()
        
        // When
        val result = strategy.parse(hexString)
        
        // Then
        assertEquals(Color.White, result)
    }
    
    @Test
    fun `parse should convert 00000000 to fully transparent black`() {
        // Given
        val strategy = provideStrategy()
        val hexString = provideTransparentBlackHex()
        
        // When
        val result = strategy.parse(hexString)
        
        // Then
        assertEquals(Color.Transparent, result)
    }
    
    @Test
    fun `parse should handle lowercase hex correctly`() {
        // Given
        val strategy = provideStrategy()
        val hexString = provideLowercaseSemiTransparentRedHex()
        
        // When
        val result = strategy.parse(hexString)
        
        // Then
        // 80ff0000 = ARGB(128, 255, 0, 0) = semi-transparent red
        assertEquals(1f, result.red, 0.01f)
        assertEquals(0f, result.green, 0.01f)
        assertEquals(0f, result.blue, 0.01f)
        assertEquals(0.502f, result.alpha, 0.01f)
    }
    
    @Test
    fun `parse should handle mixed case hex correctly`() {
        // Given
        val strategy = provideStrategy()
        val hexString = provideMixedCaseHex()
        
        // When
        val result = strategy.parse(hexString)
        
        // Then
        // AaBbCcDd = ARGB(170, 187, 204, 221)
        assertEquals(0.733f, result.red, 0.01f)
        assertEquals(0.8f, result.green, 0.01f)
        assertEquals(0.867f, result.blue, 0.01f)
        assertEquals(0.667f, result.alpha, 0.01f)
    }
    
    @Test
    fun `parse should handle 25 percent alpha correctly`() {
        // Given
        val strategy = provideStrategy()
        val hexString = provide25PercentAlphaHex()
        
        // When
        val result = strategy.parse(hexString)
        
        // Then
        // 40FFFFFF = ARGB(64, 255, 255, 255) = 25% opaque white
        assertEquals(1f, result.red, 0.01f)
        assertEquals(1f, result.green, 0.01f)
        assertEquals(1f, result.blue, 0.01f)
        assertEquals(0.251f, result.alpha, 0.01f)
    }
    
    @Test
    fun `parse should handle 75 percent alpha correctly`() {
        // Given
        val strategy = provideStrategy()
        val hexString = provide75PercentAlphaHex()
        
        // When
        val result = strategy.parse(hexString)
        
        // Then
        // BF000000 = ARGB(191, 0, 0, 0) = 75% opaque black
        assertEquals(0f, result.red, 0.01f)
        assertEquals(0f, result.green, 0.01f)
        assertEquals(0f, result.blue, 0.01f)
        assertEquals(0.749f, result.alpha, 0.01f)
    }
    
    @Test
    fun `parse should handle mid-range color values with alpha`() {
        // Given
        val strategy = provideStrategy()
        val hexString = provideMidRangeHex()
        
        // When
        val result = strategy.parse(hexString)
        
        // Then
        // 80808080 = ARGB(128, 128, 128, 128) = semi-transparent gray
        assertEquals(0.502f, result.red, 0.01f)
        assertEquals(0.502f, result.green, 0.01f)
        assertEquals(0.502f, result.blue, 0.01f)
        assertEquals(0.502f, result.alpha, 0.01f)
    }
    
    @Test
    fun `parse should correctly extract ARGB components`() {
        // Given
        val strategy = provideStrategy()
        val hexString = provideCustomHex()
        
        // When
        val result = strategy.parse(hexString)
        
        // Then
        // 12345678 = ARGB(18, 52, 86, 120)
        assertEquals(0.204f, result.red, 0.01f)
        assertEquals(0.337f, result.green, 0.01f)
        assertEquals(0.471f, result.blue, 0.01f)
        assertEquals(0.071f, result.alpha, 0.01f)
    }
    
    // Exception Handling Tests
    
    @Test(expected = NumberFormatException::class)
    fun `parse should throw NumberFormatException for invalid hex characters`() {
        // Given
        val strategy = provideStrategy()
        val invalidHex = provideInvalidHexCharacters()
        
        // When
        strategy.parse(invalidHex)
        
        // Then - exception thrown
    }
    
    @Test(expected = StringIndexOutOfBoundsException::class)
    fun `parse should throw exception for string shorter than 8 characters`() {
        // Given
        val strategy = provideStrategy()
        val shortHex = provideShortHexString()
        
        // When
        strategy.parse(shortHex)
        
        // Then - exception thrown
    }
    
    @Test(expected = NumberFormatException::class)
    fun `parse should throw NumberFormatException for special characters`() {
        // Given
        val strategy = provideStrategy()
        val specialChars = provideSpecialCharacters()
        
        // When
        strategy.parse(specialChars)
        
        // Then - exception thrown
    }
    
    @Test(expected = NumberFormatException::class)
    fun `parse should throw NumberFormatException for non-hex letters`() {
        // Given
        val strategy = provideStrategy()
        val invalidLetters = provideNonHexLetters()
        
        // When
        strategy.parse(invalidLetters)
        
        // Then - exception thrown
    }
    
    // Provider Functions
    
    private fun provideStrategy() = Argb8FormatStrategy()
    
    // Valid hex string providers
    private fun provideSemiTransparentRedHex() = "80FF0000"
    private fun provideOpaqueGreenHex() = "FF00FF00"
    private fun provideTransparentBlueHex() = "000000FF"
    private fun provideOpaqueWhiteHex() = "FFFFFFFF"
    private fun provideTransparentBlackHex() = "00000000"
    private fun provideLowercaseSemiTransparentRedHex() = "80ff0000"
    private fun provideMixedCaseHex() = "AaBbCcDd"
    private fun provide25PercentAlphaHex() = "40FFFFFF"
    private fun provide75PercentAlphaHex() = "BF000000"
    private fun provideMidRangeHex() = "80808080"
    private fun provideCustomHex() = "12345678"
    
    // Invalid input providers
    private fun provideInvalidHexCharacters() = "GGGGGGGG"
    private fun provideShortHexString() = "FF0000"
    private fun provideSpecialCharacters() = "@!$%^&*("
    private fun provideNonHexLetters() = "XYZWXYZW"
}

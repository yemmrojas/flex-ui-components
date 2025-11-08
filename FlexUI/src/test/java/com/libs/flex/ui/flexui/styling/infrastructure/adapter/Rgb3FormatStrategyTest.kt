package com.libs.flex.ui.flexui.styling.infrastructure.adapter

import androidx.compose.ui.graphics.Color
import com.libs.flex.ui.flexui.styling.domain.model.ColorConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for Rgb3FormatStrategy.
 *
 * Tests the 3-character RGB hex format parsing strategy including:
 * - Strategy selection based on hex length
 * - Valid 3-character hex color parsing
 * - Exception handling for invalid input
 */
class Rgb3FormatStrategyTest {
    
    // Strategy Selection Tests
    
    @Test
    fun `canParse should return true for length 3`() {
        // Given
        val strategy = provideStrategy()
        val hexLength = ColorConstants.RGB3_LENGTH
        
        // When
        val result = strategy.canParse(hexLength)
        
        // Then
        assertTrue(result)
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
    fun `canParse should return false for length 8`() {
        // Given
        val strategy = provideStrategy()
        val hexLength = ColorConstants.ARGB8_LENGTH
        
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
    fun `canParse should return false for length 2`() {
        // Given
        val strategy = provideStrategy()
        val hexLength = 2
        
        // When
        val result = strategy.canParse(hexLength)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `canParse should return false for length 4`() {
        // Given
        val strategy = provideStrategy()
        val hexLength = 4
        
        // When
        val result = strategy.canParse(hexLength)
        
        // Then
        assertFalse(result)
    }
    
    // Valid Color Parsing Tests
    
    @Test
    fun `parse should convert F00 to red`() {
        // Given
        val strategy = provideStrategy()
        val hexString = provideRedHex()
        
        // When
        val result = strategy.parse(hexString)
        
        // Then
        assertEquals(Color.Red, result)
    }
    
    @Test
    fun `parse should convert 0F0 to green`() {
        // Given
        val strategy = provideStrategy()
        val hexString = provideGreenHex()
        
        // When
        val result = strategy.parse(hexString)
        
        // Then
        assertEquals(Color.Green, result)
    }
    
    @Test
    fun `parse should convert 00F to blue`() {
        // Given
        val strategy = provideStrategy()
        val hexString = provideBlueHex()
        
        // When
        val result = strategy.parse(hexString)
        
        // Then
        assertEquals(Color.Blue, result)
    }
    
    @Test
    fun `parse should convert 000 to black`() {
        // Given
        val strategy = provideStrategy()
        val hexString = provideBlackHex()
        
        // When
        val result = strategy.parse(hexString)
        
        // Then
        assertEquals(Color.Black, result)
    }
    
    @Test
    fun `parse should convert FFF to white`() {
        // Given
        val strategy = provideStrategy()
        val hexString = provideWhiteHex()
        
        // When
        val result = strategy.parse(hexString)
        
        // Then
        assertEquals(Color.White, result)
    }
    
    @Test
    fun `parse should handle lowercase hex correctly`() {
        // Given
        val strategy = provideStrategy()
        val hexString = provideLowercaseRedHex()
        
        // When
        val result = strategy.parse(hexString)
        
        // Then
        assertEquals(Color.Red, result)
    }
    
    @Test
    fun `parse should handle mixed case hex correctly`() {
        // Given
        val strategy = provideStrategy()
        val hexString = provideMixedCaseHex()
        
        // When
        val result = strategy.parse(hexString)
        
        // Then
        // FaB = FFAABB = RGB(255, 170, 187)
        assertEquals(1f, result.red, 0.01f)
        assertEquals(0.667f, result.green, 0.01f)
        assertEquals(0.733f, result.blue, 0.01f)
        assertEquals(1f, result.alpha, 0.01f)
    }
    
    @Test
    fun `parse should create color with full opacity`() {
        // Given
        val strategy = provideStrategy()
        val hexString = provideRedHex()
        
        // When
        val result = strategy.parse(hexString)
        
        // Then
        assertEquals(ColorConstants.FULL_ALPHA, result.alpha, 0.01f)
    }
    
    @Test
    fun `parse should handle mid-range color values`() {
        // Given
        val strategy = provideStrategy()
        val hexString = provideMidRangeHex()
        
        // When
        val result = strategy.parse(hexString)
        
        // Then
        // 888 = 888888 = RGB(136, 136, 136) = gray
        assertEquals(0.533f, result.red, 0.01f)
        assertEquals(0.533f, result.green, 0.01f)
        assertEquals(0.533f, result.blue, 0.01f)
    }
    
    @Test
    fun `parse should correctly double each character`() {
        // Given
        val strategy = provideStrategy()
        val hexString = provideCustomHex()
        
        // When
        val result = strategy.parse(hexString)
        
        // Then
        // 1A5 = 11AA55 = RGB(17, 170, 85)
        assertEquals(0.067f, result.red, 0.01f)
        assertEquals(0.667f, result.green, 0.01f)
        assertEquals(0.333f, result.blue, 0.01f)
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
    fun `parse should throw exception for string shorter than 3 characters`() {
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
    
    private fun provideStrategy() = Rgb3FormatStrategy()
    
    // Valid hex string providers
    private fun provideRedHex() = "F00"
    private fun provideGreenHex() = "0F0"
    private fun provideBlueHex() = "00F"
    private fun provideBlackHex() = "000"
    private fun provideWhiteHex() = "FFF"
    private fun provideLowercaseRedHex() = "f00"
    private fun provideMixedCaseHex() = "FaB"
    private fun provideMidRangeHex() = "888"
    private fun provideCustomHex() = "1A5"
    
    // Invalid input providers
    private fun provideInvalidHexCharacters() = "GGG"
    private fun provideShortHexString() = "FF"
    private fun provideSpecialCharacters() = "@!$"
    private fun provideNonHexLetters() = "XYZ"
}

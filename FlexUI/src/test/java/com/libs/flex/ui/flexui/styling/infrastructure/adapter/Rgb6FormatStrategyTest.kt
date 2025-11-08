package com.libs.flex.ui.flexui.styling.infrastructure.adapter

import androidx.compose.ui.graphics.Color
import com.libs.flex.ui.flexui.styling.domain.model.ColorConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for Rgb6FormatStrategy.
 *
 * Tests the 6-character RGB hex format parsing strategy including:
 * - Strategy selection based on hex length
 * - Valid 6-character hex color parsing
 * - Exception handling for invalid input
 */
class Rgb6FormatStrategyTest {
    
    // Strategy Selection Tests
    
    @Test
    fun `canParse should return true for length 6`() {
        // Given
        val strategy = provideStrategy()
        val hexLength = ColorConstants.RGB6_LENGTH
        
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
    fun `canParse should return false for length 5`() {
        // Given
        val strategy = provideStrategy()
        val hexLength = 5
        
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
    
    // Valid Color Parsing Tests
    
    @Test
    fun `parse should convert FF0000 to red`() {
        // Given
        val strategy = provideStrategy()
        val hexString = provideRedHex()
        
        // When
        val result = strategy.parse(hexString)
        
        // Then
        assertEquals(Color.Red, result)
    }
    
    @Test
    fun `parse should convert 00FF00 to green`() {
        // Given
        val strategy = provideStrategy()
        val hexString = provideGreenHex()
        
        // When
        val result = strategy.parse(hexString)
        
        // Then
        assertEquals(Color.Green, result)
    }
    
    @Test
    fun `parse should convert 0000FF to blue`() {
        // Given
        val strategy = provideStrategy()
        val hexString = provideBlueHex()
        
        // When
        val result = strategy.parse(hexString)
        
        // Then
        assertEquals(Color.Blue, result)
    }
    
    @Test
    fun `parse should convert 000000 to black`() {
        // Given
        val strategy = provideStrategy()
        val hexString = provideBlackHex()
        
        // When
        val result = strategy.parse(hexString)
        
        // Then
        assertEquals(Color.Black, result)
    }
    
    @Test
    fun `parse should convert FFFFFF to white`() {
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
        // FfAaBb = RGB(255, 170, 187)
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
        // 808080 = RGB(128, 128, 128) = gray
        assertEquals(0.502f, result.red, 0.01f)
        assertEquals(0.502f, result.green, 0.01f)
        assertEquals(0.502f, result.blue, 0.01f)
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
    fun `parse should throw exception for string shorter than 6 characters`() {
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
    
    // Provider Functions
    
    private fun provideStrategy() = Rgb6FormatStrategy()
    
    // Valid hex string providers
    private fun provideRedHex() = "FF0000"
    private fun provideGreenHex() = "00FF00"
    private fun provideBlueHex() = "0000FF"
    private fun provideBlackHex() = "000000"
    private fun provideWhiteHex() = "FFFFFF"
    private fun provideLowercaseRedHex() = "ff0000"
    private fun provideMixedCaseHex() = "FfAaBb"
    private fun provideMidRangeHex() = "808080"
    
    // Invalid input providers
    private fun provideInvalidHexCharacters() = "GGGGGG"
    private fun provideShortHexString() = "FF0"
    private fun provideSpecialCharacters() = "@!$%^&"
}

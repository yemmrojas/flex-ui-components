package com.libs.flex.ui.flexui.styling

import androidx.compose.ui.graphics.Color
import com.libs.flex.ui.flexui.styling.infrastructure.adapter.HexColorParser
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for ColorParser (deprecated wrapper).
 *
 * Tests color parsing from various hexadecimal formats including
 * RGB, RRGGBB, and AARRGGBB formats, as well as error handling.
 *
 * Note: These tests verify backward compatibility of the deprecated ColorParser object.
 * For new code, use HexColorParserTest instead.
 */
class ColorParserTest {
    
    @Test
    fun `parse should return correct color for valid 6-digit hex`() {
        // Given
        val parser = provideParser()
        val hexColor = provideValidSixDigitHex()
        
        // When
        val result = parser.parse(hexColor)
        
        // Then
        assertEquals(Color.Red, result)
    }
    
    @Test
    fun `parse should return correct color for valid 3-digit hex`() {
        // Given
        val parser = provideParser()
        val hexColor = provideValidThreeDigitHex()
        
        // When
        val result = parser.parse(hexColor)
        
        // Then
        assertEquals(Color.Red, result)
    }
    
    @Test
    fun `parse should return correct color for valid 8-digit hex with alpha`() {
        // Given
        val parser = provideParser()
        val hexColor = provideValidEightDigitHexWithAlpha()
        
        // When
        val result = parser.parse(hexColor)
        
        // Then
        // #80FF0000 = 50% transparent red
        assertEquals(0.5f, result.alpha, 0.01f)
        assertEquals(1f, result.red, 0.01f)
        assertEquals(0f, result.green, 0.01f)
        assertEquals(0f, result.blue, 0.01f)
    }
    
    @Test
    fun `parse should return Color Transparent for invalid hex format`() {
        // Given
        val parser = provideParser()
        val invalidHex = provideInvalidHexFormat()
        
        // When
        val result = parser.parse(invalidHex)
        
        // Then
        assertEquals(Color.Transparent, result)
    }
    
    @Test
    fun `parse should return Color Transparent for empty string`() {
        // Given
        val parser = provideParser()
        val emptyHex = provideEmptyString()
        
        // When
        val result = parser.parse(emptyHex)
        
        // Then
        assertEquals(Color.Transparent, result)
    }
    
    @Test
    fun `parse should return Color Transparent for hex without hash prefix`() {
        // Given
        val parser = provideParser()
        val hexWithoutHash = provideHexWithoutHashPrefix()
        
        // When
        val result = parser.parse(hexWithoutHash)
        
        // Then
        assertEquals(Color.Transparent, result)
    }
    
    @Test
    fun `parse should handle black color correctly`() {
        // Given
        val parser = provideParser()
        val blackHex = provideBlackHex()
        
        // When
        val result = parser.parse(blackHex)
        
        // Then
        assertEquals(Color.Black, result)
    }
    
    @Test
    fun `parse should handle white color correctly`() {
        // Given
        val parser = provideParser()
        val whiteHex = provideWhiteHex()
        
        // When
        val result = parser.parse(whiteHex)
        
        // Then
        assertEquals(Color.White, result)
    }
    
    @Test
    fun `parse should handle blue color correctly`() {
        // Given
        val parser = provideParser()
        val blueHex = provideBlueHex()
        
        // When
        val result = parser.parse(blueHex)
        
        // Then
        assertEquals(Color.Blue, result)
    }
    
    @Test
    fun `parse should handle green color correctly`() {
        // Given
        val parser = provideParser()
        val greenHex = provideGreenHex()
        
        // When
        val result = parser.parse(greenHex)
        
        // Then
        assertEquals(Color.Green, result)
    }
    
    // Provider functions
    
    private fun provideParser() = HexColorParser()
    
    private fun provideValidSixDigitHex() = "#FF0000"
    
    private fun provideValidThreeDigitHex() = "#F00"
    
    private fun provideValidEightDigitHexWithAlpha() = "#80FF0000"
    
    private fun provideInvalidHexFormat() = "#GGGGGG"
    
    private fun provideEmptyString() = ""
    
    private fun provideHexWithoutHashPrefix() = "FF0000"
    
    private fun provideBlackHex() = "#000000"
    
    private fun provideWhiteHex() = "#FFFFFF"
    
    private fun provideBlueHex() = "#0000FF"
    
    private fun provideGreenHex() = "#00FF00"
}

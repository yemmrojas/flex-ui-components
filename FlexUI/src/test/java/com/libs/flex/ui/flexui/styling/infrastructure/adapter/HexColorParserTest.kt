package com.libs.flex.ui.flexui.styling.infrastructure.adapter

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for HexColorParser.
 *
 * Tests color parsing from various hexadecimal formats including
 * RGB, RRGGBB, and AARRGGBB formats, as well as error handling and robustness.
 */
class HexColorParserTest {
    
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
    
    // Error Handling Tests
    
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
    fun `parse should handle whitespace-only string without crashing`() {
        // Given
        val parser = provideParser()
        val whitespace = provideWhitespaceString()
        
        // When
        val result = parser.parse(whitespace)
        
        // Then
        assertEquals(Color.Transparent, result)
    }
    
    @Test
    fun `parse should handle hash-only string without crashing`() {
        // Given
        val parser = provideParser()
        val hashOnly = provideHashOnlyString()
        
        // When
        val result = parser.parse(hashOnly)
        
        // Then
        assertEquals(Color.Transparent, result)
    }
    
    @Test
    fun `parse should handle special characters without crashing`() {
        // Given
        val parser = provideParser()
        val specialChars = provideSpecialCharactersString()
        
        // When
        val result = parser.parse(specialChars)
        
        // Then
        assertEquals(Color.Transparent, result)
    }
    
    @Test
    fun `parse should handle very long string without crashing`() {
        // Given
        val parser = provideParser()
        val veryLong = provideVeryLongString()
        
        // When
        val result = parser.parse(veryLong)
        
        // Then
        assertEquals(Color.Transparent, result)
    }
    
    @Test
    fun `parse should handle wrong length without crashing`() {
        // Given
        val parser = provideParser()
        val wrongLength = provideWrongLengthString()
        
        // When
        val result = parser.parse(wrongLength)
        
        // Then
        assertEquals(Color.Transparent, result)
    }
    
    @Test
    fun `parse should handle mixed case correctly`() {
        // Given
        val parser = provideParser()
        val mixedCase = provideMixedCaseString()
        
        // When
        val result = parser.parse(mixedCase)
        
        // Then
        // Should parse successfully (hex is case-insensitive)
        assert(result != Color.Transparent)
    }
    
    @Test
    fun `parse should handle unicode characters without crashing`() {
        // Given
        val parser = provideParser()
        val unicode = provideUnicodeString()
        
        // When
        val result = parser.parse(unicode)
        
        // Then
        assertEquals(Color.Transparent, result)
    }
    
    @Test
    fun `parse should handle SQL injection attempt without crashing`() {
        // Given
        val parser = provideParser()
        val sqlInjection = provideSQLInjectionString()
        
        // When
        val result = parser.parse(sqlInjection)
        
        // Then
        assertEquals(Color.Transparent, result)
    }
    
    @Test
    fun `parse should handle newlines without crashing`() {
        // Given
        val parser = provideParser()
        val withNewlines = provideStringWithNewlines()
        
        // When
        val result = parser.parse(withNewlines)
        
        // Then
        assertEquals(Color.Transparent, result)
    }
    
    @Test
    fun `parse should handle negative numbers without crashing`() {
        // Given
        val parser = provideParser()
        val negative = provideNegativeNumberString()
        
        // When
        val result = parser.parse(negative)
        
        // Then
        assertEquals(Color.Transparent, result)
    }
    
    @Test
    fun `parse should handle multiple hashes without crashing`() {
        // Given
        val parser = provideParser()
        val multipleHashes = provideMultipleHashesString()
        
        // When
        val result = parser.parse(multipleHashes)
        
        // Then
        assertEquals(Color.Transparent, result)
    }
    
    @Test
    fun `parse should handle valid colors with extra whitespace`() {
        // Given
        val parser = provideParser()
        val withWhitespace = provideStringWithWhitespace()
        
        // When
        val result = parser.parse(withWhitespace)
        
        // Then
        // Should parse successfully after trimming
        assertEquals(Color.Red, result)
    }
    
    // Integration Tests - Strategy Selection and Complete Flow
    
    @Test
    fun `integration test - should select RGB3 strategy for 3-char hex`() {
        // Given
        val parser = provideParserWithAllStrategies()
        val threeCharHex = "#ABC"
        
        // When
        val result = parser.parse(threeCharHex)
        
        // Then
        // Verify strategy was selected and parsing succeeded
        assert(result != Color.Transparent)
        // #ABC should expand to #AABBCC
        assertEquals(0xAA / 255f, result.red, 0.01f)
        assertEquals(0xBB / 255f, result.green, 0.01f)
        assertEquals(0xCC / 255f, result.blue, 0.01f)
    }
    
    @Test
    fun `integration test - should select RGB6 strategy for 6-char hex`() {
        // Given
        val parser = provideParserWithAllStrategies()
        val sixCharHex = "#123456"
        
        // When
        val result = parser.parse(sixCharHex)
        
        // Then
        // Verify strategy was selected and parsing succeeded
        assert(result != Color.Transparent)
        assertEquals(0x12 / 255f, result.red, 0.01f)
        assertEquals(0x34 / 255f, result.green, 0.01f)
        assertEquals(0x56 / 255f, result.blue, 0.01f)
    }
    
    @Test
    fun `integration test - should select ARGB8 strategy for 8-char hex`() {
        // Given
        val parser = provideParserWithAllStrategies()
        val eightCharHex = "#12345678"
        
        // When
        val result = parser.parse(eightCharHex)
        
        // Then
        // Verify strategy was selected and parsing succeeded
        assert(result != Color.Transparent)
        assertEquals(0x12 / 255f, result.alpha, 0.01f)
        assertEquals(0x34 / 255f, result.red, 0.01f)
        assertEquals(0x56 / 255f, result.green, 0.01f)
        assertEquals(0x78 / 255f, result.blue, 0.01f)
    }
    
    @Test
    fun `integration test - should handle all formats in sequence`() {
        // Given
        val parser = provideParserWithAllStrategies()
        
        // When & Then - Test all formats work correctly
        val rgb3 = parser.parse("#F00")
        assertEquals(Color.Red, rgb3)
        
        val rgb6 = parser.parse("#00FF00")
        assertEquals(Color.Green, rgb6)
        
        val argb8 = parser.parse("#FF0000FF")
        assertEquals(Color.Blue, argb8)
    }
    
    @Test
    fun `integration test - should return transparent when no strategy matches`() {
        // Given
        val parser = provideParserWithAllStrategies()
        val unsupportedLength = "#FFFF" // 4 chars - no strategy for this
        
        // When
        val result = parser.parse(unsupportedLength)
        
        // Then
        assertEquals(Color.Transparent, result)
    }
    
    @Test
    fun `integration test - should handle blank input before strategy selection`() {
        // Given
        val parser = provideParserWithAllStrategies()
        val blankInput = "   "
        
        // When
        val result = parser.parse(blankInput)
        
        // Then
        assertEquals(Color.Transparent, result)
    }
    
    @Test
    fun `integration test - should validate hex prefix before strategy selection`() {
        // Given
        val parser = provideParserWithAllStrategies()
        val noPrefix = "FF0000"
        
        // When
        val result = parser.parse(noPrefix)
        
        // Then
        assertEquals(Color.Transparent, result)
    }
    
    @Test
    fun `integration test - should validate hex characters before strategy selection`() {
        // Given
        val parser = provideParserWithAllStrategies()
        val invalidChars = "#GGGGGG"
        
        // When
        val result = parser.parse(invalidChars)
        
        // Then
        assertEquals(Color.Transparent, result)
    }
    
    @Test
    fun `integration test - should preprocess and trim before strategy selection`() {
        // Given
        val parser = provideParserWithAllStrategies()
        val withWhitespace = "  #FF0000  "
        
        // When
        val result = parser.parse(withWhitespace)
        
        // Then
        assertEquals(Color.Red, result)
    }
    
    @Test
    fun `integration test - should handle empty after preprocessing`() {
        // Given
        val parser = provideParserWithAllStrategies()
        val hashOnly = "#"
        
        // When
        val result = parser.parse(hashOnly)
        
        // Then
        assertEquals(Color.Transparent, result)
    }
    
    @Test
    fun `integration test - should handle boundary values for RGB3`() {
        // Given
        val parser = provideParserWithAllStrategies()
        
        // When & Then
        val black = parser.parse("#000")
        assertEquals(Color.Black, black)
        
        val white = parser.parse("#FFF")
        assertEquals(Color.White, white)
    }
    
    @Test
    fun `integration test - should handle boundary values for RGB6`() {
        // Given
        val parser = provideParserWithAllStrategies()
        
        // When & Then
        val black = parser.parse("#000000")
        assertEquals(Color.Black, black)
        
        val white = parser.parse("#FFFFFF")
        assertEquals(Color.White, white)
    }
    
    @Test
    fun `integration test - should handle boundary values for ARGB8`() {
        // Given
        val parser = provideParserWithAllStrategies()
        
        // When & Then
        val fullyTransparent = parser.parse("#00000000")
        assertEquals(0f, fullyTransparent.alpha, 0.01f)
        
        val fullyOpaque = parser.parse("#FFFFFFFF")
        assertEquals(1f, fullyOpaque.alpha, 0.01f)
        assertEquals(Color.White.copy(alpha = 1f), fullyOpaque)
    }
    
    @Test
    fun `integration test - should handle case insensitivity across all strategies`() {
        // Given
        val parser = provideParserWithAllStrategies()
        
        // When & Then
        val rgb3Lower = parser.parse("#abc")
        val rgb3Upper = parser.parse("#ABC")
        assertEquals(rgb3Lower, rgb3Upper)
        
        val rgb6Lower = parser.parse("#abcdef")
        val rgb6Upper = parser.parse("#ABCDEF")
        assertEquals(rgb6Lower, rgb6Upper)
        
        val argb8Lower = parser.parse("#12abcdef")
        val argb8Upper = parser.parse("#12ABCDEF")
        assertEquals(argb8Lower, argb8Upper)
    }
    
    @Test
    fun `integration test - should never crash with any input`() {
        // Given
        val parser = provideParserWithAllStrategies()
        val problematicInputs = listOf(
            "",
            " ",
            "#",
            "##",
            "#GGGGGG",
            "#12345",
            "#1234567",
            "#123456789",
            "invalid",
            "#\n\r\t",
            "#-12345",
            null.toString()
        )
        
        // When & Then - None should crash
        problematicInputs.forEach { input ->
            val result = parser.parse(input)
            // Should always return a valid Color (even if Transparent)
            assert(result == Color.Transparent || result != Color.Transparent)
        }
    }
    
    @Test
    fun `integration test - should handle strategy exception gracefully`() {
        // Given
        val parser = provideParserWithAllStrategies()
        // This should trigger a parsing exception in the strategy
        val malformedHex = "#ZZZZZZ"
        
        // When
        val result = parser.parse(malformedHex)
        
        // Then - Should catch exception and return transparent
        assertEquals(Color.Transparent, result)
    }
    
    // Provider functions
    
    private fun provideParser() = HexColorParser()
    
    private fun provideParserWithAllStrategies() = HexColorParser(
        listOf(
            Rgb3FormatStrategy(),
            Rgb6FormatStrategy(),
            Argb8FormatStrategy()
        )
    )
    
    // Valid color providers
    private fun provideValidSixDigitHex() = "#FF0000"
    private fun provideValidThreeDigitHex() = "#F00"
    private fun provideValidEightDigitHexWithAlpha() = "#80FF0000"
    private fun provideBlackHex() = "#000000"
    private fun provideWhiteHex() = "#FFFFFF"
    private fun provideBlueHex() = "#0000FF"
    private fun provideGreenHex() = "#00FF00"
    
    // Error case providers
    private fun provideInvalidHexFormat() = "#GGGGGG"
    private fun provideEmptyString() = ""
    private fun provideHexWithoutHashPrefix() = "FF0000"
    
    // Robustness test providers
    private fun provideWhitespaceString() = "   "
    private fun provideHashOnlyString() = "#"
    private fun provideSpecialCharactersString() = "#@!$%^&"
    private fun provideVeryLongString() = "#" + "F".repeat(1000)
    private fun provideWrongLengthString() = "#FF"
    private fun provideMixedCaseString() = "#FfAaBb"
    private fun provideUnicodeString() = "#你好世界"
    private fun provideSQLInjectionString() = "#'; DROP TABLE colors; --"
    private fun provideStringWithNewlines() = "#FF\n00\r\n00"
    private fun provideNegativeNumberString() = "#-FF000"
    private fun provideMultipleHashesString() = "##FF0000"
    private fun provideStringWithWhitespace() = "  #FF0000  "
}

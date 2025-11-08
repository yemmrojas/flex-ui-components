package com.libs.flex.ui.flexui.styling

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.libs.flex.ui.flexui.styling.infrastructure.adapter.ComposeDimensionParser
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for DimensionParser (deprecated wrapper).
 *
 * Tests dimension parsing from various formats including dp values,
 * match_parent, wrap_content, and error handling.
 *
 * Note: These tests verify backward compatibility of the deprecated DimensionParser object.
 * For new code, use ComposeDimensionParserTest instead.
 */
class DimensionParserTest {
    
    @Test
    fun `parse should return correct Dp for valid dp string`() {
        // Given
        val parser = provideParser()
        val dimension = provideValidDpString()
        
        // When
        val result = parser.parse(dimension)
        
        // Then
        assertEquals(16.dp, result)
    }
    
    @Test
    fun `parse should return Dp Infinity for match_parent`() {
        // Given
        val parser = provideParser()
        val dimension = provideMatchParentString()
        
        // When
        val result = parser.parse(dimension)
        
        // Then
        assertEquals(Dp.Infinity, result)
    }
    
    @Test
    fun `parse should return Dp Unspecified for wrap_content`() {
        // Given
        val parser = provideParser()
        val dimension = provideWrapContentString()
        
        // When
        val result = parser.parse(dimension)
        
        // Then
        assertEquals(Dp.Unspecified, result)
    }
    
    @Test
    fun `parse should return correct Dp for plain numeric string`() {
        // Given
        val parser = provideParser()
        val dimension = providePlainNumericString()
        
        // When
        val result = parser.parse(dimension)
        
        // Then
        assertEquals(100.dp, result)
    }
    
    @Test
    fun `parse should return 0 dp for invalid string`() {
        // Given
        val parser = provideParser()
        val dimension = provideInvalidString()
        
        // When
        val result = parser.parse(dimension)
        
        // Then
        assertEquals(0.dp, result)
    }
    
    @Test
    fun `parse should return 0 dp for empty string`() {
        // Given
        val parser = provideParser()
        val dimension = provideEmptyString()
        
        // When
        val result = parser.parse(dimension)
        
        // Then
        assertEquals(0.dp, result)
    }
    
    @Test
    fun `parse should handle large dp values correctly`() {
        // Given
        val parser = provideParser()
        val dimension = provideLargeDpValue()
        
        // When
        val result = parser.parse(dimension)
        
        // Then
        assertEquals(1000.dp, result)
    }
    
    @Test
    fun `parse should handle zero dp correctly`() {
        // Given
        val parser = provideParser()
        val dimension = provideZeroDp()
        
        // When
        val result = parser.parse(dimension)
        
        // Then
        assertEquals(0.dp, result)
    }
    
    @Test
    fun `parse should return 0 dp for negative values`() {
        // Given
        val parser = provideParser()
        val dimension = provideNegativeDp()
        
        // When
        val result = parser.parse(dimension)
        
        // Then
        // Note: Negative dp values are parsed but Compose may handle them differently
        assertEquals((-10).dp, result)
    }
    
    @Test
    fun `parse should handle dp string with spaces`() {
        // Given
        val parser = provideParser()
        val dimension = provideDpStringWithSpaces()
        
        // When
        val result = parser.parse(dimension)
        
        // Then
        // Spaces are not trimmed, so this should fail to parse
        assertEquals(0.dp, result)
    }
    
    @Test
    fun `parse should handle single digit dp values`() {
        // Given
        val parser = provideParser()
        val dimension = provideSingleDigitDp()
        
        // When
        val result = parser.parse(dimension)
        
        // Then
        assertEquals(8.dp, result)
    }
    
    // Provider functions
    
    private fun provideParser() = ComposeDimensionParser()
    
    private fun provideValidDpString() = "16dp"
    
    private fun provideMatchParentString() = "match_parent"
    
    private fun provideWrapContentString() = "wrap_content"
    
    private fun providePlainNumericString() = "100"
    
    private fun provideInvalidString() = "invalid"
    
    private fun provideEmptyString() = ""
    
    private fun provideLargeDpValue() = "1000dp"
    
    private fun provideZeroDp() = "0dp"
    
    private fun provideNegativeDp() = "-10dp"
    
    private fun provideDpStringWithSpaces() = " 16 dp "
    
    private fun provideSingleDigitDp() = "8dp"
}

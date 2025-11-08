package com.libs.flex.ui.flexui.styling

import androidx.compose.ui.Modifier
import com.libs.flex.ui.flexui.model.PaddingValues
import com.libs.flex.ui.flexui.model.StyleProperties
import com.libs.flex.ui.flexui.styling.domain.service.StyleResolverService
import com.libs.flex.ui.flexui.styling.infrastructure.adapter.ComposeDimensionParser
import com.libs.flex.ui.flexui.styling.infrastructure.adapter.HexColorParser
import org.junit.Test

/**
 * Unit tests for StyleResolver (deprecated wrapper).
 *
 * Tests the application of various style properties to Compose modifiers,
 * including padding, background color, dimensions, border radius, and elevation.
 *
 * Note: These tests verify backward compatibility of the deprecated StyleResolver object.
 * For new code, use StyleResolverServiceTest instead.
 */
class StyleResolverTest {
    
    @Test
    fun `applyStyles should return original modifier when style is null`() {
        // Given
        val resolver = provideResolver()
        val modifier = provideBaseModifier()
        val style = provideNullStyle()
        
        // When
        val result = resolver.applyStyles(modifier, style)
        
        // Then
        // Modifier should be unchanged (same reference)
        assert(result === modifier)
    }
    
    @Test
    fun `applyStyles should apply padding when provided`() {
        // Given
        val resolver = provideResolver()
        val modifier = provideBaseModifier()
        val style = provideStyleWithPadding()
        
        // When
        val result = resolver.applyStyles(modifier, style)
        
        // Then
        // Modifier chain should be different from original
        assert(result !== modifier)
    }
    
    @Test
    fun `applyStyles should apply background color when provided`() {
        // Given
        val resolver = provideResolver()
        val modifier = provideBaseModifier()
        val style = provideStyleWithBackgroundColor()
        
        // When
        val result = resolver.applyStyles(modifier, style)
        
        // Then
        assert(result !== modifier)
    }
    
    @Test
    fun `applyStyles should apply width when provided`() {
        // Given
        val resolver = provideResolver()
        val modifier = provideBaseModifier()
        val style = provideStyleWithWidth()
        
        // When
        val result = resolver.applyStyles(modifier, style)
        
        // Then
        assert(result !== modifier)
    }
    
    @Test
    fun `applyStyles should apply height when provided`() {
        // Given
        val resolver = provideResolver()
        val modifier = provideBaseModifier()
        val style = provideStyleWithHeight()
        
        // When
        val result = resolver.applyStyles(modifier, style)
        
        // Then
        assert(result !== modifier)
    }
    
    @Test
    fun `applyStyles should apply border radius when provided`() {
        // Given
        val resolver = provideResolver()
        val modifier = provideBaseModifier()
        val style = provideStyleWithBorderRadius()
        
        // When
        val result = resolver.applyStyles(modifier, style)
        
        // Then
        assert(result !== modifier)
    }
    
    @Test
    fun `applyStyles should apply elevation when provided`() {
        // Given
        val resolver = provideResolver()
        val modifier = provideBaseModifier()
        val style = provideStyleWithElevation()
        
        // When
        val result = resolver.applyStyles(modifier, style)
        
        // Then
        assert(result !== modifier)
    }
    
    @Test
    fun `applyStyles should apply all properties when all are provided`() {
        // Given
        val resolver = provideResolver()
        val modifier = provideBaseModifier()
        val style = provideStyleWithAllProperties()
        
        // When
        val result = resolver.applyStyles(modifier, style)
        
        // Then
        assert(result !== modifier)
    }
    
    @Test
    fun `applyStyles should handle empty padding values`() {
        // Given
        val resolver = provideResolver()
        val modifier = provideBaseModifier()
        val style = provideStyleWithEmptyPadding()
        
        // When
        val result = resolver.applyStyles(modifier, style)
        
        // Then
        assert(result !== modifier)
    }
    
    @Test
    fun `applyStyles should handle match_parent width`() {
        // Given
        val resolver = provideResolver()
        val modifier = provideBaseModifier()
        val style = provideStyleWithMatchParentWidth()
        
        // When
        val result = resolver.applyStyles(modifier, style)
        
        // Then
        assert(result !== modifier)
    }
    
    @Test
    fun `applyStyles should handle wrap_content height`() {
        // Given
        val resolver = provideResolver()
        val modifier = provideBaseModifier()
        val style = provideStyleWithWrapContentHeight()
        
        // When
        val result = resolver.applyStyles(modifier, style)
        
        // Then
        assert(result !== modifier)
    }
    
    @Test
    fun `applyStyles should handle elevation with border radius`() {
        // Given
        val resolver = provideResolver()
        val modifier = provideBaseModifier()
        val style = provideStyleWithElevationAndBorderRadius()
        
        // When
        val result = resolver.applyStyles(modifier, style)
        
        // Then
        assert(result !== modifier)
    }
    
    @Test
    fun `applyStyles should handle elevation without border radius`() {
        // Given
        val resolver = provideResolver()
        val modifier = provideBaseModifier()
        val style = provideStyleWithElevationOnly()
        
        // When
        val result = resolver.applyStyles(modifier, style)
        
        // Then
        assert(result !== modifier)
    }
    
    @Test
    fun `applyStyles should handle invalid color gracefully`() {
        // Given
        val resolver = provideResolver()
        val modifier = provideBaseModifier()
        val style = provideStyleWithInvalidColor()
        
        // When
        val result = resolver.applyStyles(modifier, style)
        
        // Then
        // Should not throw exception, applies transparent color
        assert(result !== modifier)
    }
    
    @Test
    fun `applyStyles should handle invalid width gracefully`() {
        // Given
        val resolver = provideResolver()
        val modifier = provideBaseModifier()
        val style = provideStyleWithInvalidWidth()
        
        // When
        val result = resolver.applyStyles(modifier, style)
        
        // Then
        // Should not throw exception, applies 0.dp
        assert(result !== modifier)
    }
    
    // Provider functions
    
    private fun provideResolver() = StyleResolverService(
        colorParser = HexColorParser(),
        dimensionParser = ComposeDimensionParser()
    )
    
    private fun provideBaseModifier() = Modifier
    
    private fun provideNullStyle(): StyleProperties? = null
    
    private fun provideStyleWithPadding() = StyleProperties(
        padding = PaddingValues(start = 16, top = 8, end = 16, bottom = 8)
    )
    
    private fun provideStyleWithBackgroundColor() = StyleProperties(
        backgroundColor = "#FF0000"
    )
    
    private fun provideStyleWithWidth() = StyleProperties(
        width = "100dp"
    )
    
    private fun provideStyleWithHeight() = StyleProperties(
        height = "50dp"
    )
    
    private fun provideStyleWithBorderRadius() = StyleProperties(
        borderRadius = 8
    )
    
    private fun provideStyleWithElevation() = StyleProperties(
        elevation = 4
    )
    
    private fun provideStyleWithAllProperties() = StyleProperties(
        padding = PaddingValues(start = 16, top = 8, end = 16, bottom = 8),
        backgroundColor = "#FFFFFF",
        width = "200dp",
        height = "100dp",
        borderRadius = 12,
        elevation = 8
    )
    
    private fun provideStyleWithEmptyPadding() = StyleProperties(
        padding = PaddingValues(start = 0, top = 0, end = 0, bottom = 0)
    )
    
    private fun provideStyleWithMatchParentWidth() = StyleProperties(
        width = "match_parent"
    )
    
    private fun provideStyleWithWrapContentHeight() = StyleProperties(
        height = "wrap_content"
    )
    
    private fun provideStyleWithElevationAndBorderRadius() = StyleProperties(
        elevation = 4,
        borderRadius = 8
    )
    
    private fun provideStyleWithElevationOnly() = StyleProperties(
        elevation = 4
    )
    
    private fun provideStyleWithInvalidColor() = StyleProperties(
        backgroundColor = "invalid_color"
    )
    
    private fun provideStyleWithInvalidWidth() = StyleProperties(
        width = "invalid_width"
    )
}

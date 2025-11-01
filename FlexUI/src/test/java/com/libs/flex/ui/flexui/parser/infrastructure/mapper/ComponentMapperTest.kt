package com.libs.flex.ui.flexui.parser.infrastructure.mapper

import com.libs.flex.ui.flexui.exceptions.ComponentTypeNotFoundException
import com.libs.flex.ui.flexui.model.ComponentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ComponentMapperTest {
    
    @Test
    fun `mapType should return correct ComponentType for valid layout type`() {
        // Given
        val mapper = provideComponentMapper()
        val typeString = "contentVertical"
        
        // When
        val result = mapper.mapType(typeString)
        
        // Then
        assertEquals(ComponentType.CONTENT_VERTICAL, result)
    }
    
    @Test
    fun `mapType should return correct ComponentType for valid atomic type`() {
        // Given
        val mapper = provideComponentMapper()
        val typeString = "componentButton"
        
        // When
        val result = mapper.mapType(typeString)
        
        // Then
        assertEquals(ComponentType.COMPONENT_BUTTON, result)
    }
    
    @Test(expected = ComponentTypeNotFoundException::class)
    fun `mapType should throw exception for unknown type`() {
        // Given
        val mapper = provideComponentMapper()
        val invalidType = "unknownType"
        
        // When
        mapper.mapType(invalidType)
        
        // Then - exception thrown
    }
    
    @Test
    fun `mapTypeIgnoreCase should handle case insensitive matching`() {
        // Given
        val mapper = provideComponentMapper()
        val typeString = "CONTENTVERTICAL"
        
        // When
        val result = mapper.mapTypeIgnoreCase(typeString)
        
        // Then
        assertEquals(ComponentType.CONTENT_VERTICAL, result)
    }
    
    @Test
    fun `isLayoutType should return true for layout types`() {
        // Given
        val mapper = provideComponentMapper()
        
        // When & Then
        assertTrue(mapper.isLayoutType(ComponentType.CONTENT_VERTICAL))
        assertTrue(mapper.isLayoutType(ComponentType.CONTENT_HORIZONTAL))
        assertTrue(mapper.isLayoutType(ComponentType.CONTENT_SCROLL))
        assertTrue(mapper.isLayoutType(ComponentType.CONTENT_WITH_FLOATING_BUTTON))
        assertTrue(mapper.isLayoutType(ComponentType.CONTENT_LIST))
        assertTrue(mapper.isLayoutType(ComponentType.CONTENT_SLIDER))
    }
    
    @Test
    fun `isLayoutType should return false for atomic types`() {
        // Given
        val mapper = provideComponentMapper()
        
        // When & Then
        assertFalse(mapper.isLayoutType(ComponentType.COMPONENT_BUTTON))
        assertFalse(mapper.isLayoutType(ComponentType.COMPONENT_INPUT))
        assertFalse(mapper.isLayoutType(ComponentType.COMPONENT_TEXT_VIEW))
        assertFalse(mapper.isLayoutType(ComponentType.COMPONENT_CHECK))
        assertFalse(mapper.isLayoutType(ComponentType.COMPONENT_SELECT))
        assertFalse(mapper.isLayoutType(ComponentType.COMPONENT_SLIDER_CHECK))
        assertFalse(mapper.isLayoutType(ComponentType.COMPONENT_IMAGE))
        assertFalse(mapper.isLayoutType(ComponentType.COMPONENT_LOADER))
        assertFalse(mapper.isLayoutType(ComponentType.COMPONENT_TOAST))
    }
    
    @Test
    fun `all ComponentType values should have valid jsonKey mapping`() {
        // Given
        val mapper = provideComponentMapper()
        
        // When & Then - verify all enum values can be mapped
        ComponentType.entries.forEach { type ->
            val result = mapper.mapType(type.jsonKey)
            assertEquals(type, result)
        }
    }
    
    // Provider functions
    
    private fun provideComponentMapper() = ComponentMapper()
}

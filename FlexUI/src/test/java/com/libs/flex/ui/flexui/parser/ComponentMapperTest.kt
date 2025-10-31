package com.libs.flex.ui.flexui.parser

import com.libs.flex.ui.flexui.exceptions.ComponentTypeNotFoundException
import com.libs.flex.ui.flexui.model.ComponentType
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ComponentMapperTest {
    
    @After
    fun tearDown() {
        unmockkAll()
        clearAllMocks()
    }
    
    @Test
    fun `mapType should return CONTENT_VERTICAL for contentVertical string`() {
        // Arrange
        val typeString = provideContentVerticalTypeString()
        
        // Act
        val result = ComponentMapper.mapType(typeString)
        
        // Assert
        assertEquals(ComponentType.CONTENT_VERTICAL, result)
    }
    
    @Test
    fun `mapType should return CONTENT_HORIZONTAL for contentHorizontal string`() {
        // Arrange
        val typeString = provideContentHorizontalTypeString()
        
        // Act
        val result = ComponentMapper.mapType(typeString)
        
        // Assert
        assertEquals(ComponentType.CONTENT_HORIZONTAL, result)
    }
    
    @Test
    fun `mapType should return CONTENT_SCROLL for contentScroll string`() {
        // Arrange
        val typeString = provideContentScrollTypeString()
        
        // Act
        val result = ComponentMapper.mapType(typeString)
        
        // Assert
        assertEquals(ComponentType.CONTENT_SCROLL, result)
    }
    
    @Test
    fun `mapType should return CONTENT_WITH_FLOATING_BUTTON for contentWithFloatingButton string`() {
        // Arrange
        val typeString = provideContentWithFloatingButtonTypeString()
        
        // Act
        val result = ComponentMapper.mapType(typeString)
        
        // Assert
        assertEquals(ComponentType.CONTENT_WITH_FLOATING_BUTTON, result)
    }
    
    @Test
    fun `mapType should return CONTENT_LIST for contentList string`() {
        // Arrange
        val typeString = provideContentListTypeString()
        
        // Act
        val result = ComponentMapper.mapType(typeString)
        
        // Assert
        assertEquals(ComponentType.CONTENT_LIST, result)
    }
    
    @Test
    fun `mapType should return CONTENT_SLIDER for contentSlider string`() {
        // Arrange
        val typeString = provideContentSliderTypeString()
        
        // Act
        val result = ComponentMapper.mapType(typeString)
        
        // Assert
        assertEquals(ComponentType.CONTENT_SLIDER, result)
    }
    
    @Test
    fun `mapType should return COMPONENT_INPUT for componentInput string`() {
        // Arrange
        val typeString = provideComponentInputTypeString()
        
        // Act
        val result = ComponentMapper.mapType(typeString)
        
        // Assert
        assertEquals(ComponentType.COMPONENT_INPUT, result)
    }
    
    @Test
    fun `mapType should return COMPONENT_TEXT_VIEW for componentTextView string`() {
        // Arrange
        val typeString = provideComponentTextViewTypeString()
        
        // Act
        val result = ComponentMapper.mapType(typeString)
        
        // Assert
        assertEquals(ComponentType.COMPONENT_TEXT_VIEW, result)
    }
    
    @Test
    fun `mapType should return COMPONENT_CHECK for componentCheck string`() {
        // Arrange
        val typeString = provideComponentCheckTypeString()
        
        // Act
        val result = ComponentMapper.mapType(typeString)
        
        // Assert
        assertEquals(ComponentType.COMPONENT_CHECK, result)
    }
    
    @Test
    fun `mapType should return COMPONENT_SELECT for componentSelect string`() {
        // Arrange
        val typeString = provideComponentSelectTypeString()
        
        // Act
        val result = ComponentMapper.mapType(typeString)
        
        // Assert
        assertEquals(ComponentType.COMPONENT_SELECT, result)
    }
    
    @Test
    fun `mapType should return COMPONENT_SLIDER_CHECK for componentSliderCheck string`() {
        // Arrange
        val typeString = provideComponentSliderCheckTypeString()
        
        // Act
        val result = ComponentMapper.mapType(typeString)
        
        // Assert
        assertEquals(ComponentType.COMPONENT_SLIDER_CHECK, result)
    }
    
    @Test
    fun `mapType should return COMPONENT_BUTTON for componentButton string`() {
        // Arrange
        val typeString = provideComponentButtonTypeString()
        
        // Act
        val result = ComponentMapper.mapType(typeString)
        
        // Assert
        assertEquals(ComponentType.COMPONENT_BUTTON, result)
    }
    
    @Test
    fun `mapType should return COMPONENT_IMAGE for componentImage string`() {
        // Arrange
        val typeString = provideComponentImageTypeString()
        
        // Act
        val result = ComponentMapper.mapType(typeString)
        
        // Assert
        assertEquals(ComponentType.COMPONENT_IMAGE, result)
    }
    
    @Test
    fun `mapType should return COMPONENT_LOADER for componentLoader string`() {
        // Arrange
        val typeString = provideComponentLoaderTypeString()
        
        // Act
        val result = ComponentMapper.mapType(typeString)
        
        // Assert
        assertEquals(ComponentType.COMPONENT_LOADER, result)
    }
    
    @Test
    fun `mapType should return COMPONENT_TOAST for componentToast string`() {
        // Arrange
        val typeString = provideComponentToastTypeString()
        
        // Act
        val result = ComponentMapper.mapType(typeString)
        
        // Assert
        assertEquals(ComponentType.COMPONENT_TOAST, result)
    }
    
    @Test(expected = ComponentTypeNotFoundException::class)
    fun `mapType should throw ComponentTypeNotFoundException for unknown type`() {
        // Arrange
        val unknownType = provideUnknownTypeString()
        
        // Act
        ComponentMapper.mapType(unknownType)
        
        // Assert - exception expected
    }
    
    @Test(expected = ComponentTypeNotFoundException::class)
    fun `mapType should throw ComponentTypeNotFoundException for empty string`() {
        // Arrange
        val emptyType = provideEmptyTypeString()
        
        // Act
        ComponentMapper.mapType(emptyType)
        
        // Assert - exception expected
    }
    
    @Test(expected = ComponentTypeNotFoundException::class)
    fun `mapType should throw ComponentTypeNotFoundException for case mismatch`() {
        // Arrange
        val wrongCaseType = provideWrongCaseTypeString()
        
        // Act
        ComponentMapper.mapType(wrongCaseType)
        
        // Assert - exception expected
    }
    
    @Test
    fun `isLayoutType should return true for CONTENT_VERTICAL`() {
        // Arrange
        val layoutType = provideContentVerticalType()
        
        // Act
        val result = ComponentMapper.isLayoutType(layoutType)
        
        // Assert
        assertTrue(result)
    }
    
    @Test
    fun `isLayoutType should return true for CONTENT_HORIZONTAL`() {
        // Arrange
        val layoutType = provideContentHorizontalType()
        
        // Act
        val result = ComponentMapper.isLayoutType(layoutType)
        
        // Assert
        assertTrue(result)
    }
    
    @Test
    fun `isLayoutType should return true for CONTENT_SCROLL`() {
        // Arrange
        val layoutType = provideContentScrollType()
        
        // Act
        val result = ComponentMapper.isLayoutType(layoutType)
        
        // Assert
        assertTrue(result)
    }
    
    @Test
    fun `isLayoutType should return true for CONTENT_WITH_FLOATING_BUTTON`() {
        // Arrange
        val layoutType = provideContentWithFloatingButtonType()
        
        // Act
        val result = ComponentMapper.isLayoutType(layoutType)
        
        // Assert
        assertTrue(result)
    }
    
    @Test
    fun `isLayoutType should return true for CONTENT_LIST`() {
        // Arrange
        val layoutType = provideContentListType()
        
        // Act
        val result = ComponentMapper.isLayoutType(layoutType)
        
        // Assert
        assertTrue(result)
    }
    
    @Test
    fun `isLayoutType should return true for CONTENT_SLIDER`() {
        // Arrange
        val layoutType = provideContentSliderType()
        
        // Act
        val result = ComponentMapper.isLayoutType(layoutType)
        
        // Assert
        assertTrue(result)
    }
    
    @Test
    fun `isLayoutType should return false for COMPONENT_INPUT`() {
        // Arrange
        val atomicType = provideComponentInputType()
        
        // Act
        val result = ComponentMapper.isLayoutType(atomicType)
        
        // Assert
        assertFalse(result)
    }
    
    @Test
    fun `isLayoutType should return false for COMPONENT_TEXT_VIEW`() {
        // Arrange
        val atomicType = provideComponentTextViewType()
        
        // Act
        val result = ComponentMapper.isLayoutType(atomicType)
        
        // Assert
        assertFalse(result)
    }
    
    @Test
    fun `isLayoutType should return false for COMPONENT_CHECK`() {
        // Arrange
        val atomicType = provideComponentCheckType()
        
        // Act
        val result = ComponentMapper.isLayoutType(atomicType)
        
        // Assert
        assertFalse(result)
    }
    
    @Test
    fun `isLayoutType should return false for COMPONENT_SELECT`() {
        // Arrange
        val atomicType = provideComponentSelectType()
        
        // Act
        val result = ComponentMapper.isLayoutType(atomicType)
        
        // Assert
        assertFalse(result)
    }
    
    @Test
    fun `isLayoutType should return false for COMPONENT_SLIDER_CHECK`() {
        // Arrange
        val atomicType = provideComponentSliderCheckType()
        
        // Act
        val result = ComponentMapper.isLayoutType(atomicType)
        
        // Assert
        assertFalse(result)
    }
    
    @Test
    fun `isLayoutType should return false for COMPONENT_BUTTON`() {
        // Arrange
        val atomicType = provideComponentButtonType()
        
        // Act
        val result = ComponentMapper.isLayoutType(atomicType)
        
        // Assert
        assertFalse(result)
    }
    
    @Test
    fun `isLayoutType should return false for COMPONENT_IMAGE`() {
        // Arrange
        val atomicType = provideComponentImageType()
        
        // Act
        val result = ComponentMapper.isLayoutType(atomicType)
        
        // Assert
        assertFalse(result)
    }
    
    @Test
    fun `isLayoutType should return false for COMPONENT_LOADER`() {
        // Arrange
        val atomicType = provideComponentLoaderType()
        
        // Act
        val result = ComponentMapper.isLayoutType(atomicType)
        
        // Assert
        assertFalse(result)
    }
    
    @Test
    fun `isLayoutType should return false for COMPONENT_TOAST`() {
        // Arrange
        val atomicType = provideComponentToastType()
        
        // Act
        val result = ComponentMapper.isLayoutType(atomicType)
        
        // Assert
        assertFalse(result)
    }
    
    // Provider functions for type strings
    
    private fun provideContentVerticalTypeString() = "contentVertical"
    private fun provideContentHorizontalTypeString() = "contentHorizontal"
    private fun provideContentScrollTypeString() = "contentScroll"
    private fun provideContentWithFloatingButtonTypeString() = "contentWithFloatingButton"
    private fun provideContentListTypeString() = "contentList"
    private fun provideContentSliderTypeString() = "contentSlider"
    
    private fun provideComponentInputTypeString() = "componentInput"
    private fun provideComponentTextViewTypeString() = "componentTextView"
    private fun provideComponentCheckTypeString() = "componentCheck"
    private fun provideComponentSelectTypeString() = "componentSelect"
    private fun provideComponentSliderCheckTypeString() = "componentSliderCheck"
    private fun provideComponentButtonTypeString() = "componentButton"
    private fun provideComponentImageTypeString() = "componentImage"
    private fun provideComponentLoaderTypeString() = "componentLoader"
    private fun provideComponentToastTypeString() = "componentToast"
    
    private fun provideUnknownTypeString() = "unknownComponentType"
    private fun provideEmptyTypeString() = ""
    private fun provideWrongCaseTypeString() = "ComponentButton"
    
    // Provider functions for ComponentType enums
    
    private fun provideContentVerticalType() = ComponentType.CONTENT_VERTICAL
    private fun provideContentHorizontalType() = ComponentType.CONTENT_HORIZONTAL
    private fun provideContentScrollType() = ComponentType.CONTENT_SCROLL
    private fun provideContentWithFloatingButtonType() = ComponentType.CONTENT_WITH_FLOATING_BUTTON
    private fun provideContentListType() = ComponentType.CONTENT_LIST
    private fun provideContentSliderType() = ComponentType.CONTENT_SLIDER
    
    private fun provideComponentInputType() = ComponentType.COMPONENT_INPUT
    private fun provideComponentTextViewType() = ComponentType.COMPONENT_TEXT_VIEW
    private fun provideComponentCheckType() = ComponentType.COMPONENT_CHECK
    private fun provideComponentSelectType() = ComponentType.COMPONENT_SELECT
    private fun provideComponentSliderCheckType() = ComponentType.COMPONENT_SLIDER_CHECK
    private fun provideComponentButtonType() = ComponentType.COMPONENT_BUTTON
    private fun provideComponentImageType() = ComponentType.COMPONENT_IMAGE
    private fun provideComponentLoaderType() = ComponentType.COMPONENT_LOADER
    private fun provideComponentToastType() = ComponentType.COMPONENT_TOAST
}

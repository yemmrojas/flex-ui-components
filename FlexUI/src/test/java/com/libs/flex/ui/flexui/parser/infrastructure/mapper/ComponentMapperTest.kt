package com.libs.flex.ui.flexui.parser.infrastructure.mapper

import com.libs.flex.ui.flexui.exceptions.ComponentTypeNotFoundException
import com.libs.flex.ui.flexui.model.ComponentType
import com.libs.flex.ui.flexui.parser.infrastructure.mapper.ComponentMapper
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
        // Given
        val typeString = provideContentVerticalTypeString()
        
        // When
        val result = ComponentMapper.mapType(typeString)
        
        // Then
        assertEquals(ComponentType.CONTENT_VERTICAL, result)
    }
    
    @Test
    fun `mapType should return CONTENT_HORIZONTAL for contentHorizontal string`() {
        // Given
        val typeString = provideContentHorizontalTypeString()
        
        // When
        val result = ComponentMapper.mapType(typeString)
        
        // Then
        assertEquals(ComponentType.CONTENT_HORIZONTAL, result)
    }
    
    @Test
    fun `mapType should return CONTENT_SCROLL for contentScroll string`() {
        // Given
        val typeString = provideContentScrollTypeString()
        
        // When
        val result = ComponentMapper.mapType(typeString)
        
        // Then
        assertEquals(ComponentType.CONTENT_SCROLL, result)
    }
    
    @Test
    fun `mapType should return CONTENT_WITH_FLOATING_BUTTON for contentWithFloatingButton string`() {
        // Given
        val typeString = provideContentWithFloatingButtonTypeString()
        
        // When
        val result = ComponentMapper.mapType(typeString)
        
        // Then
        assertEquals(ComponentType.CONTENT_WITH_FLOATING_BUTTON, result)
    }
    
    @Test
    fun `mapType should return CONTENT_LIST for contentList string`() {
        // Given
        val typeString = provideContentListTypeString()
        
        // When
        val result = ComponentMapper.mapType(typeString)
        
        // Then
        assertEquals(ComponentType.CONTENT_LIST, result)
    }
    
    @Test
    fun `mapType should return CONTENT_SLIDER for contentSlider string`() {
        // Given
        val typeString = provideContentSliderTypeString()
        
        // When
        val result = ComponentMapper.mapType(typeString)
        
        // Then
        assertEquals(ComponentType.CONTENT_SLIDER, result)
    }
    
    @Test
    fun `mapType should return COMPONENT_INPUT for componentInput string`() {
        // Given
        val typeString = provideComponentInputTypeString()
        
        // When
        val result = ComponentMapper.mapType(typeString)
        
        // Then
        assertEquals(ComponentType.COMPONENT_INPUT, result)
    }
    
    @Test
    fun `mapType should return COMPONENT_TEXT_VIEW for componentTextView string`() {
        // Given
        val typeString = provideComponentTextViewTypeString()
        
        // When
        val result = ComponentMapper.mapType(typeString)
        
        // Then
        assertEquals(ComponentType.COMPONENT_TEXT_VIEW, result)
    }
    
    @Test
    fun `mapType should return COMPONENT_CHECK for componentCheck string`() {
        // Given
        val typeString = provideComponentCheckTypeString()
        
        // When
        val result = ComponentMapper.mapType(typeString)
        
        // Then
        assertEquals(ComponentType.COMPONENT_CHECK, result)
    }
    
    @Test
    fun `mapType should return COMPONENT_SELECT for componentSelect string`() {
        // Given
        val typeString = provideComponentSelectTypeString()
        
        // When
        val result = ComponentMapper.mapType(typeString)
        
        // Then
        assertEquals(ComponentType.COMPONENT_SELECT, result)
    }
    
    @Test
    fun `mapType should return COMPONENT_SLIDER_CHECK for componentSliderCheck string`() {
        // Given
        val typeString = provideComponentSliderCheckTypeString()
        
        // When
        val result = ComponentMapper.mapType(typeString)
        
        // Then
        assertEquals(ComponentType.COMPONENT_SLIDER_CHECK, result)
    }
    
    @Test
    fun `mapType should return COMPONENT_BUTTON for componentButton string`() {
        // Given
        val typeString = provideComponentButtonTypeString()
        
        // When
        val result = ComponentMapper.mapType(typeString)
        
        // Then
        assertEquals(ComponentType.COMPONENT_BUTTON, result)
    }
    
    @Test
    fun `mapType should return COMPONENT_IMAGE for componentImage string`() {
        // Given
        val typeString = provideComponentImageTypeString()
        
        // When
        val result = ComponentMapper.mapType(typeString)
        
        // Then
        assertEquals(ComponentType.COMPONENT_IMAGE, result)
    }
    
    @Test
    fun `mapType should return COMPONENT_LOADER for componentLoader string`() {
        // Given
        val typeString = provideComponentLoaderTypeString()
        
        // When
        val result = ComponentMapper.mapType(typeString)
        
        // Then
        assertEquals(ComponentType.COMPONENT_LOADER, result)
    }
    
    @Test
    fun `mapType should return COMPONENT_TOAST for componentToast string`() {
        // Given
        val typeString = provideComponentToastTypeString()
        
        // When
        val result = ComponentMapper.mapType(typeString)
        
        // Then
        assertEquals(ComponentType.COMPONENT_TOAST, result)
    }
    
    @Test(expected = ComponentTypeNotFoundException::class)
    fun `mapType should throw ComponentTypeNotFoundException for unknown type`() {
        // Given
        val unknownType = provideUnknownTypeString()
        
        // When
        ComponentMapper.mapType(unknownType)
        
        // Then - exception expected
    }
    
    @Test(expected = ComponentTypeNotFoundException::class)
    fun `mapType should throw ComponentTypeNotFoundException for empty string`() {
        // Given
        val emptyType = provideEmptyTypeString()
        
        // When
        ComponentMapper.mapType(emptyType)
        
        // Then - exception expected
    }
    
    @Test(expected = ComponentTypeNotFoundException::class)
    fun `mapType should throw ComponentTypeNotFoundException for case mismatch`() {
        // Given
        val wrongCaseType = provideWrongCaseTypeString()
        
        // When
        ComponentMapper.mapType(wrongCaseType)
        
        // Then - exception expected
    }
    
    @Test
    fun `isLayoutType should return true for CONTENT_VERTICAL`() {
        // Given
        val layoutType = provideContentVerticalType()
        
        // When
        val result = ComponentMapper.isLayoutType(layoutType)
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `isLayoutType should return true for CONTENT_HORIZONTAL`() {
        // Given
        val layoutType = provideContentHorizontalType()
        
        // When
        val result = ComponentMapper.isLayoutType(layoutType)
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `isLayoutType should return true for CONTENT_SCROLL`() {
        // Given
        val layoutType = provideContentScrollType()
        
        // When
        val result = ComponentMapper.isLayoutType(layoutType)
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `isLayoutType should return true for CONTENT_WITH_FLOATING_BUTTON`() {
        // Given
        val layoutType = provideContentWithFloatingButtonType()
        
        // When
        val result = ComponentMapper.isLayoutType(layoutType)
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `isLayoutType should return true for CONTENT_LIST`() {
        // Given
        val layoutType = provideContentListType()
        
        // When
        val result = ComponentMapper.isLayoutType(layoutType)
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `isLayoutType should return true for CONTENT_SLIDER`() {
        // Given
        val layoutType = provideContentSliderType()
        
        // When
        val result = ComponentMapper.isLayoutType(layoutType)
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `isLayoutType should return false for COMPONENT_INPUT`() {
        // Given
        val atomicType = provideComponentInputType()
        
        // When
        val result = ComponentMapper.isLayoutType(atomicType)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `isLayoutType should return false for COMPONENT_TEXT_VIEW`() {
        // Given
        val atomicType = provideComponentTextViewType()
        
        // When
        val result = ComponentMapper.isLayoutType(atomicType)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `isLayoutType should return false for COMPONENT_CHECK`() {
        // Given
        val atomicType = provideComponentCheckType()
        
        // When
        val result = ComponentMapper.isLayoutType(atomicType)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `isLayoutType should return false for COMPONENT_SELECT`() {
        // Given
        val atomicType = provideComponentSelectType()
        
        // When
        val result = ComponentMapper.isLayoutType(atomicType)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `isLayoutType should return false for COMPONENT_SLIDER_CHECK`() {
        // Given
        val atomicType = provideComponentSliderCheckType()
        
        // When
        val result = ComponentMapper.isLayoutType(atomicType)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `isLayoutType should return false for COMPONENT_BUTTON`() {
        // Given
        val atomicType = provideComponentButtonType()
        
        // When
        val result = ComponentMapper.isLayoutType(atomicType)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `isLayoutType should return false for COMPONENT_IMAGE`() {
        // Given
        val atomicType = provideComponentImageType()
        
        // When
        val result = ComponentMapper.isLayoutType(atomicType)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `isLayoutType should return false for COMPONENT_LOADER`() {
        // Given
        val atomicType = provideComponentLoaderType()
        
        // When
        val result = ComponentMapper.isLayoutType(atomicType)
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `isLayoutType should return false for COMPONENT_TOAST`() {
        // Given
        val atomicType = provideComponentToastType()
        
        // When
        val result = ComponentMapper.isLayoutType(atomicType)
        
        // Then
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

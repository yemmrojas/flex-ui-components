package com.libs.flex.ui.flexui.parser

import com.libs.flex.ui.flexui.exceptions.JsonParseException
import com.libs.flex.ui.flexui.exceptions.MissingPropertyException
import com.libs.flex.ui.flexui.model.AtomicDescriptor
import com.libs.flex.ui.flexui.model.ComponentType
import com.libs.flex.ui.flexui.model.LayoutDescriptor
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonParserTest {
    
    @After
    fun tearDown() {
        unmockkAll()
        clearAllMocks()
    }
    
    @Test
    fun `parse should return success when JSON contains valid atomic component`() = runTest {
        // Given
        val parser = provideJsonParser()
        val validJson = provideValidAtomicComponentJson()
        
        // when
        val result = parser.parse(validJson)
        
        // Then
        assertTrue(result.isSuccess)
        val descriptor = result.getOrNull()
        assertNotNull(descriptor)
        assertTrue(descriptor is AtomicDescriptor)
        assertEquals("test_button", descriptor?.id)
        assertEquals(ComponentType.COMPONENT_BUTTON, descriptor?.type)
    }
    
    @Test
    fun `parse should return success when JSON contains valid layout container`() = runTest {
        // Arrange
        val parser = provideJsonParser()
        val validJson = provideValidLayoutContainerJson()
        
        // Act
        val result = parser.parse(validJson)
        
        // Assert
        assertTrue(result.isSuccess)
        val descriptor = result.getOrNull()
        assertNotNull(descriptor)
        assertTrue(descriptor is LayoutDescriptor)
        assertEquals("test_container", descriptor?.id)
        assertEquals(ComponentType.CONTENT_VERTICAL, descriptor?.type)
    }
    
    @Test
    fun `parse should handle nested children recursively`() = runTest {
        // Arrange
        val parser = provideJsonParser()
        val nestedJson = provideNestedComponentJson()
        
        // Act
        val result = parser.parse(nestedJson)
        
        // Assert
        assertTrue(result.isSuccess)
        val descriptor = result.getOrNull() as? LayoutDescriptor
        assertNotNull(descriptor)
        assertEquals(2, descriptor?.children?.size)
        assertEquals("child1", descriptor?.children?.get(0)?.id)
        assertEquals("child2", descriptor?.children?.get(1)?.id)
    }
    
    @Test
    fun `parse should extract style properties correctly`() = runTest {
        // Arrange
        val parser = provideJsonParser()
        val styledJson = provideStyledComponentJson()
        
        // Act
        val result = parser.parse(styledJson)
        
        // Assert
        assertTrue(result.isSuccess)
        val descriptor = result.getOrNull()
        assertNotNull(descriptor?.style)
        assertEquals("#FF0000", descriptor?.style?.backgroundColor)
        assertEquals(8, descriptor?.style?.borderRadius)
        assertEquals(4, descriptor?.style?.elevation)
        assertEquals(16, descriptor?.style?.padding?.start)
    }
    
    @Test
    fun `parse should extract atomic component properties`() = runTest {
        // Arrange
        val parser = provideJsonParser()
        val inputJson = provideInputComponentJson()
        
        // Act
        val result = parser.parse(inputJson)
        
        // Assert
        assertTrue(result.isSuccess)
        val descriptor = result.getOrNull() as? AtomicDescriptor
        assertNotNull(descriptor)
        assertEquals("Username", descriptor?.label)
        assertEquals("Enter username", descriptor?.placeholder)
        assertEquals("outlined", descriptor?.inputStyle)
        assertTrue(descriptor?.enabled ?: false)
        assertNotNull(descriptor?.validation)
        assertTrue(descriptor?.validation?.required ?: false)
        assertEquals(3, descriptor?.validation?.minLength)
    }
    
    @Test
    fun `parse should extract select options correctly`() = runTest {
        // Arrange
        val parser = provideJsonParser()
        val selectJson = provideSelectComponentJson()
        
        // Act
        val result = parser.parse(selectJson)
        
        // Assert
        assertTrue(result.isSuccess)
        val descriptor = result.getOrNull() as? AtomicDescriptor
        assertNotNull(descriptor?.options)
        assertEquals(2, descriptor?.options?.size)
        assertEquals("United States", descriptor?.options?.get(0)?.label)
        assertEquals("us", descriptor?.options?.get(0)?.value)
    }
    
    @Test
    fun `parse should extract layout container properties`() = runTest {
        // Arrange
        val parser = provideJsonParser()
        val sliderJson = provideSliderComponentJson()
        
        // Act
        val result = parser.parse(sliderJson)
        
        // Assert
        assertTrue(result.isSuccess)
        val descriptor = result.getOrNull() as? LayoutDescriptor
        assertNotNull(descriptor)
        assertTrue(descriptor?.autoPlay ?: false)
        assertEquals(3000L, descriptor?.autoPlayInterval)
        assertEquals(2, descriptor?.items?.size)
    }
    
    @Test
    fun `parse should return failure when type is missing`() = runTest {
        // Arrange
        val parser = provideJsonParser()
        val invalidJson = provideJsonWithoutType()
        
        // Act
        val result = parser.parse(invalidJson)
        
        // Assert
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is MissingPropertyException)
    }
    
    @Test
    fun `parse should return failure when id is missing`() = runTest {
        // Arrange
        val parser = provideJsonParser()
        val invalidJson = provideJsonWithoutId()
        
        // Act
        val result = parser.parse(invalidJson)
        
        // Assert
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is MissingPropertyException)
    }
    
    @Test
    fun `parse should return failure when type is unknown`() = runTest {
        // Arrange
        val parser = provideJsonParser()
        val invalidJson = provideJsonWithUnknownType()
        
        // Act
        val result = parser.parse(invalidJson)
        
        // Assert
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is JsonParseException)
    }
    
    @Test
    fun `parse should handle malformed JSON`() = runTest {
        // Arrange
        val parser = provideJsonParser()
        val malformedJson = provideMalformedJson()
        
        // Act
        val result = parser.parse(malformedJson)
        
        // Assert
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `parse should handle deeply nested component hierarchy`() = runTest {
        // Arrange
        val parser = provideJsonParser()
        val deeplyNestedJson = provideDeeplyNestedJson()
        
        // Act
        val result = parser.parse(deeplyNestedJson)
        
        // Assert
        assertTrue(result.isSuccess)
        val root = result.getOrNull() as? LayoutDescriptor
        assertNotNull(root)
        assertEquals("root", root?.id)
        
        val level1 = root?.children?.get(0) as? LayoutDescriptor
        assertEquals("level1", level1?.id)
        
        val level2 = level1?.children?.get(0) as? LayoutDescriptor
        assertEquals("level2", level2?.id)
        
        val leaf = level2?.children?.get(0) as? AtomicDescriptor
        assertEquals("leaf", leaf?.id)
        assertEquals("Deep nested text", leaf?.text)
    }
    
    @Test
    fun `parse should handle optional properties with null values`() = runTest {
        // Arrange
        val parser = provideJsonParser()
        val minimalJson = provideMinimalComponentJson()
        
        // Act
        val result = parser.parse(minimalJson)
        
        // Assert
        assertTrue(result.isSuccess)
        val descriptor = result.getOrNull() as? AtomicDescriptor
        assertNotNull(descriptor)
        assertNull(descriptor?.text)
        assertNull(descriptor?.buttonStyle)
        assertNull(descriptor?.style)
        assertTrue(descriptor?.enabled ?: false)
    }
    
    // Provider functions
    
    private fun provideJsonParser() = JsonParser()
    
    private fun provideValidAtomicComponentJson() = """
        {
            "id": "test_button",
            "type": "componentButton",
            "text": "Click Me",
            "buttonStyle": "primary"
        }
    """.trimIndent()
    
    private fun provideValidLayoutContainerJson() = """
        {
            "id": "test_container",
            "type": "contentVertical",
            "arrangement": "center",
            "alignment": "start",
            "children": []
        }
    """.trimIndent()
    
    private fun provideNestedComponentJson() = """
        {
            "id": "parent",
            "type": "contentVertical",
            "children": [
                {
                    "id": "child1",
                    "type": "componentTextView",
                    "text": "Hello"
                },
                {
                    "id": "child2",
                    "type": "componentButton",
                    "text": "Click"
                }
            ]
        }
    """.trimIndent()
    
    private fun provideStyledComponentJson() = """
        {
            "id": "styled_component",
            "type": "componentButton",
            "text": "Styled Button",
            "style": {
                "padding": {
                    "start": 16,
                    "top": 8,
                    "end": 16,
                    "bottom": 8
                },
                "backgroundColor": "#FF0000",
                "borderRadius": 8,
                "elevation": 4
            }
        }
    """.trimIndent()
    
    private fun provideInputComponentJson() = """
        {
            "id": "input_field",
            "type": "componentInput",
            "label": "Username",
            "placeholder": "Enter username",
            "inputStyle": "outlined",
            "enabled": true,
            "validation": {
                "required": true,
                "minLength": 3,
                "maxLength": 20,
                "errorMessage": "Username must be 3-20 characters"
            }
        }
    """.trimIndent()
    
    private fun provideSelectComponentJson() = """
        {
            "id": "country_select",
            "type": "componentSelect",
            "label": "Country",
            "options": [
                {"label": "United States", "value": "us"},
                {"label": "Canada", "value": "ca"}
            ]
        }
    """.trimIndent()
    
    private fun provideSliderComponentJson() = """
        {
            "id": "slider",
            "type": "contentSlider",
            "autoPlay": true,
            "autoPlayInterval": 3000,
            "items": [
                "https://example.com/image1.jpg",
                "https://example.com/image2.jpg"
            ]
        }
    """.trimIndent()
    
    private fun provideJsonWithoutType() = """
        {
            "id": "test_component",
            "text": "No type specified"
        }
    """.trimIndent()
    
    private fun provideJsonWithoutId() = """
        {
            "type": "componentButton",
            "text": "No id specified"
        }
    """.trimIndent()
    
    private fun provideJsonWithUnknownType() = """
        {
            "id": "test_component",
            "type": "unknownComponentType",
            "text": "Invalid type"
        }
    """.trimIndent()
    
    private fun provideMalformedJson() = """
        {
            "id": "test",
            "type": "componentButton"
            "text": "Missing comma"
        }
    """.trimIndent()
    
    private fun provideDeeplyNestedJson() = """
        {
            "id": "root",
            "type": "contentVertical",
            "children": [
                {
                    "id": "level1",
                    "type": "contentHorizontal",
                    "children": [
                        {
                            "id": "level2",
                            "type": "contentScroll",
                            "children": [
                                {
                                    "id": "leaf",
                                    "type": "componentTextView",
                                    "text": "Deep nested text"
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    """.trimIndent()
    
    private fun provideMinimalComponentJson() = """
        {
            "id": "minimal",
            "type": "componentButton"
        }
    """.trimIndent()
}

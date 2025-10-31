package com.libs.flex.ui.flexui.parser.application

import com.libs.flex.ui.flexui.model.ComponentType
import com.libs.flex.ui.flexui.model.LayoutDescriptor
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonParserFacadeTest {
    
    @After
    fun tearDown() {
        unmockkAll()
        clearAllMocks()
    }
    
    @Test
    fun `parse should return success for valid JSON`() = runTest {
        // Given
        val facade = provideJsonParserFacade()
        val validJson = provideValidLayoutJson()
        
        // When
        val result = facade.parse(validJson)
        
        // Then
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
    }
    
    @Test
    fun `parse should parse layout component correctly`() = runTest {
        // Given
        val facade = provideJsonParserFacade()
        val layoutJson = provideValidLayoutJson()
        
        // When
        val result = facade.parse(layoutJson)
        
        // Then
        assertTrue(result.isSuccess)
        val descriptor = result.getOrNull()
        assertTrue(descriptor is LayoutDescriptor)
        assertEquals("test_layout", descriptor?.id)
        assertEquals(ComponentType.CONTENT_VERTICAL, descriptor?.type)
    }
    
    @Test
    fun `parse should parse atomic component correctly`() = runTest {
        // Given
        val facade = provideJsonParserFacade()
        val atomicJson = provideValidAtomicJson()
        
        // When
        val result = facade.parse(atomicJson)
        
        // Then
        assertTrue(result.isSuccess)
        val descriptor = result.getOrNull()
        assertNotNull(descriptor)
        assertEquals("test_button", descriptor?.id)
        assertEquals(ComponentType.COMPONENT_BUTTON, descriptor?.type)
    }
    
    @Test
    fun `parse should handle nested components`() = runTest {
        // Given
        val facade = provideJsonParserFacade()
        val nestedJson = provideNestedComponentsJson()
        
        // When
        val result = facade.parse(nestedJson)
        
        // Then
        assertTrue(result.isSuccess)
        val descriptor = result.getOrNull() as? LayoutDescriptor
        assertNotNull(descriptor)
        assertEquals(2, descriptor?.children?.size)
    }
    
    @Test
    fun `parse should return failure for malformed JSON`() = runTest {
        // Given
        val facade = provideJsonParserFacade()
        val malformedJson = provideMalformedJson()
        
        // When
        val result = facade.parse(malformedJson)
        
        // Then
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `parse should return failure for missing required fields`() = runTest {
        // Given
        val facade = provideJsonParserFacade()
        val invalidJson = provideJsonWithoutId()
        
        // When
        val result = facade.parse(invalidJson)
        
        // Then
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `defaultStrategies should return non-empty list`() {
        // When
        val strategies = JsonParserFacade.defaultStrategies()
        
        // Then
        assertTrue(strategies.isNotEmpty())
        assertEquals(2, strategies.size)
    }
    
    // Provider functions
    
    private fun provideJsonParserFacade() = JsonParserFacade()
    
    private fun provideValidLayoutJson() = """
        {
            "id": "test_layout",
            "type": "contentVertical",
            "arrangement": "center"
        }
    """.trimIndent()
    
    private fun provideValidAtomicJson() = """
        {
            "id": "test_button",
            "type": "componentButton",
            "text": "Click Me"
        }
    """.trimIndent()
    
    private fun provideNestedComponentsJson() = """
        {
            "id": "parent",
            "type": "contentVertical",
            "children": [
                {
                    "id": "child1",
                    "type": "componentTextView",
                    "text": "Text 1"
                },
                {
                    "id": "child2",
                    "type": "componentButton",
                    "text": "Button 1"
                }
            ]
        }
    """.trimIndent()
    
    private fun provideMalformedJson() = """
        {
            "id": "test"
            "type": "componentButton"
        }
    """.trimIndent()
    
    private fun provideJsonWithoutId() = """
        {
            "type": "componentButton",
            "text": "No ID"
        }
    """.trimIndent()
}

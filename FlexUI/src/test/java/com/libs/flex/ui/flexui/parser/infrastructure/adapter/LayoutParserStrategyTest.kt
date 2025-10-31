package com.libs.flex.ui.flexui.parser.infrastructure.adapter

import com.libs.flex.ui.flexui.model.ComponentDescriptor
import com.libs.flex.ui.flexui.model.ComponentType
import com.libs.flex.ui.flexui.model.LayoutDescriptor
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LayoutParserStrategyTest {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    @After
    fun tearDown() {
        unmockkAll()
        clearAllMocks()
    }
    
    @Test
    fun `canParse should return true for layout types`() {
        // Given
        val strategy = provideLayoutParserStrategy()
        
        // When & Assert
        assertTrue(strategy.canParse(ComponentType.CONTENT_VERTICAL))
        assertTrue(strategy.canParse(ComponentType.CONTENT_HORIZONTAL))
        assertTrue(strategy.canParse(ComponentType.CONTENT_SCROLL))
    }
    
    @Test
    fun `canParse should return false for atomic types`() {
        // Given
        val strategy = provideLayoutParserStrategy()
        
        // When & Assert
        assertTrue(!strategy.canParse(ComponentType.COMPONENT_BUTTON))
        assertTrue(!strategy.canParse(ComponentType.COMPONENT_TEXT_VIEW))
    }
    
    @Test
    fun `parse should create LayoutDescriptor with correct properties`() {
        // Given
        val strategy = provideLayoutParserStrategy()
        val jsonString = provideLayoutJson()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        val mockParser: (kotlinx.serialization.json.JsonObject) -> ComponentDescriptor = mockk()
        
        // When
        val result = strategy.parse(jsonObject, ComponentType.CONTENT_VERTICAL, mockParser)
        
        // Then
        assertTrue(result is LayoutDescriptor)
        val layout = result as LayoutDescriptor
        assertEquals("test_layout", layout.id)
        assertEquals(ComponentType.CONTENT_VERTICAL, layout.type)
        assertEquals("center", layout.arrangement)
        assertEquals("start", layout.alignment)
    }
    
    @Test
    fun `parse should recursively parse children`() {
        // Given
        val strategy = provideLayoutParserStrategy()
        val jsonString = provideLayoutWithChildrenJson()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        val mockChild = mockk<ComponentDescriptor>()
        val mockParser: (kotlinx.serialization.json.JsonObject) -> ComponentDescriptor = mockk()
        every { mockParser(any()) } returns mockChild
        
        // When
        val result = strategy.parse(jsonObject, ComponentType.CONTENT_VERTICAL, mockParser)
        
        // Then
        assertTrue(result is LayoutDescriptor)
        val layout = result as LayoutDescriptor
        assertEquals(2, layout.children.size)
        verify(exactly = 2) { mockParser(any()) }
    }
    
    @Test
    fun `parse should handle optional properties`() {
        // Given
        val strategy = provideLayoutParserStrategy()
        val jsonString = provideMinimalLayoutJson()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        val mockParser: (kotlinx.serialization.json.JsonObject) -> ComponentDescriptor = mockk()
        
        // When
        val result = strategy.parse(jsonObject, ComponentType.CONTENT_VERTICAL, mockParser)
        
        // Then
        assertTrue(result is LayoutDescriptor)
        val layout = result as LayoutDescriptor
        assertEquals("minimal_layout", layout.id)
        assertTrue(layout.children.isEmpty())
        assertEquals(null, layout.arrangement)
        assertEquals(null, layout.alignment)
    }
    
    @Test
    fun `parse should handle slider properties`() {
        // Given
        val strategy = provideLayoutParserStrategy()
        val jsonString = provideSliderLayoutJson()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        val mockParser: (kotlinx.serialization.json.JsonObject) -> ComponentDescriptor = mockk()
        
        // When
        val result = strategy.parse(jsonObject, ComponentType.CONTENT_SLIDER, mockParser)
        
        // Then
        assertTrue(result is LayoutDescriptor)
        val layout = result as LayoutDescriptor
        assertEquals(true, layout.autoPlay)
        assertEquals(3000L, layout.autoPlayInterval)
        assertNotNull(layout.items)
        assertEquals(2, layout.items?.size)
    }
    
    // Provider functions
    
    private fun provideLayoutParserStrategy() = LayoutParserStrategy()
    
    private fun provideLayoutJson() = """
        {
            "id": "test_layout",
            "type": "contentVertical",
            "arrangement": "center",
            "alignment": "start"
        }
    """.trimIndent()
    
    private fun provideLayoutWithChildrenJson() = """
        {
            "id": "parent_layout",
            "type": "contentVertical",
            "children": [
                {"id": "child1", "type": "componentTextView", "text": "Text 1"},
                {"id": "child2", "type": "componentButton", "text": "Button 1"}
            ]
        }
    """.trimIndent()
    
    private fun provideMinimalLayoutJson() = """
        {
            "id": "minimal_layout",
            "type": "contentVertical"
        }
    """.trimIndent()
    
    private fun provideSliderLayoutJson() = """
        {
            "id": "slider_layout",
            "type": "contentSlider",
            "autoPlay": true,
            "autoPlayInterval": 3000,
            "items": ["item1", "item2"]
        }
    """.trimIndent()
}

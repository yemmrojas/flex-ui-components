package com.libs.flex.ui.flexui.parser.infrastructure.adapter

import com.libs.flex.ui.flexui.model.AtomicDescriptor
import com.libs.flex.ui.flexui.model.ComponentDescriptor
import com.libs.flex.ui.flexui.model.ComponentType
import io.mockk.clearAllMocks
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AtomicParserStrategyTest {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    @After
    fun tearDown() {
        unmockkAll()
        clearAllMocks()
    }
    
    @Test
    fun `canParse should return true for atomic types`() {
        // Given
        val strategy = provideAtomicParserStrategy()
        
        // When & Assert
        assertTrue(strategy.canParse(ComponentType.COMPONENT_BUTTON))
        assertTrue(strategy.canParse(ComponentType.COMPONENT_TEXT_VIEW))
        assertTrue(strategy.canParse(ComponentType.COMPONENT_INPUT))
    }
    
    @Test
    fun `canParse should return false for layout types`() {
        // Given
        val strategy = provideAtomicParserStrategy()
        
        // When & Assert
        assertFalse(strategy.canParse(ComponentType.CONTENT_VERTICAL))
        assertFalse(strategy.canParse(ComponentType.CONTENT_HORIZONTAL))
    }
    
    @Test
    fun `parse should create AtomicDescriptor with correct properties`() {
        // Given
        val strategy = provideAtomicParserStrategy()
        val jsonString = provideButtonJson()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        val mockParser: (kotlinx.serialization.json.JsonObject) -> ComponentDescriptor = mockk()
        
        // When
        val result = strategy.parse(jsonObject, ComponentType.COMPONENT_BUTTON, mockParser)
        
        // Then
        assertTrue(result is AtomicDescriptor)
        val atomic = result as AtomicDescriptor
        assertEquals("test_button", atomic.id)
        assertEquals(ComponentType.COMPONENT_BUTTON, atomic.type)
        assertEquals("Click Me", atomic.text)
        assertEquals("primary", atomic.buttonStyle)
    }
    
    @Test
    fun `parse should handle input component with validation`() {
        // Given
        val strategy = provideAtomicParserStrategy()
        val jsonString = provideInputWithValidationJson()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        val mockParser: (kotlinx.serialization.json.JsonObject) -> ComponentDescriptor = mockk()
        
        // When
        val result = strategy.parse(jsonObject, ComponentType.COMPONENT_INPUT, mockParser)
        
        // Then
        assertTrue(result is AtomicDescriptor)
        val atomic = result as AtomicDescriptor
        assertEquals("username_input", atomic.id)
        assertEquals("Username", atomic.label)
        assertEquals("Enter username", atomic.placeholder)
        assertNotNull(atomic.validation)
        assertTrue(atomic.validation?.required ?: false)
        assertEquals(3, atomic.validation?.minLength)
    }
    
    @Test
    fun `parse should handle select component with options`() {
        // Given
        val strategy = provideAtomicParserStrategy()
        val jsonString = provideSelectWithOptionsJson()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        val mockParser: (kotlinx.serialization.json.JsonObject) -> ComponentDescriptor = mockk()
        
        // When
        val result = strategy.parse(jsonObject, ComponentType.COMPONENT_SELECT, mockParser)
        
        // Then
        assertTrue(result is AtomicDescriptor)
        val atomic = result as AtomicDescriptor
        assertEquals("country_select", atomic.id)
        assertNotNull(atomic.options)
        assertEquals(2, atomic.options?.size)
        assertEquals("United States", atomic.options?.get(0)?.label)
        assertEquals("us", atomic.options?.get(0)?.value)
    }
    
    @Test
    fun `parse should handle minimal atomic component`() {
        // Given
        val strategy = provideAtomicParserStrategy()
        val jsonString = provideMinimalAtomicJson()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        val mockParser: (kotlinx.serialization.json.JsonObject) -> ComponentDescriptor = mockk()
        
        // When
        val result = strategy.parse(jsonObject, ComponentType.COMPONENT_TEXT_VIEW, mockParser)
        
        // Then
        assertTrue(result is AtomicDescriptor)
        val atomic = result as AtomicDescriptor
        assertEquals("minimal_text", atomic.id)
        assertNull(atomic.text)
        assertNull(atomic.style)
        assertTrue(atomic.enabled!!)
    }
    
    // Provider functions
    
    private fun provideAtomicParserStrategy() = AtomicParserStrategy()
    
    private fun provideButtonJson() = """
        {
            "id": "test_button",
            "type": "componentButton",
            "text": "Click Me",
            "buttonStyle": "primary"
        }
    """.trimIndent()
    
    private fun provideInputWithValidationJson() = """
        {
            "id": "username_input",
            "type": "componentInput",
            "label": "Username",
            "placeholder": "Enter username",
            "validation": {
                "required": true,
                "minLength": 3,
                "maxLength": 20
            }
        }
    """.trimIndent()
    
    private fun provideSelectWithOptionsJson() = """
        {
            "id": "country_select",
            "type": "componentSelect",
            "options": [
                {"label": "United States", "value": "us"},
                {"label": "Canada", "value": "ca"}
            ]
        }
    """.trimIndent()
    
    private fun provideMinimalAtomicJson() = """
        {
            "id": "minimal_text",
            "type": "componentTextView"
        }
    """.trimIndent()
}

package com.libs.flex.ui.flexui.parser.infrastructure.property
import io.mockk.clearAllMocks
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

class PropertyParserTest {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    @After
    fun tearDown() {
        unmockkAll()
        clearAllMocks()
    }
    
    @Test
    fun `StylePropertiesParser should parse all style properties correctly`() {
        // Given
        val jsonString = provideStylePropertiesJson()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        
        // When
        val result = StylePropertiesParser.parse(jsonObject)
        
        // Then
        assertNotNull(result)
        assertEquals("#FF0000", result.backgroundColor)
        assertEquals(8, result.borderRadius)
        assertEquals(4, result.elevation)
        assertEquals("match_parent", result.width)
        assertEquals("wrap_content", result.height)
        assertNotNull(result.padding)
        assertNotNull(result.margin)
    }
    
    @Test
    fun `StylePropertiesParser should handle missing optional properties`() {
        // Given
        val jsonString = provideMinimalStylePropertiesJson()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        
        // When
        val result = StylePropertiesParser.parse(jsonObject)
        
        // Then
        assertNotNull(result)
        assertNull(result.backgroundColor)
        assertNull(result.borderRadius)
        assertNull(result.padding)
    }
    
    @Test
    fun `PaddingValuesParser should parse all padding values correctly`() {
        // Given
        val jsonString = providePaddingValuesJson()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        
        // When
        val result = PaddingValuesParser.parse(jsonObject)
        
        // Then
        assertNotNull(result)
        assertEquals(16, result.start)
        assertEquals(8, result.top)
        assertEquals(16, result.end)
        assertEquals(8, result.bottom)
    }
    
    @Test
    fun `PaddingValuesParser should use default values for missing properties`() {
        // Given
        val jsonString = provideEmptyPaddingValuesJson()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        
        // When
        val result = PaddingValuesParser.parse(jsonObject)
        
        // Then
        assertNotNull(result)
        assertEquals(0, result.start)
        assertEquals(0, result.top)
        assertEquals(0, result.end)
        assertEquals(0, result.bottom)
    }
    
    @Test
    fun `SelectOptionParser should parse label and value correctly`() {
        // Given
        val jsonString = provideSelectOptionJson()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        
        // When
        val result = SelectOptionParser.parse(jsonObject)
        
        // Then
        assertNotNull(result)
        assertEquals("United States", result.label)
        assertEquals("us", result.value)
    }
    
    @Test
    fun `SelectOptionParser should handle empty values`() {
        // Given
        val jsonString = provideEmptySelectOptionJson()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        
        // When
        val result = SelectOptionParser.parse(jsonObject)
        
        // Then
        assertNotNull(result)
        assertEquals("", result.label)
        assertEquals("", result.value)
    }
    
    @Test
    fun `ValidationRulesParser should parse all validation rules correctly`() {
        // Given
        val jsonString = provideValidationRulesJson()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        
        // When
        val result = ValidationRulesParser.parse(jsonObject)
        
        // Then
        assertNotNull(result)
        assertTrue(result.required)
        assertEquals(3, result.minLength)
        assertEquals(20, result.maxLength)
        assertEquals("^[a-zA-Z]+$", result.pattern)
        assertEquals("Invalid input", result.errorMessage)
    }
    
    @Test
    fun `ValidationRulesParser should handle missing optional properties`() {
        // Given
        val jsonString = provideMinimalValidationRulesJson()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        
        // When
        val result = ValidationRulesParser.parse(jsonObject)
        
        // Then
        assertNotNull(result)
        assertFalse(result.required)
        assertNull(result.minLength)
        assertNull(result.maxLength)
        assertNull(result.pattern)
    }
    
    // Provider functions
    
    private fun provideStylePropertiesJson() = """
        {
            "padding": {
                "start": 16,
                "top": 8,
                "end": 16,
                "bottom": 8
            },
            "margin": {
                "start": 4,
                "top": 4,
                "end": 4,
                "bottom": 4
            },
            "backgroundColor": "#FF0000",
            "borderRadius": 8,
            "elevation": 4,
            "width": "match_parent",
            "height": "wrap_content"
        }
    """.trimIndent()
    
    private fun provideMinimalStylePropertiesJson() = """
        {}
    """.trimIndent()
    
    private fun providePaddingValuesJson() = """
        {
            "start": 16,
            "top": 8,
            "end": 16,
            "bottom": 8
        }
    """.trimIndent()
    
    private fun provideEmptyPaddingValuesJson() = """
        {}
    """.trimIndent()
    
    private fun provideSelectOptionJson() = """
        {
            "label": "United States",
            "value": "us"
        }
    """.trimIndent()
    
    private fun provideEmptySelectOptionJson() = """
        {}
    """.trimIndent()
    
    private fun provideValidationRulesJson() = """
        {
            "required": true,
            "minLength": 3,
            "maxLength": 20,
            "pattern": "^[a-zA-Z]+$",
            "errorMessage": "Invalid input"
        }
    """.trimIndent()
    
    private fun provideMinimalValidationRulesJson() = """
        {}
    """.trimIndent()
}

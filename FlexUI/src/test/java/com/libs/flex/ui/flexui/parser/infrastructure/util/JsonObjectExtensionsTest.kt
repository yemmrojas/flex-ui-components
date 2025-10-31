package com.libs.flex.ui.flexui.parser.infrastructure.util

import com.libs.flex.ui.flexui.exceptions.MissingPropertyException
import io.mockk.clearAllMocks
import io.mockk.unmockkAll
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonObjectExtensionsTest {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    @After
    fun tearDown() {
        unmockkAll()
        clearAllMocks()
    }
    
    @Test
    fun `getRequiredString should return value when property exists`() {
        // Given
        val jsonString = provideJsonWithStringProperty()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        
        // When
        val result = jsonObject.getRequiredString("name", "TestComponent")
        
        // Then
        assertEquals("test_value", result)
    }
    
    @Test(expected = MissingPropertyException::class)
    fun `getRequiredString should throw exception when property is missing`() {
        // Given
        val jsonString = provideEmptyJson()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        
        // When
        jsonObject.getRequiredString("name", "TestComponent")
        
        // Then - exception expected
    }
    
    @Test
    fun `getOptionalString should return value when property exists`() {
        // Given
        val jsonString = provideJsonWithStringProperty()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        
        // When
        val result = jsonObject.getOptionalString("name")
        
        // Then
        assertEquals("test_value", result)
    }
    
    @Test
    fun `getOptionalString should return null when property is missing`() {
        // Given
        val jsonString = provideEmptyJson()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        
        // When
        val result = jsonObject.getOptionalString("name")
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `getOptionalInt should return value when property exists`() {
        // Given
        val jsonString = provideJsonWithIntProperty()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        
        // When
        val result = jsonObject.getOptionalInt("count")
        
        // Then
        assertEquals(42, result)
    }
    
    @Test
    fun `getOptionalInt should return null when property is missing`() {
        // Given
        val jsonString = provideEmptyJson()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        
        // When
        val result = jsonObject.getOptionalInt("count")
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `getOptionalFloat should return value when property exists`() {
        // Given
        val jsonString = provideJsonWithFloatProperty()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        
        // When
        val result = jsonObject.getOptionalFloat("rating")
        
        // Then
        assertEquals(4.5f, result)
    }
    
    @Test
    fun `getOptionalBoolean should return value when property exists`() {
        // Given
        val jsonString = provideJsonWithBooleanProperty()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        
        // When
        val result = jsonObject.getOptionalBoolean("enabled")
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `getOptionalBoolean should return default value when property is missing`() {
        // Given
        val jsonString = provideEmptyJson()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        
        // When
        val result = jsonObject.getOptionalBoolean("enabled", true)
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `getOptionalBooleanOrNull should return value when property exists`() {
        // Given
        val jsonString = provideJsonWithBooleanProperty()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        
        // When
        val result = jsonObject.getOptionalBooleanOrNull("enabled")
        
        // Then
        assertNotNull(result)
        assertTrue(result!!)
    }
    
    @Test
    fun `getOptionalBooleanOrNull should return null when property is missing`() {
        // Given
        val jsonString = provideEmptyJson()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        
        // When
        val result = jsonObject.getOptionalBooleanOrNull("enabled")
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `getOptionalObject should return JsonObject when property exists`() {
        // Given
        val jsonString = provideJsonWithObjectProperty()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        
        // When
        val result = jsonObject.getOptionalObject("style")
        
        // Then
        assertNotNull(result)
        assertEquals("#FF0000", result?.get("color")?.jsonPrimitive?.content)
    }
    
    @Test
    fun `getOptionalObject should return null when property is missing`() {
        // Given
        val jsonString = provideEmptyJson()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        
        // When
        val result = jsonObject.getOptionalObject("style")
        
        // Then
        assertNull(result)
    }
    
    @Test
    fun `getOptionalArray should return JsonArray when property exists`() {
        // Given
        val jsonString = provideJsonWithArrayProperty()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        
        // When
        val result = jsonObject.getOptionalArray("items")
        
        // Then
        assertNotNull(result)
        assertEquals(3, result?.size)
    }
    
    @Test
    fun `getOptionalArray should return null when property is missing`() {
        // Given
        val jsonString = provideEmptyJson()
        val jsonObject = json.parseToJsonElement(jsonString).jsonObject
        
        // When
        val result = jsonObject.getOptionalArray("items")
        
        // Then
        assertNull(result)
    }
    
    // Provider functions
    
    private fun provideJsonWithStringProperty() = """
        {
            "name": "test_value"
        }
    """.trimIndent()
    
    private fun provideJsonWithIntProperty() = """
        {
            "count": 42
        }
    """.trimIndent()
    
    private fun provideJsonWithFloatProperty() = """
        {
            "rating": 4.5
        }
    """.trimIndent()
    
    private fun provideJsonWithBooleanProperty() = """
        {
            "enabled": true
        }
    """.trimIndent()
    
    private fun provideJsonWithObjectProperty() = """
        {
            "style": {
                "color": "#FF0000"
            }
        }
    """.trimIndent()
    
    private fun provideJsonWithArrayProperty() = """
        {
            "items": ["item1", "item2", "item3"]
        }
    """.trimIndent()
    
    private fun provideEmptyJson() = """
        {}
    """.trimIndent()
}

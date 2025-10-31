package com.libs.flex.ui.flexui.parser.domain.service

import com.libs.flex.ui.flexui.exceptions.JsonParseException
import com.libs.flex.ui.flexui.model.AtomicDescriptor
import com.libs.flex.ui.flexui.model.ComponentDescriptor
import com.libs.flex.ui.flexui.model.ComponentType
import com.libs.flex.ui.flexui.parser.domain.ports.ComponentParserStrategyPort
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JsonParserServiceTest {
    
    @After
    fun tearDown() {
        unmockkAll()
        clearAllMocks()
    }
    
    @Test
    fun `parse should delegate to appropriate strategy`() = runTest {
        // Given
        val mockStrategy = provideMockStrategy()
        val mockDescriptor = provideMockDescriptor()
        every { mockStrategy.canParse(any()) } returns true
        every { mockStrategy.parse(any(), any(), any()) } returns mockDescriptor
        
        val service = provideJsonParserService(listOf(mockStrategy))
        val jsonString = provideValidJson()
        
        // When
        val result = service.parse(jsonString)
        
        // Then
        assertTrue(result.isSuccess)
        verify { mockStrategy.canParse(ComponentType.COMPONENT_BUTTON) }
        verify { mockStrategy.parse(any(), ComponentType.COMPONENT_BUTTON, any()) }
    }
    
    @Test
    fun `parse should return success when JSON is valid`() = runTest {
        // Given
        val mockStrategy = provideMockStrategy()
        val mockDescriptor = provideMockDescriptor()
        every { mockStrategy.canParse(any()) } returns true
        every { mockStrategy.parse(any(), any(), any()) } returns mockDescriptor
        
        val service = provideJsonParserService(listOf(mockStrategy))
        val jsonString = provideValidJson()
        
        // When
        val result = service.parse(jsonString)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(mockDescriptor, result.getOrNull())
    }
    
    @Test
    fun `parse should return failure when no strategy found`() = runTest {
        // Given
        val mockStrategy = provideMockStrategy()
        every { mockStrategy.canParse(any()) } returns false
        
        val service = provideJsonParserService(listOf(mockStrategy))
        val jsonString = provideValidJson()
        
        // When
        val result = service.parse(jsonString)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is JsonParseException)
    }
    
    @Test
    fun `parse should return failure when JSON is malformed`() = runTest {
        // Given
        val mockStrategy = provideMockStrategy()
        val service = provideJsonParserService(listOf(mockStrategy))
        val malformedJson = provideMalformedJson()
        
        // When
        val result = service.parse(malformedJson)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is JsonParseException)
    }
    
    @Test
    fun `parse should select correct strategy based on component type`() = runTest {
        // Given
        val layoutStrategy = provideMockStrategy()
        val atomicStrategy = provideMockStrategy()
        val mockDescriptor = provideMockDescriptor()
        
        every { layoutStrategy.canParse(ComponentType.COMPONENT_BUTTON) } returns false
        every { atomicStrategy.canParse(ComponentType.COMPONENT_BUTTON) } returns true
        every { atomicStrategy.parse(any(), any(), any()) } returns mockDescriptor
        
        val service = provideJsonParserService(listOf(layoutStrategy, atomicStrategy))
        val jsonString = provideValidJson()
        
        // When
        val result = service.parse(jsonString)
        
        // Then
        assertTrue(result.isSuccess)
        verify { layoutStrategy.canParse(ComponentType.COMPONENT_BUTTON) }
        verify { atomicStrategy.canParse(ComponentType.COMPONENT_BUTTON) }
        verify { atomicStrategy.parse(any(), any(), any()) }
        verify(exactly = 0) { layoutStrategy.parse(any(), any(), any()) }
    }
    
    @Test
    fun `parse should handle nested components recursively`() = runTest {
        // Given
        val mockStrategy = provideMockStrategy()
        val mockDescriptor = provideMockDescriptor()
        every { mockStrategy.canParse(any()) } returns true
        every { mockStrategy.parse(any(), any(), any()) } returns mockDescriptor
        
        val service = provideJsonParserService(listOf(mockStrategy))
        val nestedJson = provideNestedJson()
        
        // When
        val result = service.parse(nestedJson)
        
        // Then
        assertTrue(result.isSuccess)
        // Verify strategy was called for parent
        verify(atLeast = 1) { mockStrategy.parse(any(), any(), any()) }
    }
    
    // Provider functions
    
    private fun provideJsonParserService(strategies: List<ComponentParserStrategyPort>) =
        JsonParserService(strategies)
    
    private fun provideMockStrategy() = mockk<ComponentParserStrategyPort>()
    
    private fun provideMockDescriptor() = AtomicDescriptor(
        id = "mock_id",
        type = ComponentType.COMPONENT_BUTTON,
        text = "Mock Button"
    )
    
    private fun provideValidJson() = """
        {
            "id": "test_button",
            "type": "componentButton",
            "text": "Click Me"
        }
    """.trimIndent()
    
    private fun provideMalformedJson() = """
        {
            "id": "test"
            "type": "componentButton"
        }
    """.trimIndent()
    
    private fun provideNestedJson() = """
        {
            "id": "parent",
            "type": "contentVertical",
            "children": [
                {
                    "id": "child",
                    "type": "componentButton",
                    "text": "Child Button"
                }
            ]
        }
    """.trimIndent()
}

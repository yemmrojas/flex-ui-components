package com.libs.flex.ui.flexui.parser.domain.service

import com.libs.flex.ui.flexui.exceptions.JsonParseException
import com.libs.flex.ui.flexui.model.AtomicDescriptor
import com.libs.flex.ui.flexui.model.ComponentDescriptor
import com.libs.flex.ui.flexui.model.ComponentType
import com.libs.flex.ui.flexui.parser.domain.ports.ComponentParserStrategyPort
import com.libs.flex.ui.flexui.parser.domain.ports.ComponentTypeMapperPort
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
        val mockStrategy = provideMockStrategy(
            componentType = ComponentType.COMPONENT_BUTTON,
            isSupported = true
        )
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
        val mockStrategy = provideMockStrategy(
            componentType = ComponentType.COMPONENT_BUTTON,
            isSupported = true
        )
        val mockDescriptor = provideMockDescriptor()
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
        val mockStrategy = provideMockStrategy(
            componentType = ComponentType.COMPONENT_BUTTON,
            isSupported = false
        )
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
        val mockStrategy = provideMockStrategy(
            componentType = ComponentType.COMPONENT_BUTTON,
            isSupported = true
        )
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
        val layoutStrategy = provideMockStrategy(
            componentType = ComponentType.COMPONENT_BUTTON,
            isSupported = false
        )
        val atomicStrategy = provideMockStrategy(
            componentType = ComponentType.COMPONENT_BUTTON,
            isSupported = true
        )
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
        val mockStrategy = provideMockStrategyForAnyType()
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
        JsonParserService(strategies, provideMockComponentTypeMapper())
    
    private fun provideMockStrategy(
        componentType: ComponentType,
        isSupported: Boolean = true
    ) = mockk<ComponentParserStrategyPort>().apply {
        if (isSupported) {
            every { canParse(componentType) } returns true
            every { parse(any(), componentType, any()) } returns provideMockDescriptor()
        } else {
            every { canParse(componentType) } returns false
        }
    }
    
    private fun provideMockStrategyForAnyType() = mockk<ComponentParserStrategyPort>().apply {
        every { canParse(any()) } returns true
        every { parse(any(), any(), any()) } returns provideMockDescriptor()
    }
    
    private fun provideMockComponentTypeMapper() = mockk<ComponentTypeMapperPort>().apply {
        every { mapType("componentButton") } returns ComponentType.COMPONENT_BUTTON
        every { mapType("contentVertical") } returns ComponentType.CONTENT_VERTICAL
        every { isLayoutType(any()) } returns false
    }
    
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

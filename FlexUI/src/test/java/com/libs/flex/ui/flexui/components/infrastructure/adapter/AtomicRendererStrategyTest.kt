package com.libs.flex.ui.flexui.components.infrastructure.adapter

import com.libs.flex.ui.flexui.model.AtomicDescriptor
import com.libs.flex.ui.flexui.model.ComponentType
import org.junit.Test

/**
 * Unit tests for AtomicRendererStrategy.
 *
 * Tests the strategy's ability to identify and handle atomic component types.
 */
class AtomicRendererStrategyTest {
    
    @Test
    fun `canRender should return true for atomic types`() {
        // Given
        val strategy = provideStrategy()
        val atomicTypes = provideAllAtomicTypes()
        
        // When/Then
        atomicTypes.forEach { type ->
            assert(strategy.canRender(type)) {
                "Expected canRender to return true for atomic type: $type"
            }
        }
    }
    
    @Test
    fun `canRender should return false for layout types`() {
        // Given
        val strategy = provideStrategy()
        val layoutTypes = provideAllLayoutTypes()
        
        // When/Then
        layoutTypes.forEach { type ->
            assert(!strategy.canRender(type)) {
                "Expected canRender to return false for layout type: $type"
            }
        }
    }
    
    @Test
    fun `canRender should use ComponentType isLayout property`() {
        // Given
        val strategy = provideStrategy()
        
        // When/Then
        ComponentType.entries.forEach { type ->
            val result = strategy.canRender(type)
            assert(result == !type.isLayout) {
                "Expected canRender($type) to match !type.isLayout (${!type.isLayout})"
            }
        }
    }
    
    @Test
    fun `strategy should handle COMPONENT_INPUT type`() {
        // Given
        val strategy = provideStrategy()
        val descriptor = provideAtomicDescriptor(ComponentType.COMPONENT_INPUT)
        
        // When/Then
        assert(strategy.canRender(descriptor.type))
    }
    
    @Test
    fun `strategy should handle COMPONENT_TEXT_VIEW type`() {
        // Given
        val strategy = provideStrategy()
        val descriptor = provideAtomicDescriptor(ComponentType.COMPONENT_TEXT_VIEW)
        
        // When/Then
        assert(strategy.canRender(descriptor.type))
    }
    
    @Test
    fun `strategy should handle COMPONENT_CHECK type`() {
        // Given
        val strategy = provideStrategy()
        val descriptor = provideAtomicDescriptor(ComponentType.COMPONENT_CHECK)
        
        // When/Then
        assert(strategy.canRender(descriptor.type))
    }
    
    @Test
    fun `strategy should handle COMPONENT_SELECT type`() {
        // Given
        val strategy = provideStrategy()
        val descriptor = provideAtomicDescriptor(ComponentType.COMPONENT_SELECT)
        
        // When/Then
        assert(strategy.canRender(descriptor.type))
    }
    
    @Test
    fun `strategy should handle COMPONENT_SLIDER_CHECK type`() {
        // Given
        val strategy = provideStrategy()
        val descriptor = provideAtomicDescriptor(ComponentType.COMPONENT_SLIDER_CHECK)
        
        // When/Then
        assert(strategy.canRender(descriptor.type))
    }
    
    @Test
    fun `strategy should handle COMPONENT_BUTTON type`() {
        // Given
        val strategy = provideStrategy()
        val descriptor = provideAtomicDescriptor(ComponentType.COMPONENT_BUTTON)
        
        // When/Then
        assert(strategy.canRender(descriptor.type))
    }
    
    @Test
    fun `strategy should handle COMPONENT_IMAGE type`() {
        // Given
        val strategy = provideStrategy()
        val descriptor = provideAtomicDescriptor(ComponentType.COMPONENT_IMAGE)
        
        // When/Then
        assert(strategy.canRender(descriptor.type))
    }
    
    @Test
    fun `strategy should handle COMPONENT_LOADER type`() {
        // Given
        val strategy = provideStrategy()
        val descriptor = provideAtomicDescriptor(ComponentType.COMPONENT_LOADER)
        
        // When/Then
        assert(strategy.canRender(descriptor.type))
    }
    
    @Test
    fun `strategy should handle COMPONENT_TOAST type`() {
        // Given
        val strategy = provideStrategy()
        val descriptor = provideAtomicDescriptor(ComponentType.COMPONENT_TOAST)
        
        // When/Then
        assert(strategy.canRender(descriptor.type))
    }
    
    // Provider functions
    
    private fun provideStrategy() = AtomicRendererStrategy()
    
    private fun provideAllAtomicTypes() = listOf(
        ComponentType.COMPONENT_INPUT,
        ComponentType.COMPONENT_TEXT_VIEW,
        ComponentType.COMPONENT_CHECK,
        ComponentType.COMPONENT_SELECT,
        ComponentType.COMPONENT_SLIDER_CHECK,
        ComponentType.COMPONENT_BUTTON,
        ComponentType.COMPONENT_IMAGE,
        ComponentType.COMPONENT_LOADER,
        ComponentType.COMPONENT_TOAST
    )
    
    private fun provideAllLayoutTypes() = listOf(
        ComponentType.CONTENT_VERTICAL,
        ComponentType.CONTENT_HORIZONTAL,
        ComponentType.CONTENT_SCROLL,
        ComponentType.CONTENT_WITH_FLOATING_BUTTON,
        ComponentType.CONTENT_LIST,
        ComponentType.CONTENT_SLIDER
    )
    
    private fun provideAtomicDescriptor(type: ComponentType) = AtomicDescriptor(
        id = "test_atomic_${type.name}",
        type = type,
        text = "Test"
    )
}

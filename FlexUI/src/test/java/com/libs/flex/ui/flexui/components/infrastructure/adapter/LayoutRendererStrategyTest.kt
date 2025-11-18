package com.libs.flex.ui.flexui.components.infrastructure.adapter

import com.libs.flex.ui.flexui.components.ComponentFactory
import com.libs.flex.ui.flexui.model.ComponentType
import com.libs.flex.ui.flexui.model.LayoutDescriptor
import io.mockk.mockk
import org.junit.Test

/**
 * Unit tests for LayoutRendererStrategy.
 *
 * Tests the strategy's ability to identify and handle layout component types.
 */
class LayoutRendererStrategyTest {

    @Test
    fun `canRender should return true for layout types`() {
        // Given
        val strategy = provideStrategy()
        val layoutTypes = provideAllLayoutTypes()

        // When/Then
        layoutTypes.forEach { type ->
            assert(strategy.canRender(type)) {
                "Expected canRender to return true for layout type: $type"
            }
        }
    }

    @Test
    fun `canRender should return false for atomic types`() {
        // Given
        val strategy = provideStrategy()
        val atomicTypes = provideAllAtomicTypes()

        // When/Then
        atomicTypes.forEach { type ->
            assert(!strategy.canRender(type)) {
                "Expected canRender to return false for atomic type: $type"
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
            assert(result == type.isLayout) {
                "Expected canRender($type) to match type.isLayout (${type.isLayout})"
            }
        }
    }

    @Test
    fun `strategy should handle CONTENT_VERTICAL type`() {
        // Given
        val strategy = provideStrategy()
        val descriptor = provideLayoutDescriptor(ComponentType.CONTENT_VERTICAL)

        // When/Then
        assert(strategy.canRender(descriptor.type))
    }

    @Test
    fun `strategy should handle CONTENT_HORIZONTAL type`() {
        // Given
        val strategy = provideStrategy()
        val descriptor = provideLayoutDescriptor(ComponentType.CONTENT_HORIZONTAL)

        // When/Then
        assert(strategy.canRender(descriptor.type))
    }

    @Test
    fun `strategy should handle CONTENT_SCROLL type`() {
        // Given
        val strategy = provideStrategy()
        val descriptor = provideLayoutDescriptor(ComponentType.CONTENT_SCROLL)

        // When/Then
        assert(strategy.canRender(descriptor.type))
    }

    @Test
    fun `strategy should handle CONTENT_WITH_FLOATING_BUTTON type`() {
        // Given
        val strategy = provideStrategy()
        val descriptor = provideLayoutDescriptor(ComponentType.CONTENT_WITH_FLOATING_BUTTON)

        // When/Then
        assert(strategy.canRender(descriptor.type))
    }

    @Test
    fun `strategy should handle CONTENT_LIST type`() {
        // Given
        val strategy = provideStrategy()
        val descriptor = provideLayoutDescriptor(ComponentType.CONTENT_LIST)

        // When/Then
        assert(strategy.canRender(descriptor.type))
    }

    @Test
    fun `strategy should handle CONTENT_SLIDER type`() {
        // Given
        val strategy = provideStrategy()
        val descriptor = provideLayoutDescriptor(ComponentType.CONTENT_SLIDER)

        // When/Then
        assert(strategy.canRender(descriptor.type))
    }

    // Provider functions

    private fun provideStrategy(): LayoutRendererStrategy {
        val mockFactory = mockk<ComponentFactory>(relaxed = true)
        val mockProvider = mockk<javax.inject.Provider<ComponentFactory>>()
        io.mockk.every { mockProvider.get() } returns mockFactory
        return LayoutRendererStrategy(mockProvider)
    }

    private fun provideAllLayoutTypes() = listOf(
        ComponentType.CONTENT_VERTICAL,
        ComponentType.CONTENT_HORIZONTAL,
        ComponentType.CONTENT_SCROLL,
        ComponentType.CONTENT_WITH_FLOATING_BUTTON,
        ComponentType.CONTENT_LIST,
        ComponentType.CONTENT_SLIDER
    )

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

    private fun provideLayoutDescriptor(type: ComponentType) = LayoutDescriptor(
        id = "test_layout_${type.name}",
        type = type,
        children = emptyList()
    )
}

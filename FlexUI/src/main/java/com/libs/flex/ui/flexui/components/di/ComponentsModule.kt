package com.libs.flex.ui.flexui.components.di

import com.libs.flex.ui.flexui.components.ComponentFactory
import com.libs.flex.ui.flexui.components.domain.ports.ComponentRendererStrategyPort
import com.libs.flex.ui.flexui.components.infrastructure.adapter.AtomicRendererStrategy
import com.libs.flex.ui.flexui.components.infrastructure.adapter.LayoutRendererStrategy
import com.libs.flex.ui.flexui.styling.domain.ports.StyleResolverPort
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for component rendering system dependency injection.
 *
 * This module provides bindings for the component factory and rendering
 * strategies following hexagonal architecture and Strategy Pattern.
 *
 * ## Architecture
 *
 * The ComponentFactory orchestrates rendering by:
 * - Applying styles via StyleResolverPort
 * - Delegating to ComponentRendererStrategyPort implementations
 *
 * ## Strategy Pattern
 *
 * Each strategy handles a specific category of components:
 * - LayoutRendererStrategy: Layout containers (6 types)
 * - AtomicRendererStrategy: Atomic components (9 types)
 *
 * ## Benefits
 *
 * - Open/Closed: Add new strategies without modifying factory
 * - Single Responsibility: Each strategy handles one category
 * - Testable: Mock strategies independently
 * - Extensible: Easy to add new component types
 *
 * ## Bindings
 *
 * - ComponentFactory: Orchestrates component rendering
 * - List<ComponentRendererStrategyPort>: Rendering strategies
 */
@Module
@InstallIn(SingletonComponent::class)
object ComponentsModule {

    /**
     * Provides the ComponentFactory instance.
     *
     * The factory orchestrates component rendering by applying styles
     * and delegating to appropriate strategies based on component type.
     *
     * @param styleResolver Port for applying style properties to modifiers
     * @param strategies List of rendering strategies for different component types
     * @return Configured ComponentFactory instance
     */
    @Provides
    @Singleton
    fun provideComponentFactory(
        styleResolver: StyleResolverPort,
        strategies: List<@JvmSuppressWildcards ComponentRendererStrategyPort>
    ): ComponentFactory {
        return ComponentFactory(styleResolver, strategies)
    }
    
    /**
     * Provides list of component rendering strategies.
     *
     * This function creates a list of all rendering strategies that will be
     * injected into ComponentFactory. The strategies are used to render
     * different categories of components following the Strategy Pattern.
     *
     * The order of strategies doesn't matter as selection is based on
     * the canRender() method, but they are listed in logical order:
     * 1. LayoutRendererStrategy: Handles layout containers
     * 2. AtomicRendererStrategy: Handles atomic components
     *
     * @param layoutStrategy Strategy for rendering layout containers
     * @param atomicStrategy Strategy for rendering atomic components
     * @return List of all rendering strategies
     */
    @Provides
    @Singleton
    fun provideRendererStrategies(
        layoutStrategy: LayoutRendererStrategy,
        atomicStrategy: AtomicRendererStrategy
    ): List<@JvmSuppressWildcards ComponentRendererStrategyPort> {
        return listOf(layoutStrategy, atomicStrategy)
    }
}

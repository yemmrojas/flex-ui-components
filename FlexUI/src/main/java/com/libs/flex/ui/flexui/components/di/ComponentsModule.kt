package com.libs.flex.ui.flexui.components.di

import com.libs.flex.ui.flexui.components.ComponentFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.libs.flex.ui.flexui.styling.domain.ports.StyleResolverPort
import javax.inject.Singleton

/**
 * Hilt module for component rendering system dependency injection.
 *
 * This module provides bindings for the component factory and related
 * rendering infrastructure following hexagonal architecture principles.
 *
 * ## Architecture
 *
 * The ComponentFactory acts as the central orchestrator for component rendering,
 * delegating to:
 * - StyleResolverPort: For applying styles to components
 * - Individual component implementations: For actual rendering
 *
 * ## Bindings
 *
 * - ComponentFactory: Factory for creating components from descriptors
 */
@Module
@InstallIn(SingletonComponent::class)
object ComponentsModule {

    /**
     * Provides the ComponentFactory instance.
     *
     * The factory is responsible for routing component descriptors to
     * appropriate rendering implementations and applying styles consistently.
     *
     * @param styleResolver Port for applying style properties to modifiers
     * @return Configured ComponentFactory instance
     */
    @Provides
    @Singleton
    fun provideComponentFactory(
        styleResolver: StyleResolverPort
    ): ComponentFactory {
        return ComponentFactory(styleResolver)
    }
}

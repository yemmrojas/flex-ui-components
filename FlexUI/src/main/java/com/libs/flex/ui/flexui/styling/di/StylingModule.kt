package com.libs.flex.ui.flexui.styling.di

import com.libs.flex.ui.flexui.styling.domain.ports.ColorParserPort
import com.libs.flex.ui.flexui.styling.domain.ports.DimensionParserPort
import com.libs.flex.ui.flexui.styling.domain.ports.HexFormatStrategyPort
import com.libs.flex.ui.flexui.styling.domain.ports.StyleResolverPort
import com.libs.flex.ui.flexui.styling.domain.service.StyleResolverService
import com.libs.flex.ui.flexui.styling.infrastructure.adapter.Argb8FormatStrategy
import com.libs.flex.ui.flexui.styling.infrastructure.adapter.ComposeDimensionParser
import com.libs.flex.ui.flexui.styling.infrastructure.adapter.HexColorParser
import com.libs.flex.ui.flexui.styling.infrastructure.adapter.Rgb3FormatStrategy
import com.libs.flex.ui.flexui.styling.infrastructure.adapter.Rgb6FormatStrategy
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for styling system dependency injection.
 *
 * This module provides bindings for the styling system following
 * hexagonal architecture principles (ports and adapters pattern).
 *
 * ## Architecture
 *
 * - **Ports** (domain/ports): Interfaces defining contracts
 * - **Adapters** (infrastructure/adapter): Concrete implementations
 * - **Service** (domain/service): Business logic orchestration
 *
 * ## Bindings
 *
 * - ColorParserPort -> HexColorParser
 * - DimensionParserPort -> ComposeDimensionParser
 * - StyleResolverPort -> StyleResolverService
 * - List<HexFormatStrategyPort> -> [Rgb3FormatStrategy, Rgb6FormatStrategy, Argb8FormatStrategy]
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class StylingModule {
    
    /**
     * Binds the hex color parser to the color parser port.
     *
     * This allows the domain layer to depend on the abstraction (port)
     * rather than the concrete implementation (adapter).
     */
    @Binds
    @Singleton
    abstract fun bindColorParser(
        parser: HexColorParser
    ): ColorParserPort
    
    /**
     * Binds the Compose dimension parser to the dimension parser port.
     *
     * This allows the domain layer to depend on the abstraction (port)
     * rather than the concrete implementation (adapter).
     */
    @Binds
    @Singleton
    abstract fun bindDimensionParser(
        parser: ComposeDimensionParser
    ): DimensionParserPort
    
    /**
     * Binds the style resolver service to the style resolver port.
     *
     * This service orchestrates the application of styles by delegating
     * to the color and dimension parsers through their ports.
     */
    @Binds
    @Singleton
    abstract fun bindStyleResolver(
        service: StyleResolverService
    ): StyleResolverPort
    
    companion object {
        /**
         * Provides list of hex format parsing strategies.
         *
         * This function creates a list of all supported hex color format strategies
         * that will be injected into HexColorParser. The strategies are used to
         * parse different hex color formats (RGB3, RGB6, ARGB8) following the
         * Strategy Pattern.
         *
         * The order of strategies doesn't matter as selection is based on hex length,
         * but they are listed in order of increasing complexity for clarity.
         *
         * @param rgb3 Strategy for 3-character RGB format (e.g., "F00")
         * @param rgb6 Strategy for 6-character RGB format (e.g., "FF0000")
         * @param argb8 Strategy for 8-character ARGB format (e.g., "80FF0000")
         * @return List of all hex format strategies
         */
        @Provides
        @Singleton
        fun provideHexFormatStrategies(
            rgb3: Rgb3FormatStrategy,
            rgb6: Rgb6FormatStrategy,
            argb8: Argb8FormatStrategy
        ): List<@JvmSuppressWildcards HexFormatStrategyPort> {
            return listOf(rgb3, rgb6, argb8)
        }
    }
}

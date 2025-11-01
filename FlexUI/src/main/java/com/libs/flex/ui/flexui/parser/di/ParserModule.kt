package com.libs.flex.ui.flexui.parser.di

import com.libs.flex.ui.flexui.parser.domain.ports.ComponentParserStrategyPort
import com.libs.flex.ui.flexui.parser.domain.ports.ComponentTypeMapperPort
import com.libs.flex.ui.flexui.parser.domain.ports.ParseComponentPort
import com.libs.flex.ui.flexui.parser.domain.service.JsonParserService
import com.libs.flex.ui.flexui.parser.infrastructure.adapter.AtomicParserStrategy
import com.libs.flex.ui.flexui.parser.infrastructure.adapter.LayoutParserStrategy
import com.libs.flex.ui.flexui.parser.infrastructure.mapper.ComponentMapper
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for providing parser dependencies.
 *
 * This module provides all necessary dependencies for the JSON parser,
 * following hexagonal architecture principles with proper abstraction.
 *
 * Architecture layers:
 * - Domain Ports (interfaces):
 *   - ParseComponentPort
 *   - ComponentTypeMapperPort
 *   - ComponentParserStrategyPort
 * - Application: JsonParserFacade
 * - Infrastructure (adapters):
 *   - JsonParserService
 *   - ComponentMapper
 *   - LayoutParserStrategy
 *   - AtomicParserStrategy
 *
 * All bindings use @Binds for efficiency, except where custom logic is needed.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ParserModule {
    
    /**
     * Binds JsonParserService to ParseComponentPort interface.
     *
     * Using @Binds is more efficient than @Provides for simple interface bindings.
     */
    @Binds
    @Singleton
    abstract fun bindParseComponentPort(
        service: JsonParserService
    ): ParseComponentPort
    
    /**
     * Binds ComponentMapper to ComponentTypeMapperPort interface.
     *
     * This follows the Dependency Inversion Principle:
     * - Domain depends on ComponentTypeMapperPort (abstraction)
     * - Infrastructure provides ComponentMapper (implementation)
     */
    @Binds
    @Singleton
    abstract fun bindComponentTypeMapper(
        mapper: ComponentMapper
    ): ComponentTypeMapperPort
    
    companion object {
        /**
         * Provides a list of all available parser strategies.
         *
         * @param layoutStrategy The layout parser strategy
         * @param atomicStrategy The atomic parser strategy
         * @return List of all parser strategies
         */
        @Provides
        @Singleton
        fun provideParserStrategies(
            layoutStrategy: LayoutParserStrategy,
            atomicStrategy: AtomicParserStrategy
        ): List<@JvmSuppressWildcards ComponentParserStrategyPort> {
            return listOf(layoutStrategy, atomicStrategy)
        }
    }
}

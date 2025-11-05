package com.libs.flex.ui.flexui.parser.di

import com.libs.flex.ui.flexui.parser.domain.ports.ComponentParserStrategyPort
import com.libs.flex.ui.flexui.parser.domain.ports.ComponentTypeMapperPort
import com.libs.flex.ui.flexui.parser.domain.ports.ComponentValidatorStrategyPort
import com.libs.flex.ui.flexui.parser.domain.ports.ParseComponentPort
import com.libs.flex.ui.flexui.parser.domain.ports.ValidateComponentPort
import com.libs.flex.ui.flexui.parser.domain.service.JsonParserService
import com.libs.flex.ui.flexui.parser.domain.service.ValidationEngine
import com.libs.flex.ui.flexui.parser.infrastructure.adapter.AtomicParserStrategy
import com.libs.flex.ui.flexui.parser.infrastructure.adapter.LayoutParserStrategy
import com.libs.flex.ui.flexui.parser.infrastructure.mapper.ComponentMapper
import com.libs.flex.ui.flexui.parser.infrastructure.validator.ButtonValidator
import com.libs.flex.ui.flexui.parser.infrastructure.validator.FloatingButtonLayoutValidator
import com.libs.flex.ui.flexui.parser.infrastructure.validator.ImageValidator
import com.libs.flex.ui.flexui.parser.infrastructure.validator.LayoutPropertyValidator
import com.libs.flex.ui.flexui.parser.infrastructure.validator.ListLayoutValidator
import com.libs.flex.ui.flexui.parser.infrastructure.validator.RecursiveChildrenValidator
import com.libs.flex.ui.flexui.parser.infrastructure.validator.SelectValidator
import com.libs.flex.ui.flexui.parser.infrastructure.validator.SliderCheckValidator
import com.libs.flex.ui.flexui.parser.infrastructure.validator.SliderLayoutValidator
import com.libs.flex.ui.flexui.parser.infrastructure.validator.StylePropertyValidator
import com.libs.flex.ui.flexui.parser.infrastructure.validator.TextViewValidator
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
    
    /**
     * Binds ValidationEngine to ValidateComponentPort interface.
     *
     * The ValidationEngine validates component descriptors to ensure they have
     * all required properties before rendering.
     */
    @Binds
    @Singleton
    abstract fun bindValidateComponentPort(
        engine: ValidationEngine
    ): ValidateComponentPort
    
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
        
        /**
         * Provides a list of all available validator strategies.
         *
         * This follows the Strategy Pattern, allowing easy addition of new
         * validators without modifying the ValidationEngine.
         *
         * Validators are organized by concern:
         * - Layout-specific validators (list, slider, floating button)
         * - Atomic component validators (text, button, image, select, slider check)
         * - Property validators (layout properties, style properties)
         * - Recursive validator (children and templates)
         *
         * @return List of all validator strategies
         */
        @Provides
        @Singleton
        fun provideValidatorStrategies(
            // Layout-specific validators
            listLayoutValidator: ListLayoutValidator,
            sliderLayoutValidator: SliderLayoutValidator,
            floatingButtonLayoutValidator: FloatingButtonLayoutValidator,
            
            // Atomic component validators
            textViewValidator: TextViewValidator,
            buttonValidator: ButtonValidator,
            imageValidator: ImageValidator,
            selectValidator: SelectValidator,
            sliderCheckValidator: SliderCheckValidator,
            
            // Property validators
            layoutPropertyValidator: LayoutPropertyValidator,
            stylePropertyValidator: StylePropertyValidator,
            
            // Recursive validator
            recursiveChildrenValidator: RecursiveChildrenValidator
        ): List<@JvmSuppressWildcards ComponentValidatorStrategyPort> {
            return listOf(
                // Layout-specific validators
                listLayoutValidator,
                sliderLayoutValidator,
                floatingButtonLayoutValidator,
                
                // Atomic component validators
                textViewValidator,
                buttonValidator,
                imageValidator,
                selectValidator,
                sliderCheckValidator,
                
                // Property validators
                layoutPropertyValidator,
                stylePropertyValidator,
                
                // Recursive validator (should be last to validate children after parent)
                recursiveChildrenValidator
            )
        }
    }
}

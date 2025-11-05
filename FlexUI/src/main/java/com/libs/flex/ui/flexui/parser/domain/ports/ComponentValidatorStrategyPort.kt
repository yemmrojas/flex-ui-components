package com.libs.flex.ui.flexui.parser.domain.ports

import com.libs.flex.ui.flexui.model.ComponentDescriptor

/**
 * Port (interface) for component validation strategies.
 *
 * In Hexagonal Architecture (within parser module):
 * - This is a DOMAIN PORT (interface)
 * - Defines the contract for validation strategies
 * - Part of the DOMAIN layer
 * - Implemented by infrastructure adapters
 *
 * This port follows the Strategy Pattern, allowing different validation
 * implementations for different component types without modifying the core
 * validation engine.
 *
 * Benefits:
 * - Open/Closed Principle: Add new validators without modifying existing code
 * - Single Responsibility: Each validator handles one component type
 * - Testability: Each validator can be tested independently
 */
interface ComponentValidatorStrategyPort {
    /**
     * Determines if this validator can validate the given descriptor.
     *
     * @param descriptor The component descriptor to check
     * @return true if this validator can handle the descriptor, false otherwise
     */
    fun canValidate(descriptor: ComponentDescriptor): Boolean

    /**
     * Validates the component descriptor and returns a list of error messages.
     *
     * @param descriptor The component descriptor to validate
     * @return List of validation error messages, empty if valid
     */
    fun validate(descriptor: ComponentDescriptor): List<String>
}

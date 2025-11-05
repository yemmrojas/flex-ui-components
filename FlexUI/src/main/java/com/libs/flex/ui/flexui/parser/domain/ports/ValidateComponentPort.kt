package com.libs.flex.ui.flexui.parser.domain.ports

import com.libs.flex.ui.flexui.model.ComponentDescriptor
import com.libs.flex.ui.flexui.parser.domain.model.ValidationResult

/**
 * Port (interface) for component validation operations.
 *
 * In Hexagonal Architecture (within parser module):
 * - This is an INPUT PORT (use case interface)
 * - Defines the contract for validation operations
 * - Part of the DOMAIN layer
 * - Implemented by domain services
 *
 * This port allows validation of component descriptors to ensure they have
 * all required properties and valid configurations before rendering.
 */
interface ValidateComponentPort {
    /**
     * Validates a component descriptor and its children recursively.
     *
     * This method checks:
     * - Required properties are present for the component type
     * - Property values are within valid ranges
     * - Layout-specific requirements (e.g., list containers have items)
     * - Atomic component requirements (e.g., text views have text)
     *
     * @param descriptor The component descriptor to validate
     * @return ValidationResult.Success if valid, ValidationResult.Failure with error list if invalid
     */
    fun validate(descriptor: ComponentDescriptor): ValidationResult
}

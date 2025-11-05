package com.libs.flex.ui.flexui.parser.domain.service

import com.libs.flex.ui.flexui.model.ComponentDescriptor
import com.libs.flex.ui.flexui.parser.domain.model.ValidationResult
import com.libs.flex.ui.flexui.parser.domain.ports.ComponentValidatorStrategyPort
import com.libs.flex.ui.flexui.parser.domain.ports.ValidateComponentPort
import javax.inject.Inject

/**
 * Domain Service that orchestrates component validation using strategies.
 *
 * This service validates component descriptors by delegating to specific
 * validator strategies. It follows the Strategy Pattern and Open/Closed Principle,
 * making it easy to add new validators without modifying this class.
 *
 * In Hexagonal Architecture (within parser module):
 * - This is part of the DOMAIN layer
 * - Orchestrates validation using validator strategies
 * - Implements ValidateComponentPort (input port)
 * - Depends on ComponentValidatorStrategyPort (output port)
 *
 * Benefits of this approach:
 * - Single Responsibility: Each validator handles one concern
 * - Open/Closed: Add new validators without modifying this class
 * - Testability: Each validator can be tested independently
 * - Maintainability: Changes are isolated to specific validators
 *
 * @property validators List of validation strategies to apply
 */
class ValidationEngine @Inject constructor(
    private val validators: List<@JvmSuppressWildcards ComponentValidatorStrategyPort>
) : ValidateComponentPort {

    /**
     * Validates a component descriptor using all applicable strategies.
     *
     * This method:
     * 1. Filters validators that can handle the descriptor
     * 2. Applies each validator and collects errors
     * 3. Returns success if no errors, failure otherwise
     *
     * @param descriptor The component descriptor to validate
     * @return ValidationResult.Success if valid, ValidationResult.Failure with all errors if invalid
     */
    override fun validate(descriptor: ComponentDescriptor): ValidationResult {
        val errors = validators
            .filter { it.canValidate(descriptor) }
            .flatMap { it.validate(descriptor) }

        return if (errors.isEmpty()) {
            ValidationResult.Success
        } else {
            ValidationResult.Failure(errors)
        }
    }

}

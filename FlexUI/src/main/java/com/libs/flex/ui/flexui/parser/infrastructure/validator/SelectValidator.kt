package com.libs.flex.ui.flexui.parser.infrastructure.validator

import com.libs.flex.ui.flexui.model.AtomicDescriptor
import com.libs.flex.ui.flexui.model.ComponentDescriptor
import com.libs.flex.ui.flexui.model.ComponentType
import com.libs.flex.ui.flexui.parser.domain.ports.ComponentValidatorStrategyPort
import com.libs.flex.ui.flexui.parser.infrastructure.util.ValidationUtils
import javax.inject.Inject

/**
 * Validator for COMPONENT_SELECT atomic components.
 *
 * In Hexagonal Architecture:
 * - This is an INFRASTRUCTURE ADAPTER
 * - Implements ComponentValidatorStrategyPort (domain port)
 * - Contains specific validation logic for select components
 *
 * Validation Rules:
 * - Must have 'options' property
 * - Options list must not be empty
 */
class SelectValidator @Inject constructor() : ComponentValidatorStrategyPort {

    override fun canValidate(descriptor: ComponentDescriptor): Boolean {
        return descriptor is AtomicDescriptor &&
                descriptor.type == ComponentType.COMPONENT_SELECT
    }

    override fun validate(descriptor: ComponentDescriptor): List<String> {
        val atomic = descriptor as AtomicDescriptor

        return ValidationUtils.validateNonEmptyList(
            list = atomic.options,
            propertyName = "options",
            componentType = "componentSelect",
            componentId = atomic.id
        )
    }
}

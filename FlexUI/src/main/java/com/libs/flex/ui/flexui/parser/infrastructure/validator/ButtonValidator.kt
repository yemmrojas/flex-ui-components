package com.libs.flex.ui.flexui.parser.infrastructure.validator

import com.libs.flex.ui.flexui.model.AtomicDescriptor
import com.libs.flex.ui.flexui.model.ComponentDescriptor
import com.libs.flex.ui.flexui.model.ComponentType
import com.libs.flex.ui.flexui.parser.domain.ports.ComponentValidatorStrategyPort
import com.libs.flex.ui.flexui.parser.infrastructure.util.validateAsRequired
import javax.inject.Inject

/**
 * Validator for COMPONENT_BUTTON atomic components.
 *
 * In Hexagonal Architecture:
 * - This is an INFRASTRUCTURE ADAPTER
 * - Implements ComponentValidatorStrategyPort (domain port)
 * - Contains specific validation logic for button components
 *
 * Validation Rules:
 * - Must have 'text' property
 */
class ButtonValidator @Inject constructor() : ComponentValidatorStrategyPort {

    override fun canValidate(descriptor: ComponentDescriptor): Boolean {
        return descriptor is AtomicDescriptor &&
                descriptor.type == ComponentType.COMPONENT_BUTTON
    }

    override fun validate(descriptor: ComponentDescriptor): List<String> {
        val atomic = descriptor as AtomicDescriptor

        return buildList {
            atomic.text.validateAsRequired(
                propertyName = "text",
                componentType = "componentButton",
                componentId = atomic.id
            )?.let { add(it) }
        }
    }
}

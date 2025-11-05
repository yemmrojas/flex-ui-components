package com.libs.flex.ui.flexui.parser.infrastructure.validator

import com.libs.flex.ui.flexui.model.AtomicDescriptor
import com.libs.flex.ui.flexui.model.ComponentDescriptor
import com.libs.flex.ui.flexui.model.ComponentType
import com.libs.flex.ui.flexui.parser.domain.ports.ComponentValidatorStrategyPort
import com.libs.flex.ui.flexui.parser.infrastructure.util.validateAsMinInRange
import com.libs.flex.ui.flexui.parser.infrastructure.util.validateAsRequired
import javax.inject.Inject

/**
 * Validator for COMPONENT_SLIDER_CHECK atomic components.
 *
 * In Hexagonal Architecture:
 * - This is an INFRASTRUCTURE ADAPTER
 * - Implements ComponentValidatorStrategyPort (domain port)
 * - Contains specific validation logic for slider check components
 *
 * Validation Rules:
 * - Must have 'min' property
 * - Must have 'max' property
 * - min must be less than max
 */
class SliderCheckValidator @Inject constructor() : ComponentValidatorStrategyPort {

    override fun canValidate(descriptor: ComponentDescriptor): Boolean {
        return descriptor is AtomicDescriptor &&
                descriptor.type == ComponentType.COMPONENT_SLIDER_CHECK
    }

    override fun validate(descriptor: ComponentDescriptor): List<String> {
        val atomic = descriptor as AtomicDescriptor

        return buildList {
            // Validate min
            atomic.min.validateAsRequired(
                propertyName = "min",
                componentType = "componentSliderCheck",
                componentId = atomic.id
            )?.let { add(it) }

            // Validate max
            atomic.max.validateAsRequired(
                propertyName = "max",
                componentType = "componentSliderCheck",
                componentId = atomic.id
            )?.let { add(it) }

            // Validate range if both are present
            if (atomic.min != null && atomic.max != null) {
                atomic.min.validateAsMinInRange(
                    max = atomic.max,
                    componentId = atomic.id
                )?.let { add(it) }
            }
        }
    }
}

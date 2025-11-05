package com.libs.flex.ui.flexui.parser.infrastructure.validator

import com.libs.flex.ui.flexui.model.ComponentDescriptor
import com.libs.flex.ui.flexui.model.ComponentType
import com.libs.flex.ui.flexui.model.LayoutDescriptor
import com.libs.flex.ui.flexui.parser.domain.ports.ComponentValidatorStrategyPort
import com.libs.flex.ui.flexui.parser.infrastructure.util.ValidationUtils
import javax.inject.Inject

/**
 * Validator for CONTENT_SLIDER layout components.
 *
 * In Hexagonal Architecture:
 * - This is an INFRASTRUCTURE ADAPTER
 * - Implements ComponentValidatorStrategyPort (domain port)
 * - Contains specific validation logic for slider layouts
 *
 * Validation Rules:
 * - Must have 'items' property
 * - Items array must not be empty
 * - If autoPlay is true, must have autoPlayInterval
 */
class SliderLayoutValidator @Inject constructor() : ComponentValidatorStrategyPort {

    override fun canValidate(descriptor: ComponentDescriptor): Boolean {
        return descriptor is LayoutDescriptor && descriptor.type == ComponentType.CONTENT_SLIDER
    }

    override fun validate(descriptor: ComponentDescriptor): List<String> {
        val layout = descriptor as LayoutDescriptor

        return buildList {
            // Validate items
            addAll(
                ValidationUtils.validateNonEmptyList(
                    list = layout.items,
                    propertyName = "items",
                    componentType = "contentSlider",
                    componentId = layout.id
                )
            )

            // Validate autoPlay configuration
            if (layout.autoPlay == true && layout.autoPlayInterval == null) {
                add("Component '${layout.id}' of type 'contentSlider' with autoPlay=true requires 'autoPlayInterval' property")
            }
        }
    }
}

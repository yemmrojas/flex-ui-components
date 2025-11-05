package com.libs.flex.ui.flexui.parser.infrastructure.validator

import com.libs.flex.ui.flexui.model.ComponentDescriptor
import com.libs.flex.ui.flexui.model.ComponentType
import com.libs.flex.ui.flexui.model.LayoutDescriptor
import com.libs.flex.ui.flexui.parser.domain.ports.ComponentValidatorStrategyPort
import com.libs.flex.ui.flexui.parser.infrastructure.util.ValidationUtils
import javax.inject.Inject

/**
 * Validator for CONTENT_LIST layout components.
 *
 * In Hexagonal Architecture:
 * - This is an INFRASTRUCTURE ADAPTER
 * - Implements ComponentValidatorStrategyPort (domain port)
 * - Contains specific validation logic for list layouts
 *
 * Validation Rules:
 * - Must have 'items' property
 * - Items array must not be empty
 */
class ListLayoutValidator @Inject constructor() : ComponentValidatorStrategyPort {

    override fun canValidate(descriptor: ComponentDescriptor): Boolean {
        return descriptor is LayoutDescriptor && descriptor.type == ComponentType.CONTENT_LIST
    }

    override fun validate(descriptor: ComponentDescriptor): List<String> {
        val layout = descriptor as LayoutDescriptor
        return ValidationUtils.validateNonEmptyList(
            list = layout.items,
            propertyName = "items",
            componentType = "contentList",
            componentId = layout.id
        )
    }
}

package com.libs.flex.ui.flexui.parser.infrastructure.validator

import com.libs.flex.ui.flexui.model.ComponentDescriptor
import com.libs.flex.ui.flexui.model.ComponentType
import com.libs.flex.ui.flexui.model.LayoutDescriptor
import com.libs.flex.ui.flexui.parser.domain.ports.ComponentValidatorStrategyPort
import com.libs.flex.ui.flexui.parser.infrastructure.util.validateAsRequired
import javax.inject.Inject

/**
 * Validator for CONTENT_WITH_FLOATING_BUTTON layout components.
 *
 * In Hexagonal Architecture:
 * - This is an INFRASTRUCTURE ADAPTER
 * - Implements ComponentValidatorStrategyPort (domain port)
 * - Contains specific validation logic for floating button layouts
 *
 * Validation Rules:
 * - Must have 'fabIcon' property
 */
class FloatingButtonLayoutValidator @Inject constructor() : ComponentValidatorStrategyPort {

    override fun canValidate(descriptor: ComponentDescriptor): Boolean {
        return descriptor is LayoutDescriptor &&
                descriptor.type == ComponentType.CONTENT_WITH_FLOATING_BUTTON
    }

    override fun validate(descriptor: ComponentDescriptor): List<String> {
        val layout = descriptor as LayoutDescriptor

        return buildList {
            layout.fabIcon.validateAsRequired(
                propertyName = "fabIcon",
                componentType = "contentWithFloatingButton",
                componentId = layout.id
            )?.let { add(it) }
        }
    }
}

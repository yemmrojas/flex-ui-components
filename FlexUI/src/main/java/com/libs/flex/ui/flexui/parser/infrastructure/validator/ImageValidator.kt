package com.libs.flex.ui.flexui.parser.infrastructure.validator

import com.libs.flex.ui.flexui.model.AtomicDescriptor
import com.libs.flex.ui.flexui.model.ComponentDescriptor
import com.libs.flex.ui.flexui.model.ComponentType
import com.libs.flex.ui.flexui.parser.domain.ports.ComponentValidatorStrategyPort
import com.libs.flex.ui.flexui.parser.infrastructure.util.validateAsRequired
import javax.inject.Inject

/**
 * Validator for COMPONENT_IMAGE atomic components.
 *
 * In Hexagonal Architecture:
 * - This is an INFRASTRUCTURE ADAPTER
 * - Implements ComponentValidatorStrategyPort (domain port)
 * - Contains specific validation logic for image components
 *
 * Validation Rules:
 * - Must have 'imageUrl' property
 */
class ImageValidator @Inject constructor() : ComponentValidatorStrategyPort {

    override fun canValidate(descriptor: ComponentDescriptor): Boolean {
        return descriptor is AtomicDescriptor &&
                descriptor.type == ComponentType.COMPONENT_IMAGE
    }

    override fun validate(descriptor: ComponentDescriptor): List<String> {
        val atomic = descriptor as AtomicDescriptor

        return buildList {
            atomic.imageUrl.validateAsRequired(
                propertyName = "imageUrl",
                componentType = "componentImage",
                componentId = atomic.id
            )?.let { add(it) }
        }
    }
}

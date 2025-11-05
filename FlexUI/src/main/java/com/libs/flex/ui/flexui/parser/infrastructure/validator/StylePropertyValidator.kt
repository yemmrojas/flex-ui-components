package com.libs.flex.ui.flexui.parser.infrastructure.validator

import com.libs.flex.ui.flexui.model.AtomicDescriptor
import com.libs.flex.ui.flexui.model.ComponentDescriptor
import com.libs.flex.ui.flexui.parser.domain.model.ValidationConstants
import com.libs.flex.ui.flexui.parser.domain.ports.ComponentValidatorStrategyPort
import com.libs.flex.ui.flexui.parser.infrastructure.util.validateAsEnum
import com.libs.flex.ui.flexui.parser.infrastructure.util.validateAsHexColor
import com.libs.flex.ui.flexui.parser.infrastructure.util.validateAsPositive
import javax.inject.Inject

/**
 * Validator for atomic component style properties.
 *
 * In Hexagonal Architecture:
 * - This is an INFRASTRUCTURE ADAPTER
 * - Implements ComponentValidatorStrategyPort (domain port)
 * - Contains validation logic for style-related properties
 *
 * Validation Rules:
 * - textStyle must be in valid set if specified
 * - inputStyle must be in valid set if specified
 * - buttonStyle must be in valid set if specified
 * - contentScale must be in valid set if specified
 * - loaderStyle must be in valid set if specified
 * - fontSize must be positive if specified
 * - maxLines must be positive if specified
 * - size must be positive if specified
 * - color must be valid hex format if specified
 */
class StylePropertyValidator @Inject constructor() : ComponentValidatorStrategyPort {

    override fun canValidate(descriptor: ComponentDescriptor): Boolean {
        return descriptor is AtomicDescriptor
    }

    override fun validate(descriptor: ComponentDescriptor): List<String> {
        val atomic = descriptor as AtomicDescriptor

        return buildList {
            // Validate textStyle
            atomic.textStyle?.let { style ->
                style.validateAsEnum(
                    validValues = ValidationConstants.VALID_TEXT_STYLES,
                    propertyName = "textStyle",
                    componentId = atomic.id
                )?.let { add(it) }
            }

            // Validate inputStyle
            atomic.inputStyle?.let { style ->
                style.validateAsEnum(
                    validValues = ValidationConstants.VALID_INPUT_STYLES,
                    propertyName = "inputStyle",
                    componentId = atomic.id
                )?.let { add(it) }
            }

            // Validate buttonStyle
            atomic.buttonStyle?.let { style ->
                style.validateAsEnum(
                    validValues = ValidationConstants.VALID_BUTTON_STYLES,
                    propertyName = "buttonStyle",
                    componentId = atomic.id
                )?.let { add(it) }
            }

            // Validate contentScale
            atomic.contentScale?.let { scale ->
                scale.validateAsEnum(
                    validValues = ValidationConstants.VALID_CONTENT_SCALES,
                    propertyName = "contentScale",
                    componentId = atomic.id
                )?.let { add(it) }
            }

            // Validate loaderStyle
            atomic.loaderStyle?.let { style ->
                style.validateAsEnum(
                    validValues = ValidationConstants.VALID_LOADER_STYLES,
                    propertyName = "loaderStyle",
                    componentId = atomic.id
                )?.let { add(it) }
            }

            // Validate fontSize
            atomic.fontSize?.let { size ->
                size.validateAsPositive(
                    propertyName = "fontSize",
                    componentId = atomic.id
                )?.let { add(it) }
            }

            // Validate maxLines
            atomic.maxLines?.let { lines ->
                lines.validateAsPositive(
                    propertyName = "maxLines",
                    componentId = atomic.id
                )?.let { add(it) }
            }

            // Validate size
            atomic.size?.let { size ->
                size.validateAsPositive(
                    propertyName = "size",
                    componentId = atomic.id
                )?.let { add(it) }
            }

            // Validate color
            atomic.color?.let { color ->
                color.validateAsHexColor(
                    componentId = atomic.id
                )?.let { add(it) }
            }
        }
    }
}

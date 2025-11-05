package com.libs.flex.ui.flexui.parser.infrastructure.validator

import com.libs.flex.ui.flexui.model.ComponentDescriptor
import com.libs.flex.ui.flexui.model.LayoutDescriptor
import com.libs.flex.ui.flexui.parser.domain.model.ValidationConstants
import com.libs.flex.ui.flexui.parser.domain.ports.ComponentValidatorStrategyPort
import com.libs.flex.ui.flexui.parser.infrastructure.util.ValidationUtils
import javax.inject.Inject

/**
 * Validator for layout component properties (arrangement, alignment, etc.).
 *
 * In Hexagonal Architecture:
 * - This is an INFRASTRUCTURE ADAPTER
 * - Implements ComponentValidatorStrategyPort (domain port)
 * - Contains validation logic for common layout properties
 *
 * Validation Rules:
 * - arrangement must be in valid set if specified
 * - alignment must be in valid set if specified
 * - scrollDirection must be in valid set if specified
 * - fabPosition must be in valid set if specified
 */
class LayoutPropertyValidator @Inject constructor() : ComponentValidatorStrategyPort {

    override fun canValidate(descriptor: ComponentDescriptor): Boolean {
        return descriptor is LayoutDescriptor
    }

    override fun validate(descriptor: ComponentDescriptor): List<String> {
        val layout = descriptor as LayoutDescriptor

        return buildList {
            // Validate arrangement
            layout.arrangement?.let { arrangement ->
                ValidationUtils.validateEnum(
                    value = arrangement,
                    validValues = ValidationConstants.VALID_ARRANGEMENTS,
                    propertyName = "arrangement",
                    componentId = layout.id
                )?.let { add(it) }
            }

            // Validate alignment
            layout.alignment?.let { alignment ->
                ValidationUtils.validateEnum(
                    value = alignment,
                    validValues = ValidationConstants.VALID_ALIGNMENTS,
                    propertyName = "alignment",
                    componentId = layout.id
                )?.let { add(it) }
            }

            // Validate scrollDirection
            layout.scrollDirection?.let { direction ->
                ValidationUtils.validateEnum(
                    value = direction,
                    validValues = ValidationConstants.VALID_SCROLL_DIRECTIONS,
                    propertyName = "scrollDirection",
                    componentId = layout.id
                )?.let { add(it) }
            }

            // Validate fabPosition
            layout.fabPosition?.let { position ->
                ValidationUtils.validateEnum(
                    value = position,
                    validValues = ValidationConstants.VALID_FAB_POSITIONS,
                    propertyName = "fabPosition",
                    componentId = layout.id
                )?.let { add(it) }
            }
        }
    }
}

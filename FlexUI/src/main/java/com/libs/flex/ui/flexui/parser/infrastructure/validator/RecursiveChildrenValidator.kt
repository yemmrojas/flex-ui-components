package com.libs.flex.ui.flexui.parser.infrastructure.validator

import com.libs.flex.ui.flexui.model.ComponentDescriptor
import com.libs.flex.ui.flexui.model.LayoutDescriptor
import com.libs.flex.ui.flexui.parser.domain.ports.ComponentValidatorStrategyPort
import com.libs.flex.ui.flexui.parser.domain.ports.ValidateComponentPort
import javax.inject.Inject

/**
 * Validator for recursively validating children and templates.
 *
 * In Hexagonal Architecture:
 * - This is an INFRASTRUCTURE ADAPTER
 * - Implements ComponentValidatorStrategyPort (domain port)
 * - Delegates to ValidateComponentPort for recursive validation
 *
 * Validation Rules:
 * - Validates all children recursively
 * - Validates itemTemplate if present
 */
class RecursiveChildrenValidator @Inject constructor(
    private val validateComponentPort: ValidateComponentPort
) : ComponentValidatorStrategyPort {
    
    override fun canValidate(descriptor: ComponentDescriptor): Boolean {
        return descriptor is LayoutDescriptor
    }
    
    override fun validate(descriptor: ComponentDescriptor): List<String> {
        val layout = descriptor as LayoutDescriptor
        
        return buildList {
            // Validate children recursively
            layout.children.forEach { child ->
                val childResult = validateComponentPort.validate(child)
                if (childResult.isFailure()) {
                    addAll(childResult.errorList())
                }
            }
            
            // Validate itemTemplate recursively
            layout.itemTemplate?.let { template ->
                val templateResult = validateComponentPort.validate(template)
                if (templateResult.isFailure()) {
                    addAll(templateResult.errorList())
                }
            }
        }
    }
}

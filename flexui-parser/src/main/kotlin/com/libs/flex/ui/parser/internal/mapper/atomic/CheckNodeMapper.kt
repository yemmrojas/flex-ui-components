package com.libs.flex.ui.parser.internal.mapper.atomic

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.functional.zip
import com.libs.flex.ui.core.domain.model.node.CheckNode
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.internal.mapper.NodeMapper
import com.libs.flex.ui.parser.internal.model.MappedNode
import com.libs.flex.ui.parser.internal.reader.PropertyReader
import com.libs.flex.ui.parser.internal.scope.MappingScope
import com.libs.flex.ui.parser.internal.util.Quad

/**
 * Mapeador atómico para casillas de verificación ([CheckNode]).
 *
 * Soporta el tipo oficial `flexCheck` y los aliases `checkbox`, `componentCheckbox`, `componentCheck` y `check`.
 */
internal class CheckNodeMapper : NodeMapper {

    override fun map(reader: PropertyReader, scope: MappingScope): Either<Nel<ParserError>, MappedNode> {
        val commonResult = reader.readCommonProperties()
        val labelResult = reader.requiredString("label", "text")
        val defaultCheckedResult = reader.optionalBoolean("defaultChecked", "checked", "isChecked", default = false)
        val rulesResult = reader.readValidationRules("validationRules", "validations", "rules")
        val isSensitiveResult = reader.optionalBoolean("isSensitive", "sensitive", default = false)

        return commonResult
            .zip(labelResult) { c, l -> Pair(c, l) }
            .zip(defaultCheckedResult) { (c, l), ch -> Triple(c, l, ch) }
            .zip(rulesResult) { (c, l, ch), r -> Quad(c, l, ch, r) }
            .zip(isSensitiveResult) { (c, l, ch, r), s ->
                MappedNode(
                    CheckNode(
                        id = c.id,
                        style = c.style,
                        label = l,
                        defaultChecked = ch,
                        validationRules = r,
                        isSensitive = s
                    )
                )
            }
    }

    companion object {
        val SUPPORTED_TYPES: Set<String> = setOf("flexCheck", "checkbox", "componentCheckbox", "componentCheck", "check")
    }
}

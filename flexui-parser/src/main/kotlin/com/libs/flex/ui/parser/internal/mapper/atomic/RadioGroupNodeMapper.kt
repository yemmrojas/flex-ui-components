package com.libs.flex.ui.parser.internal.mapper.atomic

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.functional.zip
import com.libs.flex.ui.core.domain.model.node.SelectNode
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.internal.mapper.NodeMapper
import com.libs.flex.ui.parser.internal.model.MappedNode
import com.libs.flex.ui.parser.internal.reader.PropertyReader
import com.libs.flex.ui.parser.internal.scope.MappingScope

/**
 * Mapeador atómico para grupos de opciones de selección única / radio buttons ([SelectNode]).
 *
 * Soporta el tipo oficial `flexRadioGroup` y los aliases `radioGroup` y `componentRadioGroup`.
 */
internal class RadioGroupNodeMapper : NodeMapper {

    override val types: Set<String> = setOf("flexRadioGroup", "radioGroup", "componentRadioGroup", "radio")

    override fun map(
        reader: PropertyReader,
        scope: MappingScope
    ): Either<Nel<ParserError>, MappedNode> {
        val commonResult = reader.readCommonProperties()
        val labelResult = reader.requiredString("label", "title")
        val optionsResult = reader.readSelectOptions("options", "items")
        val rulesResult = reader.readValidationRules("validationRules", "validations", "rules")

        return commonResult
            .zip(labelResult) { c, l -> Pair(c, l) }
            .zip(optionsResult) { (c, l), opt -> Triple(c, l, opt) }
            .zip(rulesResult) { (c, l, opt), r ->
                MappedNode(
                    SelectNode.of(
                        id = c.id,
                        style = c.style,
                        label = l,
                        options = opt,
                        validationRules = r
                    )
                )
            }
    }
}

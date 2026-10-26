package com.libs.flex.ui.parser.internal.mapper.atomic

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.functional.zip
import com.libs.flex.ui.core.domain.model.node.SliderNode
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.internal.mapper.NodeMapper
import com.libs.flex.ui.parser.internal.model.MappedNode
import com.libs.flex.ui.parser.internal.reader.PropertyReader
import com.libs.flex.ui.parser.internal.scope.MappingScope
import com.libs.flex.ui.parser.internal.util.Quad

/**
 * Mapeador atómico para controles deslizantes numéricos ([SliderNode]).
 *
 * Soporta `flexSliderCheck`, `componentSliderCheck`, `slider` y `flexSlider`.
 */
internal class SliderNodeMapper : NodeMapper {

    override fun map(
        reader: PropertyReader,
        scope: MappingScope
    ): Either<Nel<ParserError>, MappedNode> {
        val commonResult = reader.readCommonProperties()
        val rangeResult = reader.readNumberRange(minKey = "min", maxKey = "max", stepKey = "step")
        val stepResult = reader.optionalFloat("step", default = 1f)
        val rulesResult = reader.readValidationRules("validationRules", "validations", "rules")
        val isSensitiveResult = reader.optionalBoolean("isSensitive", default = false)

        return commonResult
            .zip(rangeResult) { c, r -> Pair(c, r) }
            .zip(stepResult) { (c, r), s -> Triple(c, r, s?.toDouble() ?: 1.0) }
            .zip(rulesResult) { (c, r, s), rules -> Quad(c, r, s, rules) }
            .zip(isSensitiveResult) { (c, r, s, rules), isSensitive ->
                MappedNode(
                    SliderNode(
                        id = c.id,
                        style = c.style,
                        range = r,
                        step = s,
                        validationRules = rules,
                        isSensitive = isSensitive
                    )
                )
            }
    }

    companion object {
        val SUPPORTED_TYPES: Set<String> = setOf("flexSliderCheck", "componentSliderCheck", "slider", "flexSlider")
    }
}

package com.libs.flex.ui.parser.internal.mapper.atomic

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.functional.zip
import com.libs.flex.ui.core.domain.model.node.InputNode
import com.libs.flex.ui.core.domain.value.InputVariant
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.internal.mapper.NodeMapper
import com.libs.flex.ui.parser.internal.model.MappedNode
import com.libs.flex.ui.parser.internal.reader.PropertyReader
import com.libs.flex.ui.parser.internal.scope.MappingScope
import com.libs.flex.ui.parser.internal.util.Quad
import com.libs.flex.ui.parser.internal.util.Quint

/**
 * Mapeador atómico para campos de entrada de texto ([InputNode]).
 *
 * Soporta el tipo oficial `flexInput` y los aliases heredados `input`, `componentTextInput` y `componentInput`.
 */
internal class InputNodeMapper : NodeMapper {

    override val types: Set<String> = setOf("flexInput", "input", "componentTextInput", "componentInput")

    override fun map(
        reader: PropertyReader,
        scope: MappingScope
    ): Either<Nel<ParserError>, MappedNode> {
        val commonResult = reader.readCommonProperties()
        val labelResult = reader.requiredString("label", "placeholder")
        val placeholderResult = reader.optionalString("placeholder", "hint")
        val variantResult = reader.optionalString("inputVariant", "inputStyle", "variant", default = "outlined")
        val isSensitiveResult = reader.optionalBoolean("isSensitive", "sensitive", default = false)
        val rulesResult = reader.readValidationRules("validationRules", "validations", "rules")

        return commonResult
            .zip(labelResult) { c, l -> Pair(c, l) }
            .zip(placeholderResult) { (c, l), p -> Triple(c, l, p) }
            .zip(variantResult) { (c, l, p), v -> Quad(c, l, p, InputVariant.fromString(v ?: "outlined")) }
            .zip(isSensitiveResult) { (c, l, p, v), s -> Quint(c, l, p, v, s) }
            .zip(rulesResult) { (c, l, p, v, s), r ->
                val effectiveSensitive = s || (v == InputVariant.Password)
                MappedNode(
                    InputNode(
                        id = c.id,
                        style = c.style,
                        label = l,
                        placeholder = p,
                        inputVariant = v,
                        validationRules = r,
                        isSensitive = effectiveSensitive
                    )
                )
            }
    }
}

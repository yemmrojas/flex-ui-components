package com.libs.flex.ui.parser.internal.mapper.atomic

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.functional.zip
import com.libs.flex.ui.core.domain.model.node.CustomNode
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.internal.mapper.NodeMapper
import com.libs.flex.ui.parser.internal.model.MappedNode
import com.libs.flex.ui.parser.internal.reader.PropertyReader
import com.libs.flex.ui.parser.internal.scope.MappingScope

/**
 * Mapeador para componentes personalizados / extensibles ([CustomNode]).
 *
 * Soporta el tipo oficial `flexCustom` y el alias `custom`.
 */
internal class CustomNodeMapper : NodeMapper {

    override val types: Set<String> = setOf("flexCustom", "custom")

    override fun map(reader: PropertyReader, scope: MappingScope): Either<Nel<ParserError>, MappedNode> {
        val commonResult = reader.readCommonProperties()
        val typeResult = reader.requiredString("type")

        return commonResult.zip(typeResult) { c, t ->
            MappedNode(
                CustomNode(
                    id = c.id,
                    style = c.style,
                    type = t,
                    properties = emptyMap()
                )
            )
        }
    }
}

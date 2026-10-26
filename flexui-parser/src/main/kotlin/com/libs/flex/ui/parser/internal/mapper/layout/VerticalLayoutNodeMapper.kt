package com.libs.flex.ui.parser.internal.mapper.layout

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.functional.zip
import com.libs.flex.ui.core.domain.model.node.VerticalLayoutNode
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.internal.mapper.NodeMapper
import com.libs.flex.ui.parser.internal.model.MappedNode
import com.libs.flex.ui.parser.internal.reader.PropertyReader
import com.libs.flex.ui.parser.internal.scope.MappingScope

private const val PRIMARY_TYPE = "flexVertical"
private val ALIASES = setOf("contentVertical", "vertical")

/**
 * Mapeador para contenedores de disposición vertical ([VerticalLayoutNode]).
 *
 * Mapea recursivamente sus nodos hijos y acumula plantillas encontradas.
 */
internal class VerticalLayoutNodeMapper : NodeMapper {

    override fun map(
        reader: PropertyReader,
        scope: MappingScope
    ): Either<Nel<ParserError>, MappedNode> {
        val commonResult = reader.readCommonProperties()
        val childrenResult = reader.readChildren(scope)

        return commonResult.zip(childrenResult) { common, children ->
            MappedNode(
                node = VerticalLayoutNode(
                    id = common.id,
                    style = common.style,
                    children = children.children
                ),
                templates = children.templates
            )
        }
    }

    companion object {
        val SUPPORTED_TYPES: Set<String> = setOf(PRIMARY_TYPE) + ALIASES
    }
}

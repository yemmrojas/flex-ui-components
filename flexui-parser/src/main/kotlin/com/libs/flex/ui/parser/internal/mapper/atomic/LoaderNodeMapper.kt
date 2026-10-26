package com.libs.flex.ui.parser.internal.mapper.atomic

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.functional.zip
import com.libs.flex.ui.core.domain.model.node.LoaderNode
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.internal.mapper.NodeMapper
import com.libs.flex.ui.parser.internal.model.MappedNode
import com.libs.flex.ui.parser.internal.reader.PropertyReader
import com.libs.flex.ui.parser.internal.scope.MappingScope

/**
 * Mapeador atómico para indicadores de progreso y carga ([LoaderNode]).
 *
 * Soporta el tipo oficial `flexLoader` y los aliases `componentLoader` y `loader`.
 */
internal class LoaderNodeMapper : NodeMapper {

    override fun map(
        reader: PropertyReader,
        scope: MappingScope
    ): Either<Nel<ParserError>, MappedNode> {
        val commonResult = reader.readCommonProperties()
        val variantResult = reader.optionalString("variant", default = "circular")
        val sizeResult = reader.optionalString("size", default = "medium")

        return commonResult
            .zip(variantResult) { c, v -> Pair(c, v) }
            .zip(sizeResult) { (c, v), s ->
                MappedNode(
                    LoaderNode(
                        id = c.id,
                        style = c.style,
                        variant = v ?: "circular",
                        size = s ?: "medium"
                    )
                )
            }
    }

    companion object {
        val SUPPORTED_TYPES: Set<String> = setOf("flexLoader", "componentLoader", "loader")
    }
}

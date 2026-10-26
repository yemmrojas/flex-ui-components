package com.libs.flex.ui.parser.internal.mapper.layout

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.functional.zip
import com.libs.flex.ui.core.domain.model.node.ScrollLayoutNode
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.internal.mapper.NodeMapper
import com.libs.flex.ui.parser.internal.model.MappedNode
import com.libs.flex.ui.parser.internal.reader.PropertyReader
import com.libs.flex.ui.parser.internal.scope.MappingScope

private const val PRIMARY_TYPE = "flexScroll"
private val ALIASES = setOf("contentScroll", "scroll")

private const val KEY_ORIENTATION = "orientation"
private const val DEFAULT_ORIENTATION = "vertical"

/**
 * Mapeador para contenedores con scroll ([ScrollLayoutNode]).
 */
internal class ScrollLayoutNodeMapper : NodeMapper {

    override fun map(
        reader: PropertyReader,
        scope: MappingScope
    ): Either<Nel<ParserError>, MappedNode> {
        val commonResult = reader.readCommonProperties()
        val orientationResult = reader.optionalString(KEY_ORIENTATION, default = DEFAULT_ORIENTATION)
        val childrenResult = reader.readChildren(scope)

        return commonResult
            .zip(orientationResult) { common, orientation -> Pair(common, orientation ?: DEFAULT_ORIENTATION) }
            .zip(childrenResult) { (common, orientation), children ->
                MappedNode(
                    node = ScrollLayoutNode(
                        id = common.id,
                        style = common.style,
                        orientation = orientation,
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

package com.libs.flex.ui.parser.internal.mapper.atomic

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.functional.zip
import com.libs.flex.ui.core.domain.model.node.ToastNode
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.internal.mapper.NodeMapper
import com.libs.flex.ui.parser.internal.model.MappedNode
import com.libs.flex.ui.parser.internal.reader.PropertyReader
import com.libs.flex.ui.parser.internal.scope.MappingScope

/**
 * Mapeador atómico para notificaciones toast ([ToastNode]).
 *
 * Soporta el tipo oficial `flexToast` y los aliases `componentToast` y `toast`.
 */
internal class ToastNodeMapper : NodeMapper {

    override fun map(
        reader: PropertyReader,
        scope: MappingScope
    ): Either<Nel<ParserError>, MappedNode> {
        val commonResult = reader.readCommonProperties()
        val messageResult = reader.requiredString("message", "text")
        val typeResult = reader.optionalString("type", default = "info")
        val durationResult = reader.optionalInt("durationMs", "duration", default = 3000)

        return commonResult
            .zip(messageResult) { c, m -> Pair(c, m) }
            .zip(typeResult) { (c, m), t -> Triple(c, m, t) }
            .zip(durationResult) { (c, m, t), d ->
                MappedNode(
                    ToastNode(
                        id = c.id,
                        style = c.style,
                        message = m,
                        type = t ?: "info",
                        durationMs = (d ?: 3000).toLong()
                    )
                )
            }
    }

    companion object {
        val SUPPORTED_TYPES: Set<String> = setOf("flexToast", "componentToast", "toast")
    }
}

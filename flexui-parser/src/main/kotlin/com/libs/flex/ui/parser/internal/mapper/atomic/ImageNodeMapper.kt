package com.libs.flex.ui.parser.internal.mapper.atomic

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.functional.zip
import com.libs.flex.ui.core.domain.model.node.ImageNode
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.internal.mapper.NodeMapper
import com.libs.flex.ui.parser.internal.model.MappedNode
import com.libs.flex.ui.parser.internal.reader.PropertyReader
import com.libs.flex.ui.parser.internal.scope.MappingScope

/**
 * Mapeador atómico para imágenes ([ImageNode]).
 *
 * Soporta el tipo oficial `flexImage` y los aliases `image`, `componentImageView` y `componentImage`.
 */
internal class ImageNodeMapper : NodeMapper {

    override val types: Set<String> = setOf("flexImage", "image", "componentImageView", "componentImage")

    override fun map(reader: PropertyReader, scope: MappingScope): Either<Nel<ParserError>, MappedNode> {
        val commonResult = reader.readCommonProperties()
        val urlResult = reader.requiredString("url", "src", "imageUrl")
        val contentDescResult = reader.optionalString("contentDescription", "alt", "description")
        val scaleModeResult = reader.optionalString("scaleMode", "scaleType", default = "fit")

        return commonResult
            .zip(urlResult) { c, u -> Pair(c, u) }
            .zip(contentDescResult) { (c, u), d -> Triple(c, u, d) }
            .zip(scaleModeResult) { (c, u, d), s ->
                MappedNode(
                    ImageNode(
                        id = c.id,
                        style = c.style,
                        url = u,
                        contentDescription = d,
                        scaleMode = s ?: "fit"
                    )
                )
            }
    }
}

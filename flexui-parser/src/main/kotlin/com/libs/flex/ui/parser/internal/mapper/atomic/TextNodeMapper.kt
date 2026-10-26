package com.libs.flex.ui.parser.internal.mapper.atomic

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.functional.zip
import com.libs.flex.ui.core.domain.model.node.TextNode
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.internal.mapper.NodeMapper
import com.libs.flex.ui.parser.internal.model.MappedNode
import com.libs.flex.ui.parser.internal.reader.PropertyReader
import com.libs.flex.ui.parser.internal.scope.MappingScope
import com.libs.flex.ui.parser.internal.util.Quad

/**
 * Mapeador atómico para componentes de texto ([TextNode]).
 *
 * Soporta el tipo oficial `flexText` y los aliases heredados `componentTextView` y `text`.
 */
internal class TextNodeMapper : NodeMapper {

    override fun map(
        reader: PropertyReader,
        scope: MappingScope
    ): Either<Nel<ParserError>, MappedNode> {
        val commonResult = reader.readCommonProperties()
        val textResult = reader.requiredString("text")
        val typographyResult = reader.optionalString("typography", "textStyle", default = "body1")
        val fontSizeResult = reader.optionalFontSize("fontSize")
        val maxLinesResult = reader.optionalInt("maxLines")

        return commonResult
            .zip(textResult) { common, text -> Pair(common, text) }
            .zip(typographyResult) { (common, text), typo -> Triple(common, text, typo ?: "body1") }
            .zip(fontSizeResult) { (common, text, typo), fontSize -> Quad(common, text, typo, fontSize) }
            .zip(maxLinesResult) { (common, text, typo, fontSize), maxLines ->
                MappedNode(
                    TextNode(
                        id = common.id,
                        style = common.style,
                        text = text,
                        typography = typo,
                        fontSize = fontSize,
                        maxLines = maxLines
                    )
                )
            }
    }

    companion object {
        val SUPPORTED_TYPES: Set<String> = setOf("flexText", "componentTextView", "text")
    }
}

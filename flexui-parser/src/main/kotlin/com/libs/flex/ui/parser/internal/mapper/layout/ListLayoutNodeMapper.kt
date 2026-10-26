package com.libs.flex.ui.parser.internal.mapper.layout

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.functional.zip
import com.libs.flex.ui.core.domain.model.node.ListLayoutNode
import com.libs.flex.ui.core.domain.model.node.UIComponentNode
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.internal.mapper.NodeMapper
import com.libs.flex.ui.parser.internal.model.MappedNode
import com.libs.flex.ui.parser.internal.reader.PropertyReader
import com.libs.flex.ui.parser.internal.scope.MappingScope

private const val PRIMARY_TYPE = "flexList"
private val ALIASES = setOf("contentList", "list", "componentListView")

private const val KEY_ITEM_TEMPLATE = "itemTemplate"
private const val ALIAS_TEMPLATE = "template"
private const val KEY_ITEM_TEMPLATE_ID = "itemTemplateId"
private const val ALIAS_TEMPLATE_ID = "templateId"
private const val KEY_ITEMS_COUNT = "itemsCount"
private const val KEY_ITEMS = "items"

/**
 * Mapeador para listas perezosas con plantilla reutilizable ([ListLayoutNode]).
 *
 * Extrae la plantilla embebida ([KEY_ITEM_TEMPLATE]) y la agrega al listado explícito
 * de plantillas de [MappedNode.templates].
 */
internal class ListLayoutNodeMapper : NodeMapper {

    override fun map(
        reader: PropertyReader,
        scope: MappingScope
    ): Either<Nel<ParserError>, MappedNode> {
        val commonResult = reader.readCommonProperties()
        val templateInfoResult = resolveItemTemplate(reader, scope)
        val childrenResult = reader.readChildren(scope)

        return commonResult
            .zip(templateInfoResult) { common, templateInfo -> Pair(common, templateInfo) }
            .zip(childrenResult) { (common, templateInfo), children ->
                val itemsCount = reader.optionalInt(KEY_ITEMS_COUNT).getOrNull()
                    ?: reader.optionalListSize(KEY_ITEMS)
                    ?: 0

                val (templateId, extractedTemplates) = templateInfo

                MappedNode(
                    node = ListLayoutNode(
                        id = common.id,
                        style = common.style,
                        itemTemplateId = templateId,
                        itemsCount = itemsCount,
                        children = children.children
                    ),
                    templates = extractedTemplates + children.templates
                )
            }
    }

    private fun resolveItemTemplate(
        reader: PropertyReader,
        scope: MappingScope
    ): Either<Nel<ParserError>, Pair<ComponentId, List<UIComponentNode>>> {
        return when (val templateResult = reader.readChild(scope, KEY_ITEM_TEMPLATE, ALIAS_TEMPLATE)) {
            is Either.Left -> Either.Left(templateResult.value)
            is Either.Right -> {
                val mappedTemplate = templateResult.value
                if (mappedTemplate != null) {
                    val allTemplates = mappedTemplate.templates + mappedTemplate.node
                    Either.Right(Pair(mappedTemplate.node.id, allTemplates))
                } else {
                    resolveTemplateIdFromReference(reader)
                }
            }
        }
    }

    private fun resolveTemplateIdFromReference(
        reader: PropertyReader
    ): Either<Nel<ParserError>, Pair<ComponentId, List<UIComponentNode>>> {
        val refResult = reader.optionalString(KEY_ITEM_TEMPLATE_ID, ALIAS_TEMPLATE_ID)
        val templateIdStr = refResult.getOrNull()

        return if (templateIdStr != null) {
            ComponentId.of(templateIdStr).fold(
                onLeft = { domainError ->
                    Either.Left(
                        Nel.of(
                            ParserError.InvalidPropertyValue(
                                path = reader.path,
                                property = KEY_ITEM_TEMPLATE_ID,
                                reason = "Invalid identifier: '${domainError.raw}'",
                                domainError = domainError
                            )
                        )
                    )
                },
                onRight = { Either.Right(Pair(it, emptyList())) }
            )
        } else {
            Either.Left(
                Nel.of(
                    ParserError.MissingRequiredProperty(
                        path = reader.path,
                        property = KEY_ITEM_TEMPLATE
                    )
                )
            )
        }
    }

    companion object {
        val SUPPORTED_TYPES: Set<String> = setOf(PRIMARY_TYPE) + ALIASES
    }
}

package com.libs.flex.ui.parser.internal.template

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.model.node.UIComponentNode
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.ParserMode
import com.libs.flex.ui.parser.WarningCode
import com.libs.flex.ui.parser.internal.dispatcher.NodeDispatcher
import com.libs.flex.ui.parser.internal.reader.ErrorPolicy
import com.libs.flex.ui.parser.internal.scope.MappingScope
import com.libs.flex.ui.parser.internal.util.kindName
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * Extractor y compilador de plantillas reutilizables (templates) declaradas a nivel de documento
 * o acumuladas de forma embebida en nodos hijos (ej. ListLayoutNode).
 */
internal class TemplateExtractor {

    private val keyTemplates = "templates"

    fun extractDocumentTemplates(
        rootElement: JsonObject,
        baseScope: MappingScope,
        dispatcher: NodeDispatcher,
        errorPolicy: ErrorPolicy
    ): Either<Nel<ParserError>, Map<ComponentId, UIComponentNode>> {
        val templatesElement = rootElement[keyTemplates] ?: return Either.Right(emptyMap())

        val templatesMap = mutableMapOf<ComponentId, UIComponentNode>()
        val templatePaths = mutableMapOf<ComponentId, String>()
        val errors = mutableListOf<ParserError>()

        fun mapSingleTemplate(element: JsonElement, path: String) {
            if (element !is JsonObject) {
                if (baseScope.options.mode == ParserMode.STRICT) {
                    errors.add(
                        ParserError.InvalidPropertyValue(
                            path = path,
                            property = path.substringAfterLast('.'),
                            reason = "Expected JSON object for template, but found ${element.kindName()}"
                        )
                    )
                } else {
                    baseScope.warn(
                        code = WarningCode.INVALID_TEMPLATE,
                        message = "Template at '$path' must be a JSON object, but found ${element.kindName()}. Discarded."
                    )
                }
                return
            }

            val templateScope = baseScope.childScope(path.removePrefix("$.templates.").removePrefix("$.templates"))
            when (val mapped = dispatcher.dispatch(element, templateScope, errorPolicy)) {
                is Either.Left -> {
                    if (baseScope.options.mode == ParserMode.STRICT) {
                        errors.addAll(mapped.value.toList())
                    } else {
                        val reason = mapped.value.toList().joinToString { it.message }
                        baseScope.warn(
                            code = WarningCode.INVALID_TEMPLATE,
                            message = "Template at '$path' failed to parse and was discarded: $reason"
                        )
                    }
                }
                is Either.Right -> {
                    val allTemplates = listOf(mapped.value.node) + mapped.value.templates
                    for (tmpl in allTemplates) {
                        if (templatesMap.containsKey(tmpl.id)) {
                            errors.add(
                                ParserError.DuplicateComponentId(
                                    path = path,
                                    id = tmpl.id.value
                                )
                            )
                        } else {
                            templatesMap[tmpl.id] = tmpl
                            templatePaths[tmpl.id] = path
                            dispatcher.recordPath(tmpl.id, path)
                        }
                    }
                }
            }
        }

        when (templatesElement) {
            is JsonObject -> {
                for ((key, element) in templatesElement) {
                    mapSingleTemplate(element, "$.$keyTemplates.$key")
                }
            }
            is JsonArray -> {
                templatesElement.forEachIndexed { index, element ->
                    mapSingleTemplate(element, "$.$keyTemplates[$index]")
                }
            }
            else -> {
                if (baseScope.options.mode == ParserMode.STRICT) {
                    errors.add(
                        ParserError.InvalidPropertyValue(
                            path = "$.$keyTemplates",
                            property = keyTemplates,
                            reason = "Expected JSON object or array for '$keyTemplates', but found ${templatesElement.kindName()}"
                        )
                    )
                } else {
                    baseScope.warn(
                        code = WarningCode.INVALID_TEMPLATE,
                        message = "Expected JSON object or array for '$keyTemplates', but found ${templatesElement.kindName()}. Discarded."
                    )
                }
            }
        }

        return Nel.fromListOrNull(errors)?.let { Either.Left(it) } ?: Either.Right(templatesMap)
    }

    fun mergeInlineTemplates(
        documentTemplates: Map<ComponentId, UIComponentNode>,
        inlineTemplates: List<UIComponentNode>,
        dispatcher: NodeDispatcher
    ): Either<Nel<ParserError>, Map<ComponentId, UIComponentNode>> {
        val merged = documentTemplates.toMutableMap()
        val errors = mutableListOf<ParserError>()

        for (inline in inlineTemplates) {
            if (merged.containsKey(inline.id)) {
                val path = dispatcher.getPathForId(inline.id) ?: "inline_template"
                errors.add(
                    ParserError.DuplicateComponentId(
                        path = path,
                        id = inline.id.value
                    )
                )
            } else {
                merged[inline.id] = inline
            }
        }

        return Nel.fromListOrNull(errors)?.let { Either.Left(it) } ?: Either.Right(merged)
    }
}

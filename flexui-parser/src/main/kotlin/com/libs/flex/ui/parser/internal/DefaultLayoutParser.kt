package com.libs.flex.ui.parser.internal

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.model.LayoutDocument
import com.libs.flex.ui.parser.LayoutParser
import com.libs.flex.ui.parser.ParseResult
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.ParserOptions
import com.libs.flex.ui.parser.ParserWarning
import com.libs.flex.ui.parser.internal.dispatcher.NodeDispatcher
import com.libs.flex.ui.parser.internal.envelope.DocumentEnvelope
import com.libs.flex.ui.parser.internal.guard.PayloadGuard
import com.libs.flex.ui.parser.internal.mapper.NodeMapperRegistry
import com.libs.flex.ui.parser.internal.reader.DefaultErrorPolicyFactory
import com.libs.flex.ui.parser.internal.reader.ErrorPolicy
import com.libs.flex.ui.parser.internal.reader.ErrorPolicyFactory
import com.libs.flex.ui.parser.internal.scope.DefaultMappingScope
import com.libs.flex.ui.parser.internal.template.TemplateExtractor
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException

/**
 * Implementación predeterminada interna de la fachada [LayoutParser].
 *
 * Coordina el pipeline end-to-end respetando Single Responsibility Principle (SRP)
 * y Dependency Inversion Principle (DIP):
 * 1. [PayloadGuard]: Medición segura de bytes UTF-8, pre-escaneo de anidación y parsing a JSON.
 * 2. [DocumentEnvelope]: Detección semántica de envoltura vs nodo directo y validación de versión.
 * 3. [TemplateExtractor]: Mapeo de plantillas de documento previo a la raíz para soportar referencias.
 * 4. [NodeDispatcher]: Resolución desacoplada de componentes, lectores de propiedades y fallbacks.
 * 5. Ensamblado inmutable con [LayoutDocument.create] y traducción de rutas para errores del Core.
 */
internal class DefaultLayoutParser(
    private val registry: NodeMapperRegistry = NodeMapperRegistry.createDefault(),
    private val json: Json = Json,
    private val errorPolicyFactory: ErrorPolicyFactory = DefaultErrorPolicyFactory()
) : LayoutParser {

    private val payloadGuard = PayloadGuard(json)
    private val documentEnvelope = DocumentEnvelope()
    private val templateExtractor = TemplateExtractor()

    override fun parse(
        jsonString: String,
        options: ParserOptions
    ): Either<Nel<ParserError>, ParseResult> {
        return try {
            executePipeline(jsonString, options)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Either.Left(
                Nel.of(
                    ParserError.InternalError(
                        path = "$",
                        cause = e
                    )
                )
            )
        }
    }

    private fun executePipeline(
        jsonString: String,
        options: ParserOptions
    ): Either<Nel<ParserError>, ParseResult> {
        // Pasos 1 y 2: Guardián de frontera (tamaño, pre-escaneo de profundidad y parseo)
        val rootElementResult = payloadGuard.validateAndParse(jsonString, options)
        if (rootElementResult is Either.Left) return rootElementResult
        val rootElement = (rootElementResult as Either.Right).value

        // Paso 3: Análisis de envoltura del documento
        val envelopeResult = documentEnvelope.extract(rootElement, options)
        if (envelopeResult is Either.Left) return envelopeResult
        val envelope = (envelopeResult as Either.Right).value

        val warnings = mutableListOf<ParserWarning>()
        warnings.addAll(envelope.warnings)

        val dispatcher = NodeDispatcher(registry, errorPolicyFactory = errorPolicyFactory)
        val errorPolicy = errorPolicyFactory.create(options.mode)

        val baseScope = DefaultMappingScope(
            path = "$",
            depth = 0,
            options = options,
            nodeResolver = { jsonNode, scope -> dispatcher.dispatch(jsonNode, scope, errorPolicy) },
            onWarn = { warnings.add(it) }
        )

        // Paso 4: Mapear plantillas a nivel de documento antes de la raíz (para resolver referencias por ID)
        val docTemplatesResult = templateExtractor.extractDocumentTemplates(
            rootElement = rootElement,
            baseScope = baseScope,
            dispatcher = dispatcher,
            errorPolicy = errorPolicy
        )
        if (docTemplatesResult is Either.Left) return docTemplatesResult
        val documentTemplates = (docTemplatesResult as Either.Right).value

        // Paso 5: Mapear nodo raíz
        val rootScope = DefaultMappingScope(
            path = envelope.rootPath,
            depth = 1,
            options = options,
            nodeResolver = { jsonNode, scope -> dispatcher.dispatch(jsonNode, scope, errorPolicy) },
            onWarn = { warnings.add(it) }
        )
        val rootMappedResult = dispatcher.dispatch(envelope.rootJson, rootScope, errorPolicy)
        if (rootMappedResult is Either.Left) return rootMappedResult
        val rootMappedNode = (rootMappedResult as Either.Right).value

        // Paso 6: Unificar plantillas inline con detección estricta de colisión de IDs
        val allTemplatesResult = templateExtractor.mergeInlineTemplates(
            documentTemplates = documentTemplates,
            inlineTemplates = rootMappedNode.templates,
            dispatcher = dispatcher
        )
        if (allTemplatesResult is Either.Left) return allTemplatesResult
        val allTemplates = (allTemplatesResult as Either.Right).value

        // Paso 7: Crear el LayoutDocument inmutable en el Core utilizando options.limits
        val documentResult = LayoutDocument.create(
            schemaVersion = envelope.schemaVersion,
            root = rootMappedNode.node,
            templates = allTemplates,
            limits = options.limits
        )

        return documentResult.fold(
            onLeft = { structuralErrors ->
                Either.Left(
                    structuralErrors.map { structuralError ->
                        val path = when (structuralError) {
                            is DomainError.Structural.DuplicateComponentId ->
                                dispatcher.getPathForId(structuralError.id) ?: envelope.rootPath
                            is DomainError.Structural.MaxNestingDepthExceeded ->
                                dispatcher.getPathForId(structuralError.id) ?: envelope.rootPath
                            is DomainError.Structural.InvalidComponentReference ->
                                dispatcher.getPathForId(structuralError.id) ?: envelope.rootPath
                            is DomainError.Structural.DuplicateTemplateUsage ->
                                dispatcher.getPathForId(structuralError.listId) ?: envelope.rootPath
                            else -> envelope.rootPath
                        }
                        ParserError.CoreStructuralError(
                            path = path,
                            structuralError = structuralError
                        )
                    }
                )
            },
            onRight = { document ->
                Either.Right(
                    ParseResult(
                        document = document,
                        warnings = warnings.toList()
                    )
                )
            }
        )
    }
}

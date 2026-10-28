package com.libs.flex.ui.parser.internal.dispatcher

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.model.node.UnsupportedFallbackNode
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.ParserMode
import com.libs.flex.ui.parser.WarningCode
import com.libs.flex.ui.parser.internal.mapper.NodeMapperRegistry
import com.libs.flex.ui.parser.internal.model.MappedNode
import com.libs.flex.ui.parser.internal.reader.DefaultErrorPolicyFactory
import com.libs.flex.ui.parser.internal.reader.ErrorPolicy
import com.libs.flex.ui.parser.internal.reader.ErrorPolicyFactory
import com.libs.flex.ui.parser.internal.reader.JsonPropertyReader
import com.libs.flex.ui.parser.internal.scope.MappingScope
import com.libs.flex.ui.parser.internal.util.IdSanitizer
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Despachador de componentes responsable de resolver el tipo de componente,
 * seleccionar el mapeador adecuado desde el registro, instanciar el lector de propiedades
 * y generar fallbacks en modo tolerante cuando el componente es desconocido o carece de tipo.
 */
internal class NodeDispatcher(
    private val registry: NodeMapperRegistry,
    private val idToPath: MutableMap<ComponentId, String> = mutableMapOf(),
    private val errorPolicyFactory: ErrorPolicyFactory = DefaultErrorPolicyFactory()
) {

    private val keyType = "type"
    private val keyId = "id"

    fun getPathForId(id: ComponentId): String? = idToPath[id]

    fun recordPath(id: ComponentId, path: String) {
        idToPath[id] = path
    }

    fun dispatch(
        json: JsonObject,
        scope: MappingScope,
        errorPolicy: ErrorPolicy = errorPolicyFactory.create(scope.options.mode)
    ): Either<Nel<ParserError>, MappedNode> {
        val type = (json[keyType] as? JsonPrimitive)?.content
        if (type == null) {
            return if (scope.options.mode == ParserMode.TOLERANT) {
                createFallbackNode(
                    json = json,
                    scope = scope,
                    rawType = "unknown",
                    reason = "Missing 'type' property",
                    code = WarningCode.MISSING_TYPE
                )
            } else {
                Either.Left(
                    Nel.of(
                        ParserError.MissingRequiredProperty(
                            path = scope.path,
                            property = keyType
                        )
                    )
                )
            }
        }

        val mapper = registry[type]
        if (mapper == null) {
            return if (scope.options.mode == ParserMode.TOLERANT) {
                createFallbackNode(
                    json = json,
                    scope = scope,
                    rawType = type,
                    reason = "Unknown component type: '$type'",
                    code = WarningCode.UNKNOWN_COMPONENT
                )
            } else {
                Either.Left(
                    Nel.of(
                        ParserError.UnknownComponentType(
                            path = scope.path,
                            type = type
                        )
                    )
                )
            }
        }

        val reader = JsonPropertyReader(
            json = json,
            path = scope.path,
            policy = errorPolicy,
            onWarn = { scope.warn(it.code, it.message) }
        )

        val result = mapper.map(reader, scope)
        if (result is Either.Right) {
            idToPath[result.value.node.id] = scope.path
            result.value.templates.forEach { template ->
                idToPath[template.id] = "${scope.path}.template"
            }
        }
        return result
    }

    private fun createFallbackNode(
        json: JsonObject,
        scope: MappingScope,
        rawType: String,
        reason: String,
        code: String
    ): Either<Nel<ParserError>, MappedNode> {
        val id = IdSanitizer.sanitizeToComponentId(
            (json[keyId] as? JsonPrimitive)?.content,
            fallbackPrefix = "auto_${scope.path.ifEmpty { "node" }}"
        )
        scope.warn(
            code = code,
            message = "Component at '${scope.path}' was replaced with fallback: $reason."
        )
        val fallback = UnsupportedFallbackNode(
            id = id,
            style = null,
            rawType = rawType,
            reason = reason
        )
        idToPath[id] = scope.path
        return Either.Right(MappedNode(node = fallback))
    }
}

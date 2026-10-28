package com.libs.flex.ui.parser.internal.mapper

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.model.node.UnsupportedFallbackNode
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.internal.model.MappedNode
import com.libs.flex.ui.parser.internal.reader.PropertyReader
import com.libs.flex.ui.parser.internal.scope.MappingScope
import com.libs.flex.ui.parser.internal.util.IdSanitizer

import com.libs.flex.ui.parser.ParserMode

private const val KEY_TYPE = "type"
private const val KEY_ID = "id"
private const val DEFAULT_TYPE = "unknown"
private const val DEFAULT_PATH_FALLBACK = "node"
private const val WARN_CODE_NODE_FALLBACK = "NODE_FALLBACK"

/**
 * Decorador que intercepta cualquier fallo devuelto por un [NodeMapper] delegado
 * y lo transforma en un [UnsupportedFallbackNode] inofensivo, registrando un warning en el [scope].
 *
 * Evalúa dinámicamente [com.libs.flex.ui.parser.ParserOptions.mode] en tiempo de ejecución:
 * - En modo estricto ([ParserMode.STRICT]), delega directamente sin interceptar fallos.
 * - En modo tolerante ([ParserMode.TOLERANT]), captura fallos y excepciones para evitar caídas del árbol.
 */
internal class TolerantNodeMapperDecorator(
    val delegate: NodeMapper
) : NodeMapper {

    override val types: Set<String> get() = delegate.types

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other is TolerantNodeMapperDecorator) return delegate == other.delegate
        return delegate == other
    }

    override fun hashCode(): Int = delegate.hashCode()

    override fun map(
        reader: PropertyReader,
        scope: MappingScope
    ): Either<Nel<ParserError>, MappedNode> {
        // En modo estricto, no intercepta fallos; delega directamente al mapper original
        if (scope.options.mode != ParserMode.TOLERANT) {
            return delegate.map(reader, scope)
        }

        return try {
            when (val result = delegate.map(reader, scope)) {
                is Either.Right -> result
                is Either.Left -> {
                    val reasons = result.value.toList().joinToString("; ") { it.message }
                    createFallback(reader, scope, reasons)
                }
            }
        } catch (e: Exception) {
            val reason = e.message ?: "Unexpected exception during mapping: ${e::class.simpleName}"
            createFallback(reader, scope, reason)
        }
    }

    private fun createFallback(
        reader: PropertyReader,
        scope: MappingScope,
        reason: String
    ): Either<Nel<ParserError>, MappedNode> {
        val rawType = reader.optionalString(KEY_TYPE).getOrNull() ?: DEFAULT_TYPE
        val common = reader.readCommonProperties().getOrNull()
        val safePrefix = "auto_${scope.path.ifEmpty { DEFAULT_PATH_FALLBACK }}"
        val safeId = common?.id ?: IdSanitizer.sanitizeToComponentId(
            reader.optionalString(KEY_ID).getOrNull(),
            fallbackPrefix = safePrefix
        )

        scope.warn(
            code = WARN_CODE_NODE_FALLBACK,
            message = "Node at '${scope.path}' of type '$rawType' failed mapping: $reason. Fallback created."
        )

        val fallbackNode = UnsupportedFallbackNode(
            id = safeId,
            style = common?.style,
            rawType = rawType,
            reason = reason
        )

        return Either.Right(MappedNode(node = fallbackNode))
    }
}

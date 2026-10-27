package com.libs.flex.ui.parser.internal.scope

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.ParserOptions
import com.libs.flex.ui.parser.ParserWarning
import com.libs.flex.ui.parser.internal.model.MappedNode
import com.libs.flex.ui.parser.internal.util.kindName
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * Implementación predeterminada e inmutable de [MappingScope].
 */
internal class DefaultMappingScope(
    override val path: String,
    override val depth: Int,
    override val options: ParserOptions,
    private val nodeResolver: (JsonObject, MappingScope) -> Either<Nel<ParserError>, MappedNode>,
    private val onWarn: (ParserWarning) -> Unit
) : MappingScope {

    override fun mapChild(json: JsonElement, segment: String): Either<Nel<ParserError>, MappedNode> {
        val childPath = resolveChildPath(segment)
        val nextDepth = depth + 1

        if (nextDepth > options.maxDepth) {
            return Either.Left(
                Nel.of(
                    ParserError.MaxDepthExceeded(
                        path = childPath,
                        currentDepth = nextDepth,
                        maxDepth = options.maxDepth
                    )
                )
            )
        }

        if (json !is JsonObject) {
            return Either.Left(
                Nel.of(
                    ParserError.InvalidPropertyValue(
                        path = childPath,
                        property = segment,
                        reason = "Expected JSON object for component, but found ${json.kindName()}"
                    )
                )
            )
        }

        val childScope = DefaultMappingScope(
            path = childPath,
            depth = nextDepth,
            options = options,
            nodeResolver = nodeResolver,
            onWarn = onWarn
        )

        return nodeResolver(json, childScope)
    }

    override fun warn(code: String, message: String) {
        onWarn(
            ParserWarning(
                path = path,
                code = code,
                message = message
            )
        )
    }

    override fun childScope(segment: String): MappingScope {
        return DefaultMappingScope(
            path = resolveChildPath(segment),
            depth = depth + 1,
            options = options,
            nodeResolver = nodeResolver,
            onWarn = onWarn
        )
    }

    private fun resolveChildPath(segment: String): String {
        return when {
            path.isEmpty() -> segment
            segment.startsWith("[") -> "$path$segment"
            else -> "$path.$segment"
        }
    }
}


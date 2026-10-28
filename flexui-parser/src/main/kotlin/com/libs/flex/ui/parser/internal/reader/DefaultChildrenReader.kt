package com.libs.flex.ui.parser.internal.reader

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.model.node.UIComponentNode
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.ParserWarning
import com.libs.flex.ui.parser.internal.model.ChildrenResult
import com.libs.flex.ui.parser.internal.model.MappedNode
import com.libs.flex.ui.parser.internal.scope.MappingScope
import com.libs.flex.ui.parser.internal.util.kindName
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

/**
 * Implementación predeterminada e inmutable de [ChildrenReader].
 */
internal class DefaultChildrenReader : ChildrenReader {

    override fun readChildren(
        entry: Pair<String, JsonElement>?,
        path: String,
        scope: MappingScope,
        policy: ErrorPolicy,
        onWarn: (ParserWarning) -> Unit
    ): Either<Nel<ParserError>, ChildrenResult> {
        if (entry == null) return Either.Right(ChildrenResult())
        val element = entry.second
        if (element is JsonNull) return Either.Right(ChildrenResult())

        val array = element as? JsonArray
        if (array == null) {
            val error = ParserError.InvalidPropertyValue(
                path = path,
                property = entry.first,
                reason = "Expected JSON array for children, but found ${element.kindName()}"
            )
            return policy.handleNonNullOptionalError(path, entry.first, error, ChildrenResult(), onWarn)
        }

        val children = mutableListOf<UIComponentNode>()
        val templates = mutableListOf<UIComponentNode>()
        val errors = mutableListOf<ParserError>()

        array.forEachIndexed { index, childElement ->
            val segment = "${entry.first}[$index]"
            when (val mapped = scope.mapChild(childElement, segment)) {
                is Either.Left -> errors.addAll(mapped.value.toList())
                is Either.Right -> {
                    children.add(mapped.value.node)
                    templates.addAll(mapped.value.templates)
                }
            }
        }

        return if (errors.isNotEmpty()) {
            Either.Left(Nel.fromListOrNull(errors)!!)
        } else {
            Either.Right(ChildrenResult(children = children, templates = templates))
        }
    }

    override fun readChild(
        entry: Pair<String, JsonElement>?,
        scope: MappingScope
    ): Either<Nel<ParserError>, MappedNode?> {
        if (entry == null) return Either.Right(null)
        val element = entry.second
        if (element is JsonNull) return Either.Right(null)

        return scope.mapChild(element, entry.first).map { it }
    }
}

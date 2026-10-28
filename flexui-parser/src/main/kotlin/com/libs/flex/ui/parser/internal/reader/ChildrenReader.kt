package com.libs.flex.ui.parser.internal.reader

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.ParserWarning
import com.libs.flex.ui.parser.internal.model.ChildrenResult
import com.libs.flex.ui.parser.internal.model.MappedNode
import com.libs.flex.ui.parser.internal.scope.MappingScope
import kotlinx.serialization.json.JsonElement

/**
 * Contrato abstracto para la lectura e iteración recursiva de jerarquías de componentes hijos.
 */
internal interface ChildrenReader {

    fun readChildren(
        entry: Pair<String, JsonElement>?,
        path: String,
        scope: MappingScope,
        policy: ErrorPolicy,
        onWarn: (ParserWarning) -> Unit
    ): Either<Nel<ParserError>, ChildrenResult>

    fun readChild(
        entry: Pair<String, JsonElement>?,
        scope: MappingScope
    ): Either<Nel<ParserError>, MappedNode?>
}

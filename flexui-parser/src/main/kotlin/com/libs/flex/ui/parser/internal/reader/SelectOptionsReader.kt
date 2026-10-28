package com.libs.flex.ui.parser.internal.reader

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.model.node.SelectOption
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.ParserWarning
import kotlinx.serialization.json.JsonElement

/**
 * Contrato abstracto para la deserialización y validación de opciones de selección ([SelectOption]).
 */
internal interface SelectOptionsReader {

    fun readSelectOptions(
        entry: Pair<String, JsonElement>?,
        path: String,
        policy: ErrorPolicy,
        onWarn: (ParserWarning) -> Unit
    ): Either<Nel<ParserError>, Nel<SelectOption>>
}

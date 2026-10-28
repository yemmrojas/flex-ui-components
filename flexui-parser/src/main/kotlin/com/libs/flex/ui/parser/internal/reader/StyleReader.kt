package com.libs.flex.ui.parser.internal.reader

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.value.StyleProperties
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.ParserWarning
import com.libs.flex.ui.parser.internal.model.CommonProperties
import kotlinx.serialization.json.JsonObject

/**
 * Contrato abstracto para la lectura y resolución de estilos ([StyleProperties]) y propiedades comunes.
 */
internal interface StyleReader {

    fun readCommonProperties(
        json: JsonObject,
        path: String,
        policy: ErrorPolicy,
        onWarn: (ParserWarning) -> Unit
    ): Either<Nel<ParserError>, CommonProperties>

    fun readStyleProperties(styleReader: PropertyReader): Either<Nel<ParserError>, StyleProperties>
}

package com.libs.flex.ui.parser.internal

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.parser.LayoutParser
import com.libs.flex.ui.parser.ParseResult
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.ParserOptions

/**
 * Implementación predeterminada interna de la fachada [LayoutParser].
 */
internal class DefaultLayoutParser : LayoutParser {

    override fun parse(
        jsonString: String,
        options: ParserOptions
    ): Either<Nel<ParserError>, ParseResult> {
        // Stub inicial - El pipeline completo de 7 pasos se ensambla en el Bloque 4
        return Either.Left(
            Nel.of(
                ParserError.InvalidJson(
                    path = "root",
                    reason = "DefaultLayoutParser pipeline under construction"
                )
            )
        )
    }
}

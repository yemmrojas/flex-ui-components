package com.libs.flex.ui.parser.internal.reader

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.ParserWarning

/**
 * En modo estricto, cualquier propiedad opcional inválida interrumpe el parsing retornando un error.
 */
internal class StrictErrorPolicy : ErrorPolicy {

    override fun <T> handleOptionalError(
        path: String,
        property: String,
        error: ParserError,
        default: T?,
        onWarn: (ParserWarning) -> Unit
    ): Either<Nel<ParserError>, T?> {
        return Either.Left(Nel.of(error))
    }

    override fun <T : Any> handleNonNullOptionalError(
        path: String,
        property: String,
        error: ParserError,
        default: T,
        onWarn: (ParserWarning) -> Unit
    ): Either<Nel<ParserError>, T> {
        return Either.Left(Nel.of(error))
    }
}

package com.libs.flex.ui.parser.internal.reader

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.ParserWarning
import com.libs.flex.ui.parser.WarningCode

/**
 * En modo tolerante, las propiedades opcionales inválidas se descartan aplicando su valor predeterminado
 * y emitiendo un [ParserWarning] para observabilidad.
 */
internal class TolerantErrorPolicy : ErrorPolicy {

    override fun <T> handleOptionalError(
        path: String,
        property: String,
        error: ParserError,
        default: T?,
        onWarn: (ParserWarning) -> Unit
    ): Either<Nel<ParserError>, T?> {
        onWarn(
            ParserWarning(
                path = path,
                code = WarningCode.INVALID_OPTIONAL_PROPERTY,
                message = "Optional property '$property' at '$path' was invalid: ${error.message}. Defaulting to $default"
            )
        )
        return Either.Right(default)
    }

    override fun <T : Any> handleNonNullOptionalError(
        path: String,
        property: String,
        error: ParserError,
        default: T,
        onWarn: (ParserWarning) -> Unit
    ): Either<Nel<ParserError>, T> {
        onWarn(
            ParserWarning(
                path = path,
                code = WarningCode.INVALID_OPTIONAL_PROPERTY,
                message = "Optional property '$property' at '$path' was invalid: ${error.message}. Defaulting to $default"
            )
        )
        return Either.Right(default)
    }
}

package com.libs.flex.ui.parser.internal.reader

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.ParserMode
import com.libs.flex.ui.parser.ParserWarning

/**
 * Estrategia de resolución de fallos sobre propiedades opcionales.
 *
 * Permite desacoplar a los mapeadores individuales del modo de operación ([ParserMode.STRICT] vs [ParserMode.TOLERANT]).
 */
internal interface ErrorPolicy {

    /**
     * Resuelve un error detectado al deserializar o validar una propiedad opcional.
     *
     * @param path Ruta JSON donde ocurrió el error.
     * @param property Nombre de la propiedad analizada.
     * @param error Error tipado detectado.
     * @param default Valor predeterminado a utilizar en caso de tolerancia.
     * @param onWarn Función receptora para registrar advertencias no fatales en modo tolerante.
     * @return [Either.Right] con el valor [default] (en modo tolerante), o [Either.Left] con el [ParserError] (en modo estricto).
     */
    fun <T> handleOptionalError(
        path: String,
        property: String,
        error: ParserError,
        default: T?,
        onWarn: (ParserWarning) -> Unit
    ): Either<Nel<ParserError>, T?>

    companion object {
        fun of(mode: ParserMode): ErrorPolicy = when (mode) {
            ParserMode.STRICT -> StrictErrorPolicy
            ParserMode.TOLERANT -> TolerantErrorPolicy
        }
    }
}

/**
 * En modo estricto, cualquier propiedad opcional inválida interrumpe el parsing retornando un error.
 */
internal object StrictErrorPolicy : ErrorPolicy {
    override fun <T> handleOptionalError(
        path: String,
        property: String,
        error: ParserError,
        default: T?,
        onWarn: (ParserWarning) -> Unit
    ): Either<Nel<ParserError>, T?> {
        return Either.Left(Nel.of(error))
    }
}

/**
 * En modo tolerante, las propiedades opcionales inválidas se descartan aplicando su valor predeterminado
 * y emitiendo un [ParserWarning] para observabilidad.
 */
internal object TolerantErrorPolicy : ErrorPolicy {
    private const val CODE_INVALID_OPTIONAL_PROPERTY = "INVALID_OPTIONAL_PROPERTY"

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
                code = CODE_INVALID_OPTIONAL_PROPERTY,
                message = "Optional property '$property' at '$path' was invalid: ${error.message}. Defaulting to $default"
            )
        )
        return Either.Right(default)
    }
}

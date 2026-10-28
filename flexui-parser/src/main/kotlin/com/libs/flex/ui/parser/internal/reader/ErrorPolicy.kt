package com.libs.flex.ui.parser.internal.reader

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.ParserWarning

/**
 * Estrategia de resolución de fallos sobre propiedades opcionales.
 *
 * Permite desacoplar a los mapeadores individuales del modo de operación ([com.libs.flex.ui.parser.ParserMode.STRICT] vs [com.libs.flex.ui.parser.ParserMode.TOLERANT]).
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

    /**
     * Resuelve un error detectado al deserializar una propiedad opcional con valor por defecto no nulo.
     */
    fun <T : Any> handleNonNullOptionalError(
        path: String,
        property: String,
        error: ParserError,
        default: T,
        onWarn: (ParserWarning) -> Unit
    ): Either<Nel<ParserError>, T>
}

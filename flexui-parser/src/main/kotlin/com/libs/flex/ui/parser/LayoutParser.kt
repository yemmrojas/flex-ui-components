package com.libs.flex.ui.parser

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.parser.internal.DefaultLayoutParser

/**
 * Fachada principal del módulo `:flexui-parser` (Dependency Inversion Principle).
 *
 * Deserializa, valida y mapea payloads JSON crudos hacia árboles inmutables
 * de [com.libs.flex.ui.core.domain.model.LayoutDocument], aislando el dominio de FlexUI.
 */
public interface LayoutParser {

    /**
     * Parsea un string JSON en un [ParseResult] conteniendo el [com.libs.flex.ui.core.domain.model.LayoutDocument]
     * y las advertencias no fatales, o retorna una lista no vacía de [ParserError] en caso de fallo.
     *
     * @param jsonString Cadena JSON a deserializar.
     * @param options Configuración de parsing (modo estricto/tolerante, límites de bytes y profundidad).
     * @return [Either.Right] con [ParseResult] si la operación fue exitosa, o [Either.Left] con [Nel] de [ParserError].
     */
    public fun parse(
        jsonString: String,
        options: ParserOptions = ParserOptions.DEFAULT
    ): Either<Nel<ParserError>, ParseResult>

    public companion object {
        /**
         * Crea una nueva instancia de la implementación predeterminada de [LayoutParser].
         */
        public fun create(): LayoutParser = DefaultLayoutParser()
    }
}

package com.libs.flex.ui.parser.internal.scope

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.ParserOptions
import com.libs.flex.ui.parser.internal.model.MappedNode
import kotlinx.serialization.json.JsonElement

/**
 * Contexto de alcance acotado e inmutable para el mapeo recursivo de componentes.
 *
 * Centraliza:
 * 1. La construcción determinista de la ruta [path].
 * 2. El control y validación de la profundidad [depth] contra [ParserOptions.maxDepth].
 * 3. La emisión controlada de advertencias no fatales mediante [warn].
 * 4. La delegación segura de mapeo para nodos hijos mediante [mapChild].
 */
internal interface MappingScope {
    val path: String
    val depth: Int
    val options: ParserOptions

    /**
     * Mapea un elemento JSON hijo incrementando la profundidad y validando los límites.
     *
     * @param json Elemento JSON correspondiente al nodo hijo.
     * @param segment Segmento de ruta que identifica al hijo (ej. "children[0]", "header").
     */
    fun mapChild(json: JsonElement, segment: String): Either<Nel<ParserError>, MappedNode>

    /**
     * Registra una advertencia no fatal asociada a la ruta actual del scope.
     */
    fun warn(code: String, message: String)

    /**
     * Crea un sub-scope hijo sin ejecutar el mapeo de inmediato.
     */
    fun childScope(segment: String): MappingScope
}

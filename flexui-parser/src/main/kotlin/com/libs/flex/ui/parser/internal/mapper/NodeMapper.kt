package com.libs.flex.ui.parser.internal.mapper

import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.parser.ParserError
import com.libs.flex.ui.parser.internal.model.MappedNode
import com.libs.flex.ui.parser.internal.reader.PropertyReader
import com.libs.flex.ui.parser.internal.scope.MappingScope

/**
 * Contrato para todos los mapeadores de nodos de FlexUI (LSP).
 *
 * Cada mapper declara explícitamente los identificadores de tipo y aliases que soporta
 * mediante [types] y depende exclusivamente de [PropertyReader] (DIP / ISP)
 * y de [MappingScope] para interactuar con el contexto de parsing.
 */
internal interface NodeMapper {

    /**
     * Conjunto de nombres oficiales y aliases soportados por este mapeador.
     */
    val types: Set<String>

    /**
     * Mapea un componente a su correspondiente [MappedNode] de dominio.
     *
     * @param reader Lector tipado y seguro de propiedades.
     * @param scope Alcance de mapeo para control de rutas, límites y recursión de hijos.
     * @return [Either.Right] con el [MappedNode] construido, o [Either.Left] con errores [ParserError].
     */
    fun map(reader: PropertyReader, scope: MappingScope): Either<Nel<ParserError>, MappedNode>
}

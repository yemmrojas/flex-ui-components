package com.libs.flex.ui.parser.internal.model

import com.libs.flex.ui.core.domain.model.node.UIComponentNode

/**
 * Contenedor inmutable que transporta el nodo mapeado y cualquier plantilla extraída
 * (como las plantillas de elementos de listas dinámicas) como valor explícito,
 * eliminando efectos secundarios y mutaciones de estado compartidas.
 *
 * @property node Nodo de interfaz de usuario de dominio validado.
 * @property templates Lista de componentes utilizados como plantillas para componentes hijos/listas.
 */
internal data class MappedNode(
    val node: UIComponentNode,
    val templates: List<UIComponentNode> = emptyList()
)

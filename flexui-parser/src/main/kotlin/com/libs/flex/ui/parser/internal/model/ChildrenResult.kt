package com.libs.flex.ui.parser.internal.model

import com.libs.flex.ui.core.domain.model.node.UIComponentNode

/**
 * Contenedor inmutable para el resultado del mapeo de los nodos hijos de un contenedor.
 *
 * Separa explícitamente los componentes hijos directos de las plantillas extraídas
 * recursivamente durante el proceso de mapeo.
 *
 * @property children Lista de nodos hijos directos del contenedor.
 * @property templates Lista agregada de plantillas descubiertas en los hijos (ej. en listas anidadas).
 */
internal data class ChildrenResult(
    val children: List<UIComponentNode> = emptyList(),
    val templates: List<UIComponentNode> = emptyList()
)

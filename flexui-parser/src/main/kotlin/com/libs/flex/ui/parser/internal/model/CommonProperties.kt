package com.libs.flex.ui.parser.internal.model

import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.StyleProperties

/**
 * Encapsula las propiedades comunes a todos los componentes de FlexUI (identificador y estilos).
 */
internal data class CommonProperties(
    val id: ComponentId,
    val style: StyleProperties
)

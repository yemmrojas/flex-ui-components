package com.libs.flex.ui.parser

import com.libs.flex.ui.core.domain.model.LayoutDocument

/**
 * Resultado exitoso de la operación de parsing.
 *
 * @property document El [LayoutDocument] inmutable y validado listo para la sesión de formulario o renderizado.
 * @property warnings Lista de advertencias no fatales recopiladas durante el proceso de parsing.
 */
public data class ParseResult(
    public val document: LayoutDocument,
    public val warnings: List<ParserWarning> = emptyList()
)

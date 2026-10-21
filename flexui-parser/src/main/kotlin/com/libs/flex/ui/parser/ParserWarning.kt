package com.libs.flex.ui.parser

/**
 * Representa una advertencia no fatal detectada durante el proceso de parsing.
 *
 * Permite recopilar información sobre propiedades deprecadas, fallbacks activados
 * o valores corregidos sin detener la generación del [com.libs.flex.ui.core.domain.model.LayoutDocument].
 *
 * @property path Ruta en formato JSON Pointer / dot-notation donde ocurrió la advertencia.
 * @property code Código identificador estable para métricas y monitoreo (ej. "STYLE_DEPRECATED").
 * @property message Mensaje descriptivo legible para depuración.
 */
public data class ParserWarning(
    public val path: String,
    public val code: String,
    public val message: String
)

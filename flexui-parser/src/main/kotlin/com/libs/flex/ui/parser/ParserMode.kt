package com.libs.flex.ui.parser

/**
 * Modo de operación del parser.
 */
public enum class ParserMode {
    /**
     * Cualquier propiedad inválida o tipo desconocido detiene el parsing y retorna un error `Either.Left`.
     * Recomendado para desarrollo, CI/CD y pruebas unitarias.
     */
    STRICT,

    /**
     * Errores en nodos individuales se degradan a [com.libs.flex.ui.core.domain.model.node.UnsupportedFallbackNode]
     * y las propiedades opcionales inválidas se descartan emitiendo un [ParserWarning].
     * Recomendado para entornos de producción.
     */
    TOLERANT
}
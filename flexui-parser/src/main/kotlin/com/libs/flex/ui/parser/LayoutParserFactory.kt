package com.libs.flex.ui.parser

import com.libs.flex.ui.parser.internal.DefaultLayoutParser

/**
 * Fábrica para la creación desacoplada de instancias de [LayoutParser].
 *
 * Aísla la interfaz abstracta [LayoutParser] de su implementación concreta interna [DefaultLayoutParser].
 */
public class LayoutParserFactory {

    /**
     * Crea una nueva instancia de la implementación predeterminada de [LayoutParser].
     */
    public fun create(): LayoutParser = DefaultLayoutParser()

    public companion object {
        /**
         * Método de conveniencia para crear la implementación predeterminada de [LayoutParser].
         */
        public fun create(): LayoutParser = DefaultLayoutParser()
    }
}

package com.libs.flex.ui.parser.internal.reader

import com.libs.flex.ui.parser.ParserMode

/**
 * Fábrica abstracta para la creación desacoplada de instancias de [ErrorPolicy].
 */
internal interface ErrorPolicyFactory {

    /**
     * Retorna la política de resolución de errores correspondiente a [mode].
     */
    fun create(mode: ParserMode): ErrorPolicy
}

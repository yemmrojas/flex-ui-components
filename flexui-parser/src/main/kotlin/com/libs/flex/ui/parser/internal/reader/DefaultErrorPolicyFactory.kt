package com.libs.flex.ui.parser.internal.reader

import com.libs.flex.ui.parser.ParserMode

/**
 * Implementación predeterminada de [ErrorPolicyFactory].
 */
internal class DefaultErrorPolicyFactory : ErrorPolicyFactory {

    override fun create(mode: ParserMode): ErrorPolicy = when (mode) {
        ParserMode.STRICT -> StrictErrorPolicy()
        ParserMode.TOLERANT -> TolerantErrorPolicy()
    }
}

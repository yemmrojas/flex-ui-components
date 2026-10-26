package com.libs.flex.ui.parser.internal.util

/**
 * Tuplas inmutables auxiliares para combinar múltiples resultados [com.libs.flex.ui.core.domain.functional.Either].
 */
internal data class Quad<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

internal data class Quint<out A, out B, out C, out D, out E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)

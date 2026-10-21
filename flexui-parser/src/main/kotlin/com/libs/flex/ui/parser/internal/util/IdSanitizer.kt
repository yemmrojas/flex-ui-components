package com.libs.flex.ui.parser.internal.util

import com.libs.flex.ui.core.domain.value.ComponentId

/**
 * Utilidad interna para sanear identificadores de componentes antes de instanciar [ComponentId].
 *
 * Garantiza que cualquier ID resultante cumpla con la regla de formato:
 * `^[a-zA-Z0-9_-]{1,64}$`, convirtiendo caracteres especiales (puntos, espacios, barras, corchetes)
 * en guiones bajos y truncando a un máximo de 64 caracteres.
 */
internal object IdSanitizer {

    private val INVALID_CHARACTERS_REGEX = Regex("[^a-zA-Z0-9_-]+")
    private const val MAX_ID_LENGTH = 64
    private const val DEFAULT_FALLBACK = "auto_node"

    /**
     * Sanea una cadena de texto para conformar un identificador válido.
     *
     * @param rawId Identificador original recibido en el JSON, o null.
     * @param fallbackPrefix Prefijo o identificador de reemplazo en caso de ser nulo o vacío.
     * @return Cadena garantizada de cumplir `^[a-zA-Z0-9_-]{1,64}$`.
     */
    fun sanitize(rawId: String?, fallbackPrefix: String = DEFAULT_FALLBACK): String {
        val trimmed = rawId?.trim().orEmpty()

        val rawCandidate = trimmed.ifEmpty {
            fallbackPrefix.trim()
        }

        val replaced = rawCandidate.replace(INVALID_CHARACTERS_REGEX, "_").trim('_')
        val normalized = replaced.ifEmpty { DEFAULT_FALLBACK }

        return if (normalized.length > MAX_ID_LENGTH) {
            normalized.substring(0, MAX_ID_LENGTH)
        } else {
            normalized
        }
    }

    /**
     * Sanea el identificador y lo retorna directamente como un [ComponentId] válido.
     */
    fun sanitizeToComponentId(rawId: String?, fallbackPrefix: String = DEFAULT_FALLBACK): ComponentId {
        val sanitized = sanitize(rawId, fallbackPrefix)
        return ComponentId.of(sanitized).fold(
            onLeft = {
                // Fallback defensivo garantizado
                ComponentId.of(DEFAULT_FALLBACK).fold(
                    onLeft = { error("Default fallback ID '$DEFAULT_FALLBACK' must be valid") },
                    onRight = { it }
                )
            },
            onRight = { it }
        )
    }
}

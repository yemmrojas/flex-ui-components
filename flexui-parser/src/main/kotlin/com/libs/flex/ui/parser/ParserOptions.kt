package com.libs.flex.ui.parser

import com.libs.flex.ui.core.domain.model.LayoutLimits
import com.libs.flex.ui.core.domain.value.SchemaVersion

/**
 * Opciones de configuración para el motor de parsing de FlexUI.
 *
 * @property mode Modo de resolución de errores (STRICT o TOLERANT).
 * @property maxPayloadBytes Tamaño máximo en bytes UTF-8 admitido para el payload JSON.
 * @property limits Límites estructurales del layout validados para `:flexui-core`.
 * @property defaultSchemaVersion Versión de esquema asignada si el JSON representa un nodo raíz directo sin envoltura.
 */
public data class ParserOptions(
    public val mode: ParserMode = ParserMode.STRICT,
    public val maxPayloadBytes: Long = DEFAULT_MAX_PAYLOAD_BYTES,
    public val limits: LayoutLimits = LayoutLimits.DEFAULT,
    public val defaultSchemaVersion: SchemaVersion = SchemaVersion.CURRENT
) {
    public val maxDepth: Int get() = limits.maxDepth
    public val maxNodes: Int get() = limits.maxNodes
    public val maxErrors: Int get() = limits.maxErrors

    /**
     * Constructor de conveniencia que valida [maxDepth], [maxNodes] y [maxErrors] con [LayoutLimits.of].
     * Lanza [IllegalArgumentException] si alguno de los límites es <= 0.
     */
    public constructor(
        mode: ParserMode = ParserMode.STRICT,
        maxPayloadBytes: Long = DEFAULT_MAX_PAYLOAD_BYTES,
        maxDepth: Int = LayoutLimits.DEFAULT_MAX_DEPTH,
        defaultSchemaVersion: SchemaVersion = SchemaVersion.CURRENT,
        maxNodes: Int = LayoutLimits.DEFAULT_MAX_NODES,
        maxErrors: Int = LayoutLimits.DEFAULT_MAX_ERRORS
    ) : this(
        mode = mode,
        maxPayloadBytes = maxPayloadBytes,
        limits = LayoutLimits.of(maxDepth = maxDepth, maxNodes = maxNodes, maxErrors = maxErrors).fold(
            onLeft = { err -> throw IllegalArgumentException(err.reason) },
            onRight = { it }
        ),

        defaultSchemaVersion = defaultSchemaVersion
    )

    init {
        require(maxPayloadBytes > 0) { "maxPayloadBytes must be > 0, was: $maxPayloadBytes" }
    }

    public companion object {
        public const val DEFAULT_MAX_PAYLOAD_BYTES: Long = 10 * 1024 * 1024L // 10 MB

        public val DEFAULT: ParserOptions = ParserOptions()
    }
}

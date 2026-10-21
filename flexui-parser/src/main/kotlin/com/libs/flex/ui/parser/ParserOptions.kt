package com.libs.flex.ui.parser

import com.libs.flex.ui.core.domain.model.LayoutLimits
import com.libs.flex.ui.core.domain.value.SchemaVersion

/**
 * Opciones de configuración para el motor de parsing de FlexUI.
 *
 * @property mode Modo de resolución de errores (STRICT o TOLERANT).
 * @property maxPayloadBytes Tamaño máximo en bytes UTF-8 admitido para el payload JSON.
 * @property maxDepth Profundidad máxima de anidación permitida para el árbol de componentes.
 * @property defaultSchemaVersion Versión de esquema asignada si el JSON representa un nodo raíz directo sin envoltura.
 */
public data class ParserOptions(
    public val mode: ParserMode = ParserMode.STRICT,
    public val maxPayloadBytes: Long = DEFAULT_MAX_PAYLOAD_BYTES,
    public val maxDepth: Int = LayoutLimits.DEFAULT_MAX_DEPTH,
    public val defaultSchemaVersion: SchemaVersion = SchemaVersion.CURRENT
) {
    public companion object {
        public const val DEFAULT_MAX_PAYLOAD_BYTES: Long = 10 * 1024 * 1024L // 10 MB

        public val DEFAULT: ParserOptions = ParserOptions()
    }
}

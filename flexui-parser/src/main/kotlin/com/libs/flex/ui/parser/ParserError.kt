package com.libs.flex.ui.parser

import com.libs.flex.ui.core.domain.error.DomainError

/**
 * Jerarquía sellada de errores ocurridos durante el proceso de deserialización,
 * validación y mapeo hacia el Core de FlexUI.
 *
 * Cada error preserva la ruta de ubicación [path] en el documento JSON y un mensaje legible [message].
 */
public sealed class ParserError {
    public abstract val path: String
    public abstract val message: String

    /**
     * El JSON suministrado es sintácticamente inválido o truncado.
     */
    public data class InvalidJson(
        override val path: String,
        public val reason: String
    ) : ParserError() {
        override val message: String = "Invalid JSON at '$path': $reason"
    }

    /**
     * El tamaño en bytes del documento excede el límite máximo permitido.
     */
    public data class PayloadTooLarge(
        public val sizeBytes: Long,
        public val maxBytes: Long
    ) : ParserError() {
        override val path: String = ""
        override val message: String = "Payload size ($sizeBytes bytes) exceeds limit ($maxBytes bytes)"
    }

    /**
     * La profundidad del árbol JSON supera el límite configurado.
     */
    public data class MaxDepthExceeded(
        override val path: String,
        public val currentDepth: Int,
        public val maxDepth: Int
    ) : ParserError() {
        override val message: String = "Max depth exceeded at '$path': current $currentDepth, max $maxDepth"
    }

    /**
     * Falta una propiedad obligatoria requerida por el esquema del nodo.
     */
    public data class MissingRequiredProperty(
        override val path: String,
        public val property: String
    ) : ParserError() {
        override val message: String = "Missing required property '$property' at '$path'"
    }

    /**
     * Una propiedad posee un valor inválido o no convertible.
     * Preserva el [DomainError.Value] cuando la causa es una violación de invariante en el Core.
     */
    public data class InvalidPropertyValue(
        override val path: String,
        public val property: String,
        public val reason: String,
        public val domainError: DomainError.Value? = null
    ) : ParserError() {
        override val message: String = "Invalid property '$property' at '$path': $reason"
    }

    /**
     * Se encontró un identificador de componente duplicado en el árbol JSON.
     */
    public data class DuplicateComponentId(
        override val path: String,
        public val id: String
    ) : ParserError() {
        override val message: String = "Duplicate component ID '$id' at '$path'"
    }

    /**
     * El tipo de componente no fue reconocido por el registro de mapeadores.
     */
    public data class UnknownComponentType(
        override val path: String,
        public val type: String
    ) : ParserError() {
        override val message: String = "Unknown component type '$type' at '$path'"
    }

    /**
     * Error estructural reportado por el Core al construir el [com.libs.flex.ui.core.domain.model.LayoutDocument].
     */
    public data class CoreStructuralError(
        override val path: String,
        public val structuralError: DomainError.Structural
    ) : ParserError() {
        override val message: String = "Core structural error at '$path': ${formatCoreError(structuralError)}"

        public companion object {
            private fun formatCoreError(error: DomainError.Structural): String = when (error) {
                is DomainError.Structural.DuplicateComponentId -> "Duplicate component ID '${error.id.value}'"
                is DomainError.Structural.MaxNestingDepthExceeded -> "Component '${error.id.value}' exceeded nesting depth limit (depth: ${error.depth})"
                is DomainError.Structural.MaxNodesExceeded -> "Node count ${error.count} exceeds limit ${error.max}"
                is DomainError.Structural.InvalidComponentReference -> "Invalid reference '${error.referenceId.value}' from '${error.id.value}'"
                is DomainError.Structural.DuplicateTemplateUsage -> "Duplicate template '${error.templateId.value}' used by list '${error.listId.value}'"
                is DomainError.Structural.UnsupportedSchemaVersion -> "Unsupported schema version '${error.version}' (supported: ${error.supportedMin} to ${error.supportedMax})"
            }
        }
    }

    /**
     * Error interno inesperado ocurrido durante el proceso de parsing o mapeo.
     */
    public data class InternalError(
        override val path: String,
        public val cause: Throwable
    ) : ParserError() {
        override val message: String = "Internal parser error at '$path': ${cause.message ?: cause::class.simpleName}"
    }
}


package com.libs.flex.ui.core.domain.error

import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.SessionId

public sealed interface DomainError {
    public sealed interface Structural : DomainError {
        public data class DuplicateComponentId(public val id: ComponentId) : Structural
        public data class MaxNestingDepthExceeded(public val id: ComponentId, public val depth: Int) : Structural
        public data class MaxNodesExceeded(public val count: Int, public val max: Int) : Structural
        public data class InvalidComponentReference(public val id: ComponentId, public val referenceId: ComponentId) : Structural
        public data class UnsupportedSchemaVersion(public val version: String, public val supportedMin: String, public val supportedMax: String) : Structural
    }

    public sealed interface Value : DomainError {
        public data class InvalidColorFormat(public val raw: String) : Value
        public data class InvalidIdentifier(public val raw: String) : Value
        public data class InvalidNumberRange(public val reason: String) : Value
        public data class InvalidDimension(public val reason: String) : Value
        public data class InvalidRegexPattern(public val pattern: String, public val reason: String) : Value
        public data class ValueTypeMismatch(public val expected: String, public val actual: String) : Value
        public data class InvalidOptionSelected(public val value: String) : Value
        public data class ValueOutOfRange(public val value: Double, public val min: Double, public val max: Double) : Value
        public data class InvalidStepValue(public val value: Double, public val step: Double) : Value
    }

    public sealed interface Interaction : DomainError {
        public data class SessionNotFound(public val sessionId: SessionId) : Interaction
        public data class ComponentNotFound(public val id: ComponentId) : Interaction
        public data class ComponentNotInputCapable(public val id: ComponentId) : Interaction
        public data class ComponentNotInteractive(public val id: ComponentId) : Interaction
        public data class ComponentDisabled(public val id: ComponentId) : Interaction
        public data class FormValidationFailed(public val errorsCount: Int) : Interaction
        public data class HostExecutionFailed(public val reason: String) : Interaction
    }
}

package com.libs.flex.ui.core.domain.event

import com.libs.flex.ui.core.domain.value.ActionId
import com.libs.flex.ui.core.domain.value.FieldValue
import com.libs.flex.ui.core.domain.value.NodeKey
import com.libs.flex.ui.core.domain.value.SchemaVersion
import com.libs.flex.ui.core.domain.value.SessionId

public sealed interface DomainEvent {
    public val timestamp: Long

    public data class FormSessionStartedEvent(
        public val sessionId: SessionId,
        public val layoutVersion: SchemaVersion,
        override val timestamp: Long
    ) : DomainEvent

    public data class InputValueChangedEvent(
        public val key: NodeKey,
        public val value: FieldValue,
        override val timestamp: Long
    ) : DomainEvent

    public data class InputValidationFailedEvent(
        public val key: NodeKey,
        public val message: String,
        override val timestamp: Long
    ) : DomainEvent

    public data class InputValidationRecoveredEvent(
        public val key: NodeKey,
        override val timestamp: Long
    ) : DomainEvent

    public data class UserActionDispatchedEvent(
        public val actionId: ActionId,
        public val sourceKey: NodeKey,
        override val timestamp: Long
    ) : DomainEvent

    public data class UserActionRejectedEvent(
        public val actionId: ActionId,
        public val sourceKey: NodeKey,
        public val reason: String,
        override val timestamp: Long
    ) : DomainEvent

    public data class UserActionUnhandledEvent(
        public val actionId: ActionId,
        public val sourceKey: NodeKey,
        override val timestamp: Long
    ) : DomainEvent
}

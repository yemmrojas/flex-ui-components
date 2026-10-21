package com.libs.flex.ui.core.domain.model

import com.libs.flex.ui.core.domain.event.DomainEvent

internal data class InternalStateOutcome(
    val state: FormInteractionState,
    val events: List<DomainEvent>
)

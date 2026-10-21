package com.libs.flex.ui.core.domain.model

import com.libs.flex.ui.core.domain.event.DomainEvent

public data class FormOutcome(
    public val session: FormSession,
    public val events: List<DomainEvent>
)

package com.libs.flex.ui.flexui.adapter.publisher

import com.libs.flex.ui.core.domain.event.DomainEvent
import com.libs.flex.ui.core.domain.ports.DomainEventPublisherPort

/**
 * No-operation implementation of [DomainEventPublisherPort] for release builds or silent testing.
 */
class NoOpPublisherAdapter : DomainEventPublisherPort {
    override suspend fun publish(event: DomainEvent) {
        // Intentionally no-op
    }
}


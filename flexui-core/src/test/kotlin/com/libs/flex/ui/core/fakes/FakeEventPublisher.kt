package com.libs.flex.ui.core.fakes

import com.libs.flex.ui.core.domain.event.DomainEvent
import com.libs.flex.ui.core.domain.ports.DomainEventPublisherPort

class FakeEventPublisher : DomainEventPublisherPort {
    val events: MutableList<DomainEvent> = mutableListOf()
    var exceptionToThrow: Throwable? = null

    override suspend fun publish(event: DomainEvent) {
        exceptionToThrow?.let { throw it }
        events.add(event)
    }

    fun clear() {
        events.clear()
        exceptionToThrow = null
    }
}

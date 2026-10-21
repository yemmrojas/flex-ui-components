package com.libs.flex.ui.core.domain.ports

import com.libs.flex.ui.core.domain.event.DomainEvent
import kotlin.coroutines.cancellation.CancellationException

public interface DomainEventPublisherPort {
    public suspend fun publish(event: DomainEvent)
}

public suspend fun DomainEventPublisherPort.publishSafely(event: DomainEvent) {
    try {
        publish(event)
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        // Ignora fallos no críticos de telemetría secundaria
    }
}

public suspend fun DomainEventPublisherPort.publishAllSafely(events: Iterable<DomainEvent>) {
    for (event in events) {
        publishSafely(event)
    }
}

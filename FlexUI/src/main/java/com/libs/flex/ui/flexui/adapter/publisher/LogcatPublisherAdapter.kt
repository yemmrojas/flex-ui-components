package com.libs.flex.ui.flexui.adapter.publisher

import android.util.Log
import com.libs.flex.ui.core.domain.event.DomainEvent
import com.libs.flex.ui.core.domain.ports.DomainEventPublisherPort

/**
 * Android Logcat implementation of [DomainEventPublisherPort].
 *
 * Designed for development/debug environments to observe domain lifecycle events
 * without leaking raw sensitive credentials (which are already masked as "••••••••"
 * in [DomainEvent.InputValueChangedEvent] by :flexui-core).
 */
class LogcatPublisherAdapter(
    private val tag: String = "FlexUI-Events"
) : DomainEventPublisherPort {

    override suspend fun publish(event: DomainEvent) {
        Log.d(tag, "DomainEvent emitted: ${event::class.simpleName} -> $event")
    }
}


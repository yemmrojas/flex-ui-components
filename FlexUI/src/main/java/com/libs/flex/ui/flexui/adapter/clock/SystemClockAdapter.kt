package com.libs.flex.ui.flexui.adapter.clock

import com.libs.flex.ui.core.domain.ports.ClockPort

/**
 * Production implementation of [ClockPort] using the system's real-time clock.
 */
class SystemClockAdapter : ClockPort {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}


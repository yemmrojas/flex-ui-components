package com.libs.flex.ui.core.fakes

import com.libs.flex.ui.core.domain.ports.ClockPort

class FakeClock(private var currentTime: Long = 1000L) : ClockPort {
    override fun currentTimeMillis(): Long = currentTime

    fun advanceBy(millis: Long) {
        currentTime += millis
    }

    fun setTime(millis: Long) {
        currentTime = millis
    }
}

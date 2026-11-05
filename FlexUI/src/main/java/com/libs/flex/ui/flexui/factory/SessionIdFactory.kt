package com.libs.flex.ui.flexui.factory

import com.libs.flex.ui.core.domain.value.SessionId

/**
 * Factory responsible for generating unique [SessionId] instances.
 */
interface SessionIdFactory {
    fun generate(): SessionId
}

/**
 * Default timestamp-based generator producing validated [SessionId] values.
 */
class DefaultSessionIdFactory : SessionIdFactory {
    override fun generate(): SessionId {
        val raw = "session_${System.currentTimeMillis()}"
        return SessionId.of(raw).getOrNull()
            ?: error("Illegal state: generated session ID '$raw' is invalid")
    }
}


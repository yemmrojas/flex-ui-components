package com.libs.flex.ui.core.fakes

import com.libs.flex.ui.core.domain.model.FormSession
import com.libs.flex.ui.core.domain.ports.FormSessionPort
import com.libs.flex.ui.core.domain.value.SessionId

class FakeFormSessionRepository : FormSessionPort {
    private val sessions: MutableMap<SessionId, FormSession> = mutableMapOf()

    override suspend fun load(id: SessionId): FormSession? = sessions[id]

    override suspend fun save(session: FormSession) {
        sessions[session.sessionId] = session
    }

    fun clear() {
        sessions.clear()
    }

    fun allSessions(): List<FormSession> = sessions.values.toList()

    fun contains(id: SessionId): Boolean = sessions.containsKey(id)
}

package com.libs.flex.ui.flexui.adapter.session

import com.libs.flex.ui.core.domain.model.FormSession
import com.libs.flex.ui.core.domain.value.SessionId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

/**
 * Thread-safe, reactive in-memory implementation of [ObservableFormSessionPort].
 *
 * Guarantees serial execution of updates per session using dedicated [Mutex] instances,
 * and emits real-time updates via [Flow] backed by [MutableStateFlow].
 */
class InMemoryFormSessionAdapter : ObservableFormSessionPort {

    private val sessions = ConcurrentHashMap<SessionId, MutableStateFlow<FormSession?>>()
    private val locks = ConcurrentHashMap<SessionId, Mutex>()

    private fun getLock(id: SessionId): Mutex = locks.computeIfAbsent(id) { Mutex() }

    override suspend fun save(session: FormSession) {
        val lock = getLock(session.sessionId)
        lock.withLock {
            val flow = sessions.computeIfAbsent(session.sessionId) { MutableStateFlow(null) }
            flow.value = session
        }
    }

    override suspend fun load(id: SessionId): FormSession? {
        val lock = getLock(id)
        return lock.withLock {
            sessions[id]?.value
        }
    }

    override suspend fun delete(id: SessionId) {
        val lock = getLock(id)
        lock.withLock {
            sessions.remove(id)
            locks.remove(id)
        }
    }

    override fun observeSession(id: SessionId): Flow<FormSession> {
        val flow = sessions.computeIfAbsent(id) { MutableStateFlow(null) }
        return flow.filterNotNull()
    }

    override fun contains(id: SessionId): Boolean = sessions[id]?.value != null
}

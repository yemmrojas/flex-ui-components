package com.libs.flex.ui.flexui.adapter.session

import com.libs.flex.ui.core.domain.model.FormSession
import com.libs.flex.ui.core.domain.ports.FormSessionPort
import com.libs.flex.ui.core.domain.value.SessionId
import kotlinx.coroutines.flow.Flow

/**
 * Presentation-level port that extends [FormSessionPort] to add reactive streaming
 * capabilities required by UI layers (such as ViewModels and Jetpack Compose).
 *
 * Keeps domain contracts in `:flexui-core` pure and transaction-oriented while allowing
 * delivery adapters to observe state changes as a [Flow].
 */
interface ObservableFormSessionPort : FormSessionPort {

    /**
     * Observes real-time state changes of an active [FormSession].
     *
     * @param id The session identifier to observe.
     * @return A cold or hot [Flow] emitting updated [FormSession] states.
     */
    fun observeSession(id: SessionId): Flow<FormSession>

    /**
     * Checks whether an active session is currently retained in memory.
     *
     * @param id The session identifier.
     * @return True if present, false otherwise.
     */
    fun contains(id: SessionId): Boolean
}


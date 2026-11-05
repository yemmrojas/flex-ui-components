package com.libs.flex.ui.flexui.adapter.host

import com.libs.flex.ui.core.domain.ports.ActionResult
import com.libs.flex.ui.core.domain.ports.HostActionHandlerPort
import com.libs.flex.ui.core.domain.ports.UserAction
import kotlinx.coroutines.CancellationException

/**
 * Adapter that delegates [HostActionHandlerPort] invocations to a suspending lambda provided by the host,
 * catching unexpected host failures and converting them safely to [ActionResult.Rejected].
 */
class SuspendHostActionAdapter(
    private val onAction: suspend (UserAction) -> ActionResult
) : HostActionHandlerPort {

    override suspend fun handleAction(action: UserAction): ActionResult {
        return try {
            onAction(action)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ActionResult.Rejected("Host action execution failed: ${e.message ?: e.javaClass.simpleName}")
        }
    }
}


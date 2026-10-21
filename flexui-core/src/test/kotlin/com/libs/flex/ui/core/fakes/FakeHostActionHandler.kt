package com.libs.flex.ui.core.fakes

import com.libs.flex.ui.core.domain.ports.ActionResult
import com.libs.flex.ui.core.domain.ports.HostActionHandlerPort
import com.libs.flex.ui.core.domain.ports.UserAction
import com.libs.flex.ui.core.domain.value.ActionId

class FakeHostActionHandler(
    var defaultResult: ActionResult = ActionResult.Handled
) : HostActionHandlerPort {
    val recordedActions: MutableList<UserAction> = mutableListOf()
    private val customResults: MutableMap<ActionId, ActionResult> = mutableMapOf()

    fun whenAction(actionId: ActionId, result: ActionResult) {
        customResults[actionId] = result
    }

    override suspend fun handleAction(action: UserAction): ActionResult {
        recordedActions.add(action)
        return customResults[action.actionId] ?: defaultResult
    }

    fun clear() {
        recordedActions.clear()
        customResults.clear()
    }
}

package com.libs.flex.ui.core.domain.ports

public interface HostActionHandlerPort {
    public suspend fun handleAction(action: UserAction): ActionResult
}

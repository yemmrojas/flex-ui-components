package com.libs.flex.ui.core.application.command

import com.libs.flex.ui.core.domain.ports.ActionResult

public data class ProcessUserActionCommand(
    override val sessionId: String,
    public val componentId: String,
    public val instanceIndex: Int? = null
) : Command<ActionResult>, SessionCommand {
    override val commandName: String = COMMAND_NAME

    public companion object {
        public const val COMMAND_NAME: String = "ui.action.process_click"
    }
}

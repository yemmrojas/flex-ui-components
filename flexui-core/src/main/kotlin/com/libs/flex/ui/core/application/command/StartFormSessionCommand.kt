package com.libs.flex.ui.core.application.command

import com.libs.flex.ui.core.domain.model.FormSession

public data class StartFormSessionCommand(
    override val sessionId: String,
    public val layoutId: String
) : Command<FormSession>, SessionCommand {
    override val commandName: String = COMMAND_NAME

    public companion object {
        public const val COMMAND_NAME: String = "ui.session.start"
    }
}

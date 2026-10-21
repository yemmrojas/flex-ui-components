package com.libs.flex.ui.core.application.command

import com.libs.flex.ui.core.domain.model.FormSession

public data class UpdateInputValueCommand(
    override val sessionId: String,
    public val componentId: String,
    public val rawValue: String,
    public val instanceIndex: Int? = null
) : Command<FormSession>, SessionCommand {
    override val commandName: String = COMMAND_NAME

    public companion object {
        public const val COMMAND_NAME: String = "ui.input.update_value"
    }
}

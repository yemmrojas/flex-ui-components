package com.libs.flex.ui.core.application.command

import com.libs.flex.ui.core.domain.model.FormSession
import com.libs.flex.ui.core.domain.value.FieldValue

public data class UpdateInputValueCommand(
    override val sessionId: String,
    public val componentId: String,
    public val value: FieldValue,
    public val instanceIndex: Int? = null
) : Command<FormSession>, SessionCommand {
    override val commandName: String = COMMAND_NAME

    public companion object {
        public const val COMMAND_NAME: String = "ui.input.update_value"
    }
}

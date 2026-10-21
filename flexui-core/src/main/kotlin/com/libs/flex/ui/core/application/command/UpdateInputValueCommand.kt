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

    public val rawValue: String
        get() = when (value) {
            is FieldValue.Text -> value.value
            is FieldValue.Bool -> value.value.toString()
            is FieldValue.Number -> value.value.toString()
            is FieldValue.Selection -> value.selectedIndices.joinToString(",")
        }

    public constructor(
        sessionId: String,
        componentId: String,
        rawValue: String,
        instanceIndex: Int? = null
    ) : this(
        sessionId = sessionId,
        componentId = componentId,
        value = FieldValue.Text(rawValue),
        instanceIndex = instanceIndex
    )

    public companion object {
        public const val COMMAND_NAME: String = "ui.input.update_value"

        public fun text(
            sessionId: String,
            componentId: String,
            text: String,
            instanceIndex: Int? = null
        ): UpdateInputValueCommand = UpdateInputValueCommand(
            sessionId, componentId,
            FieldValue.Text(text), instanceIndex
        )

        public fun bool(
            sessionId: String,
            componentId: String,
            checked: Boolean,
            instanceIndex: Int? = null
        ): UpdateInputValueCommand = UpdateInputValueCommand(
            sessionId, componentId,
            FieldValue.Bool(checked),
            instanceIndex
        )

        public fun number(
            sessionId: String,
            componentId: String,
            number: Double,
            instanceIndex: Int? = null
        ): UpdateInputValueCommand = UpdateInputValueCommand(
            sessionId, componentId,
            FieldValue.Number(number),
            instanceIndex
        )

        public fun selection(
            sessionId: String,
            componentId: String,
            selectedIndices: List<Int>,
            instanceIndex: Int? = null
        ): UpdateInputValueCommand = UpdateInputValueCommand(
            sessionId,
            componentId,
            FieldValue.Selection(selectedIndices),
            instanceIndex
        )
    }
}

package com.libs.flex.ui.core.application.command

import com.libs.flex.ui.core.application.bus.CommandHandler
import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.model.FormSession
import com.libs.flex.ui.core.domain.ports.ActionResult
import com.libs.flex.ui.core.domain.value.FieldValue
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class CommandContractsTest {

    @Test
    fun `StartFormSessionCommand satisfies Command and SessionCommand contracts`() {
        val cmd1 = StartFormSessionCommand(sessionId = "session-123", layoutId = "layout-home")
        val cmd2 = StartFormSessionCommand(sessionId = "session-123", layoutId = "layout-home")
        val cmd3 = cmd1.copy(layoutId = "layout-profile")

        assertIs<Command<FormSession>>(cmd1)
        assertIs<SessionCommand>(cmd1)
        assertEquals("ui.session.start", cmd1.commandName)
        assertEquals(StartFormSessionCommand.COMMAND_NAME, cmd1.commandName)
        assertEquals("session-123", cmd1.sessionId)
        assertEquals("layout-home", cmd1.layoutId)

        assertEquals(cmd1, cmd2)
        assertEquals(cmd1.hashCode(), cmd2.hashCode())
        assertNotEquals(cmd1, cmd3)
    }

    @Test
    fun `UpdateInputValueCommand satisfies Command and SessionCommand contracts`() {
        val cmdWithNullIndex = UpdateInputValueCommand(
            sessionId = "session-123",
            componentId = "input-name",
            rawValue = "Jane Doe"
        )
        assertNull(cmdWithNullIndex.instanceIndex)
        assertEquals(FieldValue.Text("Jane Doe"), cmdWithNullIndex.value)

        val cmdWithIndex = UpdateInputValueCommand(
            sessionId = "session-123",
            componentId = "input-item",
            rawValue = "Item 2",
            instanceIndex = 2
        )

        assertIs<Command<FormSession>>(cmdWithIndex)
        assertIs<SessionCommand>(cmdWithIndex)
        assertEquals("ui.input.update_value", cmdWithIndex.commandName)
        assertEquals(UpdateInputValueCommand.COMMAND_NAME, cmdWithIndex.commandName)
        assertEquals("session-123", cmdWithIndex.sessionId)
        assertEquals("input-item", cmdWithIndex.componentId)
        assertEquals("Item 2", cmdWithIndex.rawValue)
        assertEquals(FieldValue.Text("Item 2"), cmdWithIndex.value)
        assertEquals(2, cmdWithIndex.instanceIndex)

        // Native typed commands
        val boolCmd = UpdateInputValueCommand.bool("session-123", "check-agree", true)
        assertEquals(FieldValue.Bool(true), boolCmd.value)
        assertEquals("true", boolCmd.rawValue)

        val numCmd = UpdateInputValueCommand.number("session-123", "slider-price", 49.99)
        assertEquals(FieldValue.Number(49.99), numCmd.value)
        assertEquals("49.99", numCmd.rawValue)

        val selCmd = UpdateInputValueCommand.selection("session-123", "select-tags", listOf(0, 2))
        assertEquals(FieldValue.Selection(listOf(0, 2)), selCmd.value)
        assertEquals("0,2", selCmd.rawValue)
    }

    @Test
    fun `ProcessUserActionCommand satisfies Command and SessionCommand contracts`() {
        val cmd = ProcessUserActionCommand(
            sessionId = "session-456",
            componentId = "btn-submit",
            instanceIndex = 1
        )

        assertIs<Command<ActionResult>>(cmd)
        assertIs<SessionCommand>(cmd)
        assertEquals("ui.action.process_click", cmd.commandName)
        assertEquals(ProcessUserActionCommand.COMMAND_NAME, cmd.commandName)
        assertEquals("session-456", cmd.sessionId)
        assertEquals("btn-submit", cmd.componentId)
        assertEquals(1, cmd.instanceIndex)

        val cmdDefault = ProcessUserActionCommand(
            sessionId = "session-456",
            componentId = "btn-submit"
        )
        assertNull(cmdDefault.instanceIndex)
    }

    @Test
    fun `CommandHandler enforces invariant return type R`() = runTest {
        val handler = object : CommandHandler<StartFormSessionCommand, FormSession> {
            override suspend fun handle(command: StartFormSessionCommand): Either<DomainError, FormSession> {
                return Either.Left(DomainError.Structural.UnsupportedSchemaVersion("unknown", "1.0", "1.0"))
            }
        }

        val cmd = StartFormSessionCommand("sess", "layout")
        val result = handler.handle(cmd)
        assertIs<Either.Left<DomainError>>(result)
    }
}

package com.libs.flex.ui.core.application.command

import com.libs.flex.ui.core.application.bus.CommandHandler
import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.model.FormSession
import com.libs.flex.ui.core.domain.ports.ActionResult
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
        assertEquals(2, cmdWithIndex.instanceIndex)
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

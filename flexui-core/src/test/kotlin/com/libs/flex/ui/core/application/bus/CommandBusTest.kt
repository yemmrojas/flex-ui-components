package com.libs.flex.ui.core.application.bus

import com.libs.flex.ui.core.application.bus.middleware.SessionLockMiddleware
import com.libs.flex.ui.core.application.command.Command
import com.libs.flex.ui.core.application.command.SessionCommand
import com.libs.flex.ui.core.application.command.StartFormSessionCommand
import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.model.FormSession
import com.libs.flex.ui.core.domain.model.LayoutDocument
import com.libs.flex.ui.core.domain.model.node.TextNode
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.SchemaVersion
import com.libs.flex.ui.core.domain.value.SessionId
import com.libs.flex.ui.core.fakes.FakeClock
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CommandBusTest {

    private data class DummyCommand(val id: String) : Command<String> {
        override val commandName: String = "dummy.command"
    }

    private data class DuplicateNameCommand(val id: String) : Command<String> {
        override val commandName: String = "ui.session.start" // Same as StartFormSessionCommand
    }

    private data class ConcurrentSessionCommand(
        override val sessionId: String,
        val delayMs: Long
    ) : Command<Int>, SessionCommand {
        override val commandName: String = "concurrent.session.cmd"
    }

    private fun sampleSession(): FormSession {
        val root = TextNode(
            id = ComponentId.of("text-1").fold({ throw IllegalStateException() }, { it }),
            text = "Welcome"
        )
        val doc = LayoutDocument.create(SchemaVersion("1.0.0"), root).fold({ throw IllegalStateException() }, { it })
        val sid = SessionId.of("session-1").fold({ throw IllegalStateException() }, { it })
        return FormSession.start(sid, doc, FakeClock()).session
    }

    @Test
    fun `dispatch executes registered handler successfully`() = runTest {
        val expectedSession = sampleSession()
        val handler = object : CommandHandler<StartFormSessionCommand, FormSession> {
            override suspend fun handle(command: StartFormSessionCommand): Either<DomainError, FormSession> {
                return Either.Right(expectedSession)
            }
        }

        val bus = InMemoryCommandBus.builder()
            .register(StartFormSessionCommand::class, StartFormSessionCommand.COMMAND_NAME, handler)
            .build()

        val command = StartFormSessionCommand(sessionId = "session-1", layoutId = "layout-home")
        val result = bus.dispatch(command)

        val session = assertIs<Either.Right<FormSession>>(result).value
        assertEquals(expectedSession, session)
    }

    @Test
    fun `dispatch returns HostExecutionFailed when command is not registered`() = runTest {
        val bus = InMemoryCommandBus.builder().build()

        val command = DummyCommand(id = "123")
        val result = bus.dispatch(command)

        assertIs<Either.Left<DomainError>>(result)
        val error = result.value
        assertIs<DomainError.Interaction.HostExecutionFailed>(error)
        assertTrue(error.reason.contains("dummy.command"))
    }

    @Test
    fun `builder rejects duplicate handler registration for same command class`() {
        val handler = object : CommandHandler<DummyCommand, String> {
            override suspend fun handle(command: DummyCommand): Either<DomainError, String> = Either.Right("ok")
        }

        val builder = InMemoryCommandBus.builder()
            .register(DummyCommand::class, "cmd.1", handler)

        val exception = assertThrows<IllegalStateException> {
            builder.register(DummyCommand::class, "cmd.2", handler)
        }
        assertTrue(exception.message?.contains("Duplicate handler for command class") == true)
    }

    @Test
    fun `builder rejects duplicate commandName registration`() {
        val handler1 = object : CommandHandler<StartFormSessionCommand, FormSession> {
            override suspend fun handle(command: StartFormSessionCommand): Either<DomainError, FormSession> =
                Either.Right(sampleSession())
        }
        val handler2 = object : CommandHandler<DuplicateNameCommand, String> {
            override suspend fun handle(command: DuplicateNameCommand): Either<DomainError, String> =
                Either.Right("ok")
        }

        val builder = InMemoryCommandBus.builder()
            .register(StartFormSessionCommand::class, StartFormSessionCommand.COMMAND_NAME, handler1)

        val exception = assertThrows<IllegalStateException> {
            builder.register(DuplicateNameCommand::class, DuplicateNameCommand("").commandName, handler2)
        }
        assertTrue(exception.message?.contains("Duplicate commandName") == true)
    }

    @Test
    fun `middlewares execute in order and wrap command execution`() = runTest {
        val executionLog = mutableListOf<String>()

        val middleware1 = CommandMiddleware { cmd, next ->
            executionLog.add("m1:before")
            val res = next(cmd)
            executionLog.add("m1:after")
            res
        }

        val middleware2 = CommandMiddleware { cmd, next ->
            executionLog.add("m2:before")
            val res = next(cmd)
            executionLog.add("m2:after")
            res
        }

        val handler = object : CommandHandler<DummyCommand, String> {
            override suspend fun handle(command: DummyCommand): Either<DomainError, String> {
                executionLog.add("handler:executed")
                return Either.Right("done")
            }
        }

        val bus = InMemoryCommandBus.builder()
            .register(DummyCommand::class, DummyCommand("").commandName, handler)
            .addMiddleware(middleware1)
            .addMiddleware(middleware2)
            .build()

        val result = bus.dispatch(DummyCommand("1"))

        assertEquals(Either.Right("done"), result)
        assertEquals(
            listOf("m1:before", "m2:before", "handler:executed", "m2:after", "m1:after"),
            executionLog
        )
    }

    @Test
    fun `SessionLockMiddleware serializes concurrent commands for the same session`() = runTest {
        val currentConcurrency = AtomicInteger(0)
        val maxObservedConcurrency = AtomicInteger(0)

        val handler = object : CommandHandler<ConcurrentSessionCommand, Int> {
            override suspend fun handle(command: ConcurrentSessionCommand): Either<DomainError, Int> {
                val active = currentConcurrency.incrementAndGet()
                maxObservedConcurrency.updateAndGet { current -> maxOf(current, active) }
                delay(command.delayMs)
                currentConcurrency.decrementAndGet()
                return Either.Right(active)
            }
        }

        val bus = InMemoryCommandBus.builder()
            .register(ConcurrentSessionCommand::class, "concurrent.session.cmd", handler)
            .addMiddleware(SessionLockMiddleware())
            .build()

        // Dispatch 5 concurrent commands targeting the same session
        val deferredList = (1..5).map {
            async {
                bus.dispatch(ConcurrentSessionCommand(sessionId = "shared-session-1", delayMs = 50L))
            }
        }

        val results = deferredList.awaitAll()

        // All succeeded
        results.forEach { assertIs<Either.Right<Int>>(it) }

        // Max concurrency for the same session must be strictly 1
        assertEquals(1, maxObservedConcurrency.get())
    }

    @Test
    fun `SessionLockMiddleware allows different sessions to execute concurrently`() = runTest {
        val currentConcurrency = AtomicInteger(0)
        val maxObservedConcurrency = AtomicInteger(0)

        val handler = object : CommandHandler<ConcurrentSessionCommand, Int> {
            override suspend fun handle(command: ConcurrentSessionCommand): Either<DomainError, Int> {
                val active = currentConcurrency.incrementAndGet()
                maxObservedConcurrency.updateAndGet { current -> maxOf(current, active) }
                delay(command.delayMs)
                currentConcurrency.decrementAndGet()
                return Either.Right(active)
            }
        }

        val bus = InMemoryCommandBus.builder()
            .register(ConcurrentSessionCommand::class, "concurrent.session.cmd", handler)
            .addMiddleware(SessionLockMiddleware())
            .build()

        // Dispatch 3 concurrent commands targeting DIFFERENT sessions
        val deferredList = (1..3).map { index ->
            async {
                bus.dispatch(ConcurrentSessionCommand(sessionId = "session-$index", delayMs = 80L))
            }
        }

        val results = deferredList.awaitAll()

        results.forEach { assertIs<Either.Right<Int>>(it) }

        // Multiple distinct sessions run concurrently (observed concurrency > 1)
        assertTrue(maxObservedConcurrency.get() > 1)
    }
}

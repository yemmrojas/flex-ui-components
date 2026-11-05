package com.libs.flex.ui.flexui.usecase

import com.libs.flex.ui.core.application.bus.CommandHandler
import com.libs.flex.ui.core.application.bus.InMemoryCommandBus
import com.libs.flex.ui.core.application.command.StartFormSessionCommand
import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.model.FormSession
import com.libs.flex.ui.core.domain.model.LayoutDocument
import com.libs.flex.ui.core.domain.model.node.InputNode
import com.libs.flex.ui.core.domain.ports.ClockPort
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.SchemaVersion
import com.libs.flex.ui.core.domain.value.SessionId
import com.libs.flex.ui.flexui.adapter.source.DirectStringLayoutSourceAdapter
import com.libs.flex.ui.flexui.factory.SessionIdFactory
import com.libs.flex.ui.flexui.model.FlexUiError
import com.libs.flex.ui.parser.LayoutParserFactory
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InitializeLayoutSessionUseCaseTest {

    private class FakeClock : ClockPort {
        override fun currentTimeMillis(): Long = 1000L
    }

    private class FixedSessionIdFactory(private val fixedId: String) : SessionIdFactory {
        override fun generate(): SessionId = SessionId.of(fixedId).getOrNull()!!
    }

    private class FakeStringLayoutSource(
        var result: Either<FlexUiError.InvalidJson, LayoutDocument>
    ) : com.libs.flex.ui.flexui.adapter.source.StringLayoutSourcePort {
        override suspend fun load(source: String): Either<FlexUiError.InvalidJson, LayoutDocument> = result
        override suspend fun getLayout(request: com.libs.flex.ui.core.domain.ports.LayoutRequest): Either<com.libs.flex.ui.core.domain.functional.Nel<DomainError.Structural>, LayoutDocument> {
            return when (val r = result) {
                is Either.Left -> {
                    val rootId = ComponentId.of("root").getOrNull()!!
                    val error = DomainError.Structural.InvalidComponentReference(rootId, ComponentId.of("not_found").getOrNull()!!)
                    Either.Left(com.libs.flex.ui.core.domain.functional.Nel.of(error))
                }
                is Either.Right -> Either.Right(r.value)
            }
        }
    }

    private val validJson = """
        {
          "schemaVersion": "1.0.0",
          "layoutId": "login_screen",
          "root": {
            "id": "email_input",
            "type": "flexInput",
            "label": "Email"
          }
        }
    """.trimIndent()

    private fun givenSession(id: String): FormSession {
        val doc = LayoutDocument.create(
            schemaVersion = SchemaVersion("1.0.0"),
            root = InputNode(id = ComponentId.of("email_input").getOrNull()!!, label = "Email")
        ).getOrNull()!!
        return FormSession.start(SessionId.of(id).getOrNull()!!, doc, FakeClock()).session
    }

    @Test
    fun `successful parsing and session dispatch returns InitializedLayoutSession`() = runTest {
        val layoutSource = DirectStringLayoutSourceAdapter(parser = LayoutParserFactory.create())
        val startHandler = object : CommandHandler<StartFormSessionCommand, FormSession> {
            override suspend fun handle(command: StartFormSessionCommand): Either<DomainError, FormSession> {
                return Either.Right(givenSession(command.sessionId))
            }
        }

        val bus = InMemoryCommandBus.builder()
            .register(StartFormSessionCommand::class, StartFormSessionCommand.COMMAND_NAME, startHandler)
            .build()

        val idFactory = FixedSessionIdFactory("session_123")
        val useCase = InitializeLayoutSessionUseCase(
            layoutSource = layoutSource,
            commandBus = bus,
            idFactory = idFactory
        )

        val result = useCase(validJson)

        assertTrue(result.isRight)
        val sessionData = result.getOrNull()!!
        assertEquals("session_123", sessionData.sessionId.value)
        assertEquals("email_input", sessionData.rootNode.id.value)
    }

    @Test
    fun `malformed json returns InvalidJson error`() = runTest {
        val layoutSource = DirectStringLayoutSourceAdapter(parser = LayoutParserFactory.create())
        val bus = InMemoryCommandBus.builder().build()
        val useCase = InitializeLayoutSessionUseCase(
            layoutSource = layoutSource,
            commandBus = bus
        )

        val result = useCase("{ malformed json }")

        assertTrue(result.isLeft)
        assertTrue(result.leftOrNull() is FlexUiError.InvalidJson)
    }

    @Test
    fun `failed command dispatch returns SessionError`() = runTest {
        val layoutSource = DirectStringLayoutSourceAdapter(parser = LayoutParserFactory.create())
        val startHandler = object : CommandHandler<StartFormSessionCommand, FormSession> {
            override suspend fun handle(command: StartFormSessionCommand): Either<DomainError, FormSession> {
                val error = DomainError.Interaction.HostExecutionFailed("Command rejected")
                return Either.Left(error)
            }
        }

        val bus = InMemoryCommandBus.builder()
            .register(StartFormSessionCommand::class, StartFormSessionCommand.COMMAND_NAME, startHandler)
            .build()

        val useCase = InitializeLayoutSessionUseCase(
            layoutSource = layoutSource,
            commandBus = bus
        )

        val result = useCase(validJson)

        assertTrue(result.isLeft)
        val error = result.leftOrNull()
        assertTrue(error is FlexUiError.SessionError)
    }

    @Test
    fun `usecase works with pure fake port implementation demonstrating DIP`() = runTest {
        val fakeDoc = LayoutDocument.create(
            schemaVersion = SchemaVersion("1.0.0"),
            root = InputNode(id = ComponentId.of("fake_input").getOrNull()!!, label = "Fake")
        ).getOrNull()!!

        val fakeSource = FakeStringLayoutSource(Either.Right(fakeDoc))
        val startHandler = object : CommandHandler<StartFormSessionCommand, FormSession> {
            override suspend fun handle(command: StartFormSessionCommand): Either<DomainError, FormSession> {
                return Either.Right(givenSession(command.sessionId))
            }
        }

        val bus = InMemoryCommandBus.builder()
            .register(StartFormSessionCommand::class, StartFormSessionCommand.COMMAND_NAME, startHandler)
            .build()

        val idFactory = FixedSessionIdFactory("isolated_session")
        val useCase = InitializeLayoutSessionUseCase(
            layoutSource = fakeSource,
            commandBus = bus,
            idFactory = idFactory
        )

        val result = useCase("any_string")

        assertTrue(result.isRight)
        assertEquals("isolated_session", result.getOrNull()!!.sessionId.value)
        assertEquals("fake_input", result.getOrNull()!!.rootNode.id.value)
    }
}

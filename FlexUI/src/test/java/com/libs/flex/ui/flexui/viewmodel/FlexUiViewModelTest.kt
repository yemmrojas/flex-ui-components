package com.libs.flex.ui.flexui.viewmodel

import app.cash.turbine.test
import com.libs.flex.ui.core.application.bus.CommandBus
import com.libs.flex.ui.core.application.bus.CommandHandler
import com.libs.flex.ui.core.application.bus.InMemoryCommandBus
import com.libs.flex.ui.core.application.command.ProcessUserActionCommand
import com.libs.flex.ui.core.application.command.StartFormSessionCommand
import com.libs.flex.ui.core.application.command.UpdateInputValueCommand
import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.model.FormSession
import com.libs.flex.ui.core.domain.model.LayoutDocument
import com.libs.flex.ui.core.domain.model.node.InputNode
import com.libs.flex.ui.core.domain.ports.ActionResult
import com.libs.flex.ui.core.domain.ports.ClockPort
import com.libs.flex.ui.core.domain.value.ActionId
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.FieldValue
import com.libs.flex.ui.core.domain.value.NodeKey
import com.libs.flex.ui.core.domain.value.SchemaVersion
import com.libs.flex.ui.core.domain.value.SessionId
import com.libs.flex.ui.flexui.FlexUiConfig
import com.libs.flex.ui.flexui.adapter.session.InMemoryFormSessionAdapter
import com.libs.flex.ui.flexui.adapter.session.ObservableFormSessionPort
import com.libs.flex.ui.flexui.adapter.source.DirectStringLayoutSourceAdapter
import com.libs.flex.ui.flexui.factory.DefaultSessionIdFactory
import com.libs.flex.ui.flexui.factory.SessionIdFactory
import com.libs.flex.ui.flexui.model.FlexUiError
import com.libs.flex.ui.flexui.model.FlexUiEvent
import com.libs.flex.ui.flexui.model.FlexUiViewState
import com.libs.flex.ui.flexui.usecase.InitializeLayoutSessionUseCase
import com.libs.flex.ui.parser.LayoutParser
import com.libs.flex.ui.parser.LayoutParserFactory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FlexUiViewModelTest {

    private class TestDispatcherProvider(private val dispatcher: CoroutineDispatcher) : DispatcherProvider {
        override val main: CoroutineDispatcher get() = dispatcher
        override val mainImmediate: CoroutineDispatcher get() = dispatcher
        override val io: CoroutineDispatcher get() = dispatcher
        override val default: CoroutineDispatcher get() = dispatcher
    }

    private class FakeClock(var time: Long = 1000L) : ClockPort {
        override fun currentTimeMillis(): Long = time
    }

    private val sampleJson = """
        {
          "schemaVersion": "1.0.0",
          "layoutId": "test_form",
          "root": {
            "id": "input_username",
            "type": "flexInput",
            "label": "Username"
          }
        }
    """.trimIndent()

    private fun givenSession(id: String = "session_1"): FormSession {
        val rootNode = InputNode(
            id = ComponentId.of("input_username").getOrNull()!!,
            label = "Username"
        )
        val doc = LayoutDocument.create(
            schemaVersion = SchemaVersion("1.0.0"),
            root = rootNode
        ).getOrNull()!!
        return FormSession.start(SessionId.of(id).getOrNull()!!, doc, FakeClock()).session
    }

    private fun givenViewModel(
        commandBus: CommandBus,
        sessionPort: ObservableFormSessionPort,
        config: FlexUiConfig = FlexUiConfig(),
        layoutParser: LayoutParser = LayoutParserFactory.create(),
        idFactory: SessionIdFactory = DefaultSessionIdFactory()
    ): FlexUiViewModel {
        val layoutSource = DirectStringLayoutSourceAdapter(
            parser = layoutParser,
            options = config.parserOptions
        )
        val useCase = InitializeLayoutSessionUseCase(
            layoutSource = layoutSource,
            commandBus = commandBus,
            idFactory = idFactory
        )
        return FlexUiViewModel(
            initializeLayoutSessionUseCase = useCase,
            sessionPort = sessionPort,
            commandBus = commandBus,
            config = config
        )
    }

    @Test
    fun `loadJson starts form session and updates viewState to Content`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val sessionAdapter = InMemoryFormSessionAdapter()

        val startHandler = object : CommandHandler<StartFormSessionCommand, FormSession> {
            override suspend fun handle(command: StartFormSessionCommand): Either<DomainError, FormSession> {
                val session = givenSession(command.sessionId)
                sessionAdapter.save(session)
                return Either.Right(session)
            }
        }

        val bus = InMemoryCommandBus.builder()
            .register(StartFormSessionCommand::class, StartFormSessionCommand.COMMAND_NAME, startHandler)
            .build()

        val config = FlexUiConfig(dispatcherProvider = TestDispatcherProvider(testDispatcher))
        val viewModel = givenViewModel(bus, sessionAdapter, config)

        viewModel.viewState.test {
            assertEquals(FlexUiViewState.Loading, awaitItem())

            viewModel.loadJson(sampleJson)
            testScheduler.advanceUntilIdle()

            val contentState = awaitItem()
            assertTrue(contentState is FlexUiViewState.Content)
            assertEquals("input_username", (contentState as FlexUiViewState.Content).state.layoutId)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadJson with invalid JSON emits FlexUiViewState Error`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val sessionAdapter = InMemoryFormSessionAdapter()
        val bus = InMemoryCommandBus.builder().build()
        val config = FlexUiConfig(dispatcherProvider = TestDispatcherProvider(testDispatcher))
        val viewModel = givenViewModel(bus, sessionAdapter, config)

        viewModel.viewState.test {
            assertEquals(FlexUiViewState.Loading, awaitItem())

            viewModel.loadJson("{ malformed json }")
            testScheduler.advanceUntilIdle()

            val errorState = awaitItem()
            assertTrue(errorState is FlexUiViewState.Error)
            assertTrue((errorState as FlexUiViewState.Error).error is FlexUiError.InvalidJson)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onInputValueChanged dispatches UpdateInputValueCommand`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val sessionAdapter = InMemoryFormSessionAdapter()
        val session = givenSession()
        sessionAdapter.save(session)

        var dispatchedCommand: UpdateInputValueCommand? = null
        val startHandler = object : CommandHandler<StartFormSessionCommand, FormSession> {
            override suspend fun handle(command: StartFormSessionCommand): Either<DomainError, FormSession> {
                val s = givenSession(command.sessionId)
                sessionAdapter.save(s)
                return Either.Right(s)
            }
        }
        val updateHandler = object : CommandHandler<UpdateInputValueCommand, FormSession> {
            override suspend fun handle(command: UpdateInputValueCommand): Either<DomainError, FormSession> {
                dispatchedCommand = command
                return Either.Right(session)
            }
        }

        val bus = InMemoryCommandBus.builder()
            .register(StartFormSessionCommand::class, StartFormSessionCommand.COMMAND_NAME, startHandler)
            .register(UpdateInputValueCommand::class, UpdateInputValueCommand.COMMAND_NAME, updateHandler)
            .build()

        val config = FlexUiConfig(dispatcherProvider = TestDispatcherProvider(testDispatcher))
        val viewModel = givenViewModel(bus, sessionAdapter, config)

        viewModel.loadJson(sampleJson)
        testScheduler.advanceUntilIdle()

        val key = NodeKey(ComponentId.of("input_username").getOrNull()!!)
        val newValue = FieldValue.Text("alice")
        viewModel.onInputValueChanged(key, newValue)
        testScheduler.advanceUntilIdle()

        assertNotNull(dispatchedCommand)
        assertEquals("input_username", dispatchedCommand?.componentId)
        assertEquals(newValue, dispatchedCommand?.value)
    }

    @Test
    fun `onActionClicked dispatches action and emits ActionExecuted event`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val sessionAdapter = InMemoryFormSessionAdapter()

        val startHandler = object : CommandHandler<StartFormSessionCommand, FormSession> {
            override suspend fun handle(command: StartFormSessionCommand): Either<DomainError, FormSession> {
                val s = givenSession(command.sessionId)
                sessionAdapter.save(s)
                return Either.Right(s)
            }
        }
        val actionHandler = object : CommandHandler<ProcessUserActionCommand, ActionResult> {
            override suspend fun handle(command: ProcessUserActionCommand): Either<DomainError, ActionResult> {
                return Either.Right(ActionResult.Handled)
            }
        }

        val bus = InMemoryCommandBus.builder()
            .register(StartFormSessionCommand::class, StartFormSessionCommand.COMMAND_NAME, startHandler)
            .register(ProcessUserActionCommand::class, ProcessUserActionCommand.COMMAND_NAME, actionHandler)
            .build()

        val config = FlexUiConfig(dispatcherProvider = TestDispatcherProvider(testDispatcher))
        val viewModel = givenViewModel(bus, sessionAdapter, config)

        viewModel.loadJson(sampleJson)
        testScheduler.advanceUntilIdle()

        viewModel.events.test {
            val actionId = ActionId.of("submit_btn").getOrNull()!!
            val nodeKey = NodeKey(ComponentId.of("input_username").getOrNull()!!)
            viewModel.onActionClicked(actionId, nodeKey)
            testScheduler.advanceUntilIdle()

            val event = awaitItem()
            assertTrue(event is FlexUiEvent.ActionExecuted)
            assertEquals(ActionResult.Handled, (event as FlexUiEvent.ActionExecuted).result)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `action exceeding timeout emits ActionTimeout error`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val sessionAdapter = InMemoryFormSessionAdapter()

        val startHandler = object : CommandHandler<StartFormSessionCommand, FormSession> {
            override suspend fun handle(command: StartFormSessionCommand): Either<DomainError, FormSession> {
                val s = givenSession(command.sessionId)
                sessionAdapter.save(s)
                return Either.Right(s)
            }
        }
        val hangingHandler = object : CommandHandler<ProcessUserActionCommand, ActionResult> {
            override suspend fun handle(command: ProcessUserActionCommand): Either<DomainError, ActionResult> {
                delay(5_000L)
                return Either.Right(ActionResult.Handled)
            }
        }

        val bus = InMemoryCommandBus.builder()
            .register(StartFormSessionCommand::class, StartFormSessionCommand.COMMAND_NAME, startHandler)
            .register(ProcessUserActionCommand::class, ProcessUserActionCommand.COMMAND_NAME, hangingHandler)
            .build()

        val config = FlexUiConfig(
            dispatcherProvider = TestDispatcherProvider(testDispatcher),
            actionTimeoutMs = 1_000L // Timeout corto de 1 segundo
        )
        val viewModel = givenViewModel(bus, sessionAdapter, config)

        viewModel.loadJson(sampleJson)
        testScheduler.advanceUntilIdle()

        viewModel.viewState.test {
            val initial = awaitItem()
            assertTrue(initial is FlexUiViewState.Content)
            assertFalse((initial as FlexUiViewState.Content).state.isSubmitting)

            val actionId = ActionId.of("submit_btn").getOrNull()!!
            val nodeKey = NodeKey(ComponentId.of("input_username").getOrNull()!!)
            viewModel.onActionClicked(actionId, nodeKey)

            val submittingState = awaitItem()
            assertTrue((submittingState as FlexUiViewState.Content).state.isSubmitting)

            // Avanza hasta disparar el timeout
            testScheduler.advanceTimeBy(1_500L)
            testScheduler.runCurrent()

            val timeoutState = awaitItem()
            assertTrue(timeoutState is FlexUiViewState.Error)
            val error = (timeoutState as FlexUiViewState.Error).error
            assertTrue(error is FlexUiError.ActionTimeout)
            assertEquals("submit_btn", (error as FlexUiError.ActionTimeout).actionId)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `action failure due to domain error emits SessionError instead of ActionTimeout`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val sessionAdapter = InMemoryFormSessionAdapter()

        val startHandler = object : CommandHandler<StartFormSessionCommand, FormSession> {
            override suspend fun handle(command: StartFormSessionCommand): Either<DomainError, FormSession> {
                val s = givenSession(command.sessionId)
                sessionAdapter.save(s)
                return Either.Right(s)
            }
        }
        val failingActionHandler = object : CommandHandler<ProcessUserActionCommand, ActionResult> {
            override suspend fun handle(command: ProcessUserActionCommand): Either<DomainError, ActionResult> {
                return Either.Left(DomainError.Interaction.HostExecutionFailed("Host rejected action"))
            }
        }

        val bus = InMemoryCommandBus.builder()
            .register(StartFormSessionCommand::class, StartFormSessionCommand.COMMAND_NAME, startHandler)
            .register(ProcessUserActionCommand::class, ProcessUserActionCommand.COMMAND_NAME, failingActionHandler)
            .build()

        val config = FlexUiConfig(dispatcherProvider = TestDispatcherProvider(testDispatcher))
        val viewModel = givenViewModel(bus, sessionAdapter, config)

        viewModel.loadJson(sampleJson)
        testScheduler.advanceUntilIdle()

        viewModel.viewState.test {
            val initial = awaitItem()
            assertTrue(initial is FlexUiViewState.Content)

            val actionId = ActionId.of("submit_btn").getOrNull()!!
            val nodeKey = NodeKey(ComponentId.of("input_username").getOrNull()!!)
            viewModel.onActionClicked(actionId, nodeKey)

            val submittingState = awaitItem()
            assertTrue((submittingState as FlexUiViewState.Content).state.isSubmitting)

            testScheduler.advanceUntilIdle()

            val errorState = awaitItem()
            assertTrue(errorState is FlexUiViewState.Error)
            val error = (errorState as FlexUiViewState.Error).error
            assertTrue(error is FlexUiError.SessionError)
            assertFalse(error is FlexUiError.ActionTimeout)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `superseded loadJson cancels previous loading job`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val sessionAdapter = InMemoryFormSessionAdapter()
        val completedSessions = mutableListOf<String>()

        val startHandler = object : CommandHandler<StartFormSessionCommand, FormSession> {
            override suspend fun handle(command: StartFormSessionCommand): Either<DomainError, FormSession> {
                if (command.sessionId.contains("first")) {
                    delay(2_000L)
                }
                completedSessions.add(command.sessionId)
                val s = givenSession(command.sessionId)
                sessionAdapter.save(s)
                return Either.Right(s)
            }
        }

        val bus = InMemoryCommandBus.builder()
            .register(StartFormSessionCommand::class, StartFormSessionCommand.COMMAND_NAME, startHandler)
            .build()

        val config = FlexUiConfig(dispatcherProvider = TestDispatcherProvider(testDispatcher))
        val idFactory = object : SessionIdFactory {
            private var count = 0
            override fun generate(): SessionId =
                SessionId.of(if (count++ == 0) "session-first" else "session-second").getOrNull()!!
        }
        val viewModel = givenViewModel(bus, sessionAdapter, config, idFactory = idFactory)

        viewModel.loadJson(sampleJson)
        testScheduler.advanceTimeBy(100L) // Start first request but don't finish delay

        viewModel.loadJson(sampleJson.replace("Sample Title", "Second Title"))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf("session-second"), completedSessions)
    }

    @Test
    fun `hash reconciliation prevents re-parsing when jsonString is identical`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val sessionAdapter = InMemoryFormSessionAdapter()
        var startCount = 0

        val startHandler = object : CommandHandler<StartFormSessionCommand, FormSession> {
            override suspend fun handle(command: StartFormSessionCommand): Either<DomainError, FormSession> {
                startCount++
                val s = givenSession(command.sessionId)
                sessionAdapter.save(s)
                return Either.Right(s)
            }
        }

        val bus = InMemoryCommandBus.builder()
            .register(StartFormSessionCommand::class, StartFormSessionCommand.COMMAND_NAME, startHandler)
            .build()

        val config = FlexUiConfig(dispatcherProvider = TestDispatcherProvider(testDispatcher))
        val viewModel = givenViewModel(bus, sessionAdapter, config)

        viewModel.loadJson(sampleJson)
        testScheduler.advanceUntilIdle()
        assertEquals(1, startCount)

        // Segunda llamada con el mismo JSON exacto
        viewModel.loadJson(sampleJson)
        testScheduler.advanceUntilIdle()
        assertEquals(1, startCount) // No debe reiniciar la sesión
    }

    @Test
    fun `retry reloads the last requested json resetting the hash`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val sessionAdapter = InMemoryFormSessionAdapter()
        var startCount = 0

        val startHandler = object : CommandHandler<StartFormSessionCommand, FormSession> {
            override suspend fun handle(command: StartFormSessionCommand): Either<DomainError, FormSession> {
                startCount++
                val s = givenSession(command.sessionId)
                sessionAdapter.save(s)
                return Either.Right(s)
            }
        }

        val bus = InMemoryCommandBus.builder()
            .register(StartFormSessionCommand::class, StartFormSessionCommand.COMMAND_NAME, startHandler)
            .build()

        val config = FlexUiConfig(dispatcherProvider = TestDispatcherProvider(testDispatcher))
        val viewModel = givenViewModel(bus, sessionAdapter, config)

        viewModel.loadJson(sampleJson)
        testScheduler.advanceUntilIdle()
        assertEquals(1, startCount)

        viewModel.retry()
        testScheduler.advanceUntilIdle()
        assertEquals(2, startCount)
    }

    @Test
    fun `onActionClicked ignores second click while action is in flight`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val sessionAdapter = InMemoryFormSessionAdapter()

        val actionDeferred = CompletableDeferred<Unit>()
        var actionInvocationCount = 0

        val startHandler = object : CommandHandler<StartFormSessionCommand, FormSession> {
            override suspend fun handle(command: StartFormSessionCommand): Either<DomainError, FormSession> {
                val s = givenSession(command.sessionId)
                sessionAdapter.save(s)
                return Either.Right(s)
            }
        }
        val actionHandler = object : CommandHandler<ProcessUserActionCommand, ActionResult> {
            override suspend fun handle(command: ProcessUserActionCommand): Either<DomainError, ActionResult> {
                actionInvocationCount++
                actionDeferred.await()
                return Either.Right(ActionResult.Handled)
            }
        }

        val bus = InMemoryCommandBus.builder()
            .register(StartFormSessionCommand::class, StartFormSessionCommand.COMMAND_NAME, startHandler)
            .register(ProcessUserActionCommand::class, ProcessUserActionCommand.COMMAND_NAME, actionHandler)
            .build()

        val config = FlexUiConfig(dispatcherProvider = TestDispatcherProvider(testDispatcher))
        val viewModel = givenViewModel(bus, sessionAdapter, config)

        viewModel.loadJson(sampleJson)
        testScheduler.advanceUntilIdle()

        val actionId = ActionId.of("submit_btn").getOrNull()!!
        val nodeKey = NodeKey(ComponentId.of("input_username").getOrNull()!!)

        // Primer click (inicia acción)
        viewModel.onActionClicked(actionId, nodeKey)
        testScheduler.runCurrent()
        assertEquals(1, actionInvocationCount)

        // Segundo click inmediato mientras sigue en vuelo (debe ser ignorado)
        viewModel.onActionClicked(actionId, nodeKey)
        testScheduler.runCurrent()
        assertEquals(1, actionInvocationCount)

        // Liberar acción pendiente
        actionDeferred.complete(Unit)
        testScheduler.advanceUntilIdle()
        assertEquals(1, actionInvocationCount)
    }

    @Test
    fun `onCleared calls sessionPort delete with current sessionId`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val sessionAdapter = InMemoryFormSessionAdapter()
        var capturedSessionId: String? = null

        val startHandler = object : CommandHandler<StartFormSessionCommand, FormSession> {
            override suspend fun handle(command: StartFormSessionCommand): Either<DomainError, FormSession> {
                capturedSessionId = command.sessionId
                val s = givenSession(command.sessionId)
                sessionAdapter.save(s)
                return Either.Right(s)
            }
        }

        val bus = InMemoryCommandBus.builder()
            .register(StartFormSessionCommand::class, StartFormSessionCommand.COMMAND_NAME, startHandler)
            .build()

        val config = FlexUiConfig(dispatcherProvider = TestDispatcherProvider(testDispatcher))
        val viewModel = givenViewModel(bus, sessionAdapter, config)

        viewModel.loadJson(sampleJson)
        testScheduler.advanceUntilIdle()

        val sid = SessionId.of(capturedSessionId!!).getOrNull()!!
        val loadedSession = sessionAdapter.load(sid)
        assertNotNull(loadedSession)

        viewModel.clearForTesting()
        testScheduler.advanceUntilIdle()

        val deletedSession = sessionAdapter.load(sid)
        org.junit.Assert.assertNull(deletedSession)
    }

    @Test
    fun `FlexUiViewModelFactory creates functional viewModel with wired command bus`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val config = FlexUiConfig(dispatcherProvider = TestDispatcherProvider(testDispatcher))
        val factory = FlexUiViewModelFactory(config)

        val viewModel = factory.create(FlexUiViewModel::class.java)

        viewModel.viewState.test {
            assertEquals(FlexUiViewState.Loading, awaitItem())

            viewModel.loadJson(sampleJson)
            testScheduler.advanceUntilIdle()

            val content = awaitItem()
            assertTrue(content is FlexUiViewState.Content)
            assertEquals("input_username", (content as FlexUiViewState.Content).state.layoutId)

            cancelAndIgnoreRemainingEvents()
        }
    }
}

package com.libs.flex.ui.flexui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.libs.flex.ui.core.application.bus.InMemoryCommandBus
import com.libs.flex.ui.core.application.bus.middleware.SessionLockMiddleware
import com.libs.flex.ui.core.application.command.ProcessUserActionCommand
import com.libs.flex.ui.core.application.command.StartFormSessionCommand
import com.libs.flex.ui.core.application.command.UpdateInputValueCommand
import com.libs.flex.ui.core.application.handler.ProcessUserActionHandler
import com.libs.flex.ui.core.application.handler.StartFormSessionHandler
import com.libs.flex.ui.core.application.handler.UpdateInputValueHandler
import com.libs.flex.ui.core.application.usecase.ProcessUserActionUseCase
import com.libs.flex.ui.core.application.usecase.StartFormSessionUseCase
import com.libs.flex.ui.core.application.usecase.UpdateInputValueUseCase
import com.libs.flex.ui.core.domain.ports.ActionResult
import com.libs.flex.ui.flexui.FlexUiConfig
import com.libs.flex.ui.flexui.adapter.clock.SystemClockAdapter
import com.libs.flex.ui.flexui.adapter.host.SuspendHostActionAdapter
import com.libs.flex.ui.flexui.adapter.publisher.LogcatPublisherAdapter
import com.libs.flex.ui.flexui.adapter.publisher.NoOpPublisherAdapter
import com.libs.flex.ui.flexui.adapter.session.InMemoryFormSessionAdapter
import com.libs.flex.ui.flexui.adapter.source.DirectStringLayoutSourceAdapter
import com.libs.flex.ui.flexui.usecase.InitializeLayoutSessionUseCase
import com.libs.flex.ui.parser.LayoutParserFactory

/**
 * Factory creating decoupled [FlexUiViewModel] instances with standard dependencies,
 * pure Hexagonal ports, and in-memory command bus routing.
 *
 * Avoids any reflection or dynamic service locators, satisfying Clean Architecture constraints.
 */
class FlexUiViewModelFactory(
    private val config: FlexUiConfig = FlexUiConfig()
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(FlexUiViewModel::class.java)) {
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }

        val layoutParser = LayoutParserFactory.create()
        val layoutSource = DirectStringLayoutSourceAdapter(
            parser = layoutParser,
            options = config.parserOptions
        )
        val sessionPort = InMemoryFormSessionAdapter()
        val clock = SystemClockAdapter()
        val eventPublisher = if (config.isReleaseMode) {
            NoOpPublisherAdapter()
        } else {
            LogcatPublisherAdapter()
        }
        val hostActionPort = config.hostActionHandler?.let { SuspendHostActionAdapter(it) }
            ?: SuspendHostActionAdapter { ActionResult.Handled }

        val startFormSessionUseCase = StartFormSessionUseCase(
            layoutSource = layoutSource,
            sessionPort = sessionPort,
            eventPublisher = eventPublisher,
            clock = clock
        )
        val updateInputValueUseCase = UpdateInputValueUseCase(
            sessionPort = sessionPort,
            eventPublisher = eventPublisher,
            clock = clock
        )
        val processUserActionUseCase = ProcessUserActionUseCase(
            sessionPort = sessionPort,
            hostActionPort = hostActionPort,
            eventPublisher = eventPublisher,
            clock = clock
        )

        val commandBus = InMemoryCommandBus.builder()
            .register(
                StartFormSessionCommand::class,
                StartFormSessionCommand.COMMAND_NAME,
                StartFormSessionHandler(startFormSessionUseCase)
            )
            .register(
                UpdateInputValueCommand::class,
                UpdateInputValueCommand.COMMAND_NAME,
                UpdateInputValueHandler(updateInputValueUseCase)
            )
            .register(
                ProcessUserActionCommand::class,
                ProcessUserActionCommand.COMMAND_NAME,
                ProcessUserActionHandler(processUserActionUseCase)
            )
            .addMiddleware(SessionLockMiddleware())
            .build()

        val initializeLayoutSessionUseCase = InitializeLayoutSessionUseCase(
            layoutSource = layoutSource,
            commandBus = commandBus
        )

        return FlexUiViewModel(
            initializeLayoutSessionUseCase = initializeLayoutSessionUseCase,
            sessionPort = sessionPort,
            commandBus = commandBus,
            config = config
        ) as T
    }
}

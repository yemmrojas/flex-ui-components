package com.libs.flex.ui.flexui.usecase

import com.libs.flex.ui.core.application.bus.CommandBus
import com.libs.flex.ui.core.application.command.StartFormSessionCommand
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.model.node.UIComponentNode
import com.libs.flex.ui.core.domain.value.SessionId
import com.libs.flex.ui.flexui.adapter.source.StringLayoutSourcePort
import com.libs.flex.ui.flexui.factory.DefaultSessionIdFactory
import com.libs.flex.ui.flexui.factory.SessionIdFactory
import com.libs.flex.ui.flexui.model.FlexUiError

/**
 * Result data holder representing a successfully initialized layout session.
 */
data class InitializedLayoutSession(
    val sessionId: SessionId,
    val layoutId: String,
    val rootNode: UIComponentNode
)

/**
 * Presentation Use Case orchestrating the initialization of a layout session.
 *
 * Responsibilities are strictly limited to coordinating:
 * 1. Loading the layout document via [StringLayoutSourcePort].
 * 2. Generating the session identifier via [SessionIdFactory].
 * 3. Dispatching [StartFormSessionCommand] to the application core.
 */
class InitializeLayoutSessionUseCase(
    private val layoutSource: StringLayoutSourcePort,
    private val commandBus: CommandBus,
    private val idFactory: SessionIdFactory = DefaultSessionIdFactory()
) {

    suspend operator fun invoke(jsonString: String): Either<FlexUiError, InitializedLayoutSession> {
        val document = when (val res = layoutSource.load(jsonString)) {
            is Either.Left -> return res
            is Either.Right -> res.value
        }

        val sessionId = idFactory.generate()
        val layoutId = document.root.id.value

        val command = StartFormSessionCommand(sessionId = sessionId.value, layoutId = layoutId)
        return when (val outcome = commandBus.dispatch(command)) {
            is Either.Left -> {
                Either.Left(
                    FlexUiError.SessionError(
                        userMessage = "Could not initialize form session",
                        technicalCause = outcome.value::class.simpleName
                    )
                )
            }
            is Either.Right -> {
                Either.Right(
                    InitializedLayoutSession(
                        sessionId = sessionId,
                        layoutId = layoutId,
                        rootNode = document.root
                    )
                )
            }
        }
    }
}

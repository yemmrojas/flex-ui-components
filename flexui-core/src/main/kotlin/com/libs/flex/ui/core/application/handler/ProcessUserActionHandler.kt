package com.libs.flex.ui.core.application.handler

import com.libs.flex.ui.core.application.bus.CommandHandler
import com.libs.flex.ui.core.application.command.ProcessUserActionCommand
import com.libs.flex.ui.core.application.usecase.ProcessUserActionUseCase
import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.ports.ActionResult
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.NodeKey
import com.libs.flex.ui.core.domain.value.SessionId

public class ProcessUserActionHandler(
    private val useCase: ProcessUserActionUseCase
) : CommandHandler<ProcessUserActionCommand, ActionResult> {
    override suspend fun handle(command: ProcessUserActionCommand): Either<DomainError, ActionResult> {
        val sessionId = when (val res = SessionId.of(command.sessionId)) {
            is Either.Left -> return res
            is Either.Right -> res.value
        }
        val compId = when (val res = ComponentId.of(command.componentId)) {
            is Either.Left -> return res
            is Either.Right -> res.value
        }
        val key = NodeKey(compId, command.instanceIndex)

        return useCase.execute(sessionId, key)
    }
}

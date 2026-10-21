package com.libs.flex.ui.core.application.handler

import com.libs.flex.ui.core.application.bus.CommandHandler
import com.libs.flex.ui.core.application.command.UpdateInputValueCommand
import com.libs.flex.ui.core.application.usecase.UpdateInputValueUseCase
import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.model.FormSession
import com.libs.flex.ui.core.domain.value.ComponentId
import com.libs.flex.ui.core.domain.value.FieldValue
import com.libs.flex.ui.core.domain.value.NodeKey
import com.libs.flex.ui.core.domain.value.SessionId

public class UpdateInputValueHandler(
    private val useCase: UpdateInputValueUseCase
) : CommandHandler<UpdateInputValueCommand, FormSession> {
    override suspend fun handle(command: UpdateInputValueCommand): Either<DomainError, FormSession> {
        val sessionId = when (val res = SessionId.of(command.sessionId)) {
            is Either.Left -> return res
            is Either.Right -> res.value
        }
        val compId = when (val res = ComponentId.of(command.componentId)) {
            is Either.Left -> return res
            is Either.Right -> res.value
        }
        val key = NodeKey(compId, command.instanceIndex)

        return useCase.execute(sessionId, key, command.value)
    }
}

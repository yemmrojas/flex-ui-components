package com.libs.flex.ui.core.application.handler

import com.libs.flex.ui.core.application.bus.CommandHandler
import com.libs.flex.ui.core.application.command.StartFormSessionCommand
import com.libs.flex.ui.core.application.usecase.StartFormSessionUseCase
import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.model.FormSession
import com.libs.flex.ui.core.domain.value.LayoutId
import com.libs.flex.ui.core.domain.value.SessionId

public class StartFormSessionHandler(
    private val useCase: StartFormSessionUseCase
) : CommandHandler<StartFormSessionCommand, FormSession> {
    override suspend fun handle(command: StartFormSessionCommand): Either<DomainError, FormSession> {
        val sessionId = when (val res = SessionId.of(command.sessionId)) {
            is Either.Left -> return res
            is Either.Right -> res.value
        }
        val layoutId = when (val res = LayoutId.of(command.layoutId)) {
            is Either.Left -> return res
            is Either.Right -> res.value
        }
        return useCase.execute(sessionId, layoutId)
    }
}

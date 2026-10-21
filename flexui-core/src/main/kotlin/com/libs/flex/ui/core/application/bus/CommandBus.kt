package com.libs.flex.ui.core.application.bus

import com.libs.flex.ui.core.application.command.Command
import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either

public interface CommandBus {
    public suspend fun <R> dispatch(command: Command<R>): Either<DomainError, R>
}

public typealias CommandMiddleware = suspend (Command<*>, suspend (Command<*>) -> Either<DomainError, Any?>) -> Either<DomainError, Any?>

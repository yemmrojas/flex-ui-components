package com.libs.flex.ui.core.application.bus

import com.libs.flex.ui.core.application.command.Command
import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either

public interface CommandHandler<C : Command<R>, R> {
    public suspend fun handle(command: C): Either<DomainError, R>
}

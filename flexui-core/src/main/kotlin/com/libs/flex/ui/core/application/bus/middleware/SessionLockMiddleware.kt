package com.libs.flex.ui.core.application.bus.middleware

import com.libs.flex.ui.core.application.bus.CommandMiddleware
import com.libs.flex.ui.core.application.command.Command
import com.libs.flex.ui.core.application.command.SessionCommand
import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

public class SessionLockMiddleware : CommandMiddleware {
    private val mutexes = ConcurrentHashMap<String, Mutex>()

    override suspend fun intercept(
        command: Command<*>,
        next: suspend (Command<*>) -> Either<DomainError, Any?>
    ): Either<DomainError, Any?> {
        return if (command is SessionCommand) {
            val mutex = mutexes.computeIfAbsent(command.sessionId) { Mutex() }
            mutex.withLock {
                next(command)
            }
        } else {
            next(command)
        }
    }
}

public typealias SessionSerializationMiddleware = SessionLockMiddleware

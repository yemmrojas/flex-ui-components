package com.libs.flex.ui.core.application.bus

import com.libs.flex.ui.core.application.command.Command
import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either
import kotlin.reflect.KClass

public class InMemoryCommandBus(
    private val registry: Map<KClass<out Command<*>>, CommandHandler<*, *>>,
    private val middlewares: List<CommandMiddleware> = emptyList()
) : CommandBus {

    override suspend fun <R> dispatch(command: Command<R>): Either<DomainError, R> {
        val baseExecution: suspend (Command<*>) -> Either<DomainError, Any?> = { cmd ->
            @Suppress("UNCHECKED_CAST")
            val handler = registry[cmd::class] as? CommandHandler<Command<Any?>, Any?>
            if (handler == null) {
                Either.Left(
                    DomainError.Interaction.HostExecutionFailed(
                        "No handler registered for command: ${cmd.commandName}"
                    )
                )
            } else {
                @Suppress("UNCHECKED_CAST")
                handler.handle(cmd as Command<Any?>)
            }
        }

        val pipeline = middlewares.foldRight(baseExecution) { middleware, next ->
            { cmd -> middleware.intercept(cmd, next) }
        }

        val result = pipeline(command)
        @Suppress("UNCHECKED_CAST")
        return result as Either<DomainError, R>
    }

    public class Builder {
        private val handlers = mutableMapOf<KClass<out Command<*>>, CommandHandler<*, *>>()
        private val commandNames = mutableMapOf<String, KClass<out Command<*>>>()
        private val middlewares = mutableListOf<CommandMiddleware>()

        public fun <C : Command<R>, R> register(
            commandClass: KClass<C>,
            commandName: String,
            handler: CommandHandler<C, R>
        ): Builder {
            if (handlers.containsKey(commandClass)) {
                throw IllegalStateException("Duplicate handler for command class: ${commandClass.simpleName}")
            }
            if (commandNames.containsKey(commandName)) {
                throw IllegalStateException("Duplicate commandName: '$commandName' already registered by ${commandNames[commandName]?.simpleName}")
            }
            handlers[commandClass] = handler
            commandNames[commandName] = commandClass
            return this
        }

        public fun addMiddleware(middleware: CommandMiddleware): Builder {
            middlewares.add(middleware)
            return this
        }

        public fun build(): InMemoryCommandBus {
            return InMemoryCommandBus(handlers.toMap(), middlewares.toList())
        }
    }

    public companion object {
        public fun builder(): Builder = Builder()
    }
}

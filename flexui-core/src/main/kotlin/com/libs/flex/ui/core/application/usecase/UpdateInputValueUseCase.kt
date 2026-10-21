package com.libs.flex.ui.core.application.usecase

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.model.FormSession
import com.libs.flex.ui.core.domain.ports.ClockPort
import com.libs.flex.ui.core.domain.ports.DomainEventPublisherPort
import com.libs.flex.ui.core.domain.ports.FormSessionPort
import com.libs.flex.ui.core.domain.ports.publishAllSafely
import com.libs.flex.ui.core.domain.value.FieldValue
import com.libs.flex.ui.core.domain.value.NodeKey
import com.libs.flex.ui.core.domain.value.SessionId

public class UpdateInputValueUseCase(
    private val sessionPort: FormSessionPort,
    private val eventPublisher: DomainEventPublisherPort,
    private val clock: ClockPort
) {
    public suspend fun execute(
        sessionId: SessionId,
        key: NodeKey,
        value: FieldValue
    ): Either<DomainError, FormSession> {
        val session = sessionPort.load(sessionId)
            ?: return Either.Left(DomainError.Interaction.SessionNotFound(sessionId))

        val outcome = when (val res = session.updateInput(key, value, clock.currentTimeMillis())) {
            is Either.Left -> return res
            is Either.Right -> res.value
        }

        sessionPort.save(outcome.session)
        eventPublisher.publishAllSafely(outcome.events)

        return Either.Right(outcome.session)
    }
}

package com.libs.flex.ui.core.application.usecase

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.model.FormSession
import com.libs.flex.ui.core.domain.ports.ClockPort
import com.libs.flex.ui.core.domain.ports.DomainEventPublisherPort
import com.libs.flex.ui.core.domain.ports.FormSessionPort
import com.libs.flex.ui.core.domain.ports.LayoutRequest
import com.libs.flex.ui.core.domain.ports.LayoutSourcePort
import com.libs.flex.ui.core.domain.ports.publishAllSafely
import com.libs.flex.ui.core.domain.value.LayoutId
import com.libs.flex.ui.core.domain.value.SessionId

public class StartFormSessionUseCase(
    private val layoutSource: LayoutSourcePort,
    private val sessionPort: FormSessionPort,
    private val eventPublisher: DomainEventPublisherPort,
    private val clock: ClockPort
) {
    public suspend fun execute(
        sessionId: SessionId,
        layoutId: LayoutId
    ): Either<DomainError, FormSession> {
        val layoutDoc = when (val res = layoutSource.getLayout(LayoutRequest(layoutId))) {
            is Either.Left -> return Either.Left(res.value.head)
            is Either.Right -> res.value
        }

        val outcome = FormSession.start(sessionId, layoutDoc, clock)
        sessionPort.save(outcome.session)
        eventPublisher.publishAllSafely(outcome.events)

        return Either.Right(outcome.session)
    }
}

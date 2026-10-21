package com.libs.flex.ui.core.application.usecase

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.model.ActionDecision
import com.libs.flex.ui.core.domain.ports.ActionResult
import com.libs.flex.ui.core.domain.ports.ClockPort
import com.libs.flex.ui.core.domain.ports.DomainEventPublisherPort
import com.libs.flex.ui.core.domain.ports.FormSessionPort
import com.libs.flex.ui.core.domain.ports.HostActionHandlerPort
import com.libs.flex.ui.core.domain.ports.publishAllSafely
import com.libs.flex.ui.core.domain.value.NodeKey
import com.libs.flex.ui.core.domain.value.SessionId

public class ProcessUserActionUseCase(
    private val sessionPort: FormSessionPort,
    private val hostActionPort: HostActionHandlerPort,
    private val eventPublisher: DomainEventPublisherPort,
    private val clock: ClockPort
) {
    public suspend fun execute(
        sessionId: SessionId,
        key: NodeKey
    ): Either<DomainError, ActionResult> {
        val session = sessionPort.load(sessionId)
            ?: return Either.Left(DomainError.Interaction.SessionNotFound(sessionId))
        val decision = when (val res = session.requestAction(key, clock.currentTimeMillis())) {
            is Either.Left -> return res
            is Either.Right -> res.value
        }

        return when (decision) {
            is ActionDecision.Blocked -> {
                sessionPort.save(decision.outcome.session)
                eventPublisher.publishAllSafely(decision.outcome.events)
                Either.Right(ActionResult.Blocked(decision.outcome.session.formErrors))
            }
            is ActionDecision.Dispatch -> {
                sessionPort.save(decision.outcome.session)
                eventPublisher.publishAllSafely(decision.outcome.events)

                val hostResult = hostActionPort.handleAction(decision.action)

                val resolutionOutcome = decision.outcome.session.recordActionResult(
                    actionId = decision.action.actionId,
                    sourceKey = key,
                    result = hostResult,
                    occurredAt = clock.currentTimeMillis()
                )
                sessionPort.save(resolutionOutcome.session)
                eventPublisher.publishAllSafely(resolutionOutcome.events)

                Either.Right(hostResult)
            }
        }
    }
}

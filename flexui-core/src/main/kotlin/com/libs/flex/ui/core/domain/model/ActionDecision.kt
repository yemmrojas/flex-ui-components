package com.libs.flex.ui.core.domain.model

import com.libs.flex.ui.core.domain.ports.UserAction

public sealed interface ActionDecision {
    public data class Dispatch(
        public val action: UserAction,
        public val outcome: FormOutcome
    ) : ActionDecision

    public data class Blocked(
        public val outcome: FormOutcome
    ) : ActionDecision
}

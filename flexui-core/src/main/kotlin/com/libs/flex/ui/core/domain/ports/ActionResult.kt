package com.libs.flex.ui.core.domain.ports

import com.libs.flex.ui.core.domain.validation.ValidationError
import com.libs.flex.ui.core.domain.value.NodeKey

public sealed interface ActionResult {
    public data object Handled : ActionResult
    public data class Rejected(public val reason: String) : ActionResult
    public data object Unhandled : ActionResult
    public data class Blocked(public val errors: Map<NodeKey, ValidationError>) : ActionResult
}

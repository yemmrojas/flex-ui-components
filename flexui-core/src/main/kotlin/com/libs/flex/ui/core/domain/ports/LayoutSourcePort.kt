package com.libs.flex.ui.core.domain.ports

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.model.LayoutDocument
import com.libs.flex.ui.core.domain.value.LayoutId

public data class LayoutRequest(public val layoutId: LayoutId)

public interface LayoutSourcePort {
    public suspend fun getLayout(request: LayoutRequest): Either<Nel<DomainError.Structural>, LayoutDocument>
}

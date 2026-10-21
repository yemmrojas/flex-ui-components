package com.libs.flex.ui.core.fakes

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.functional.Nel
import com.libs.flex.ui.core.domain.model.LayoutDocument
import com.libs.flex.ui.core.domain.ports.LayoutRequest
import com.libs.flex.ui.core.domain.ports.LayoutSourcePort
import com.libs.flex.ui.core.domain.value.LayoutId

class FakeLayoutSource(
    private val defaultError: Nel<DomainError.Structural> = Nel(
        DomainError.Structural.UnsupportedSchemaVersion("unknown", "1.0.0", "1.0.0")
    )
) : LayoutSourcePort {
    private val layouts: MutableMap<LayoutId, Either<Nel<DomainError.Structural>, LayoutDocument>> = mutableMapOf()

    fun register(layoutId: LayoutId, document: LayoutDocument) {
        layouts[layoutId] = Either.Right(document)
    }

    fun registerError(layoutId: LayoutId, errors: Nel<DomainError.Structural>) {
        layouts[layoutId] = Either.Left(errors)
    }

    override suspend fun getLayout(request: LayoutRequest): Either<Nel<DomainError.Structural>, LayoutDocument> {
        return layouts[request.layoutId] ?: Either.Left(defaultError)
    }

    fun clear() {
        layouts.clear()
    }
}

package com.libs.flex.ui.core.domain.value

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either

@JvmInline
public value class SessionId private constructor(public val value: String) {
    public companion object {
        public fun of(raw: String): Either<DomainError.Value.InvalidIdentifier, SessionId> {
            val trimmed = raw.trim()
            return if (trimmed.isNotEmpty()) {
                Either.Right(SessionId(trimmed))
            } else {
                Either.Left(DomainError.Value.InvalidIdentifier(raw))
            }
        }
    }
}

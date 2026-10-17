package com.libs.flex.ui.core.domain.value

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either

@JvmInline
public value class ComponentId private constructor(public val value: String) {
    public companion object {
        private val REGEX = Regex("^[a-zA-Z0-9_-]{1,64}$")

        public fun of(raw: String): Either<DomainError.Value.InvalidIdentifier, ComponentId> {
            val trimmed = raw.trim()
            return if (trimmed.isNotEmpty() && REGEX.matches(trimmed)) {
                Either.Right(ComponentId(trimmed))
            } else {
                Either.Left(DomainError.Value.InvalidIdentifier(raw))
            }
        }
    }
}

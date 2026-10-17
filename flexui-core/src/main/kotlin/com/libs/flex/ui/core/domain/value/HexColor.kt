package com.libs.flex.ui.core.domain.value

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either

@JvmInline
public value class HexColor private constructor(public val value: String) {
    public companion object {
        private val REGEX = Regex("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{8}|[A-Fa-f0-9]{3})$")

        public fun of(raw: String): Either<DomainError.Value.InvalidColorFormat, HexColor> =
            if (REGEX.matches(raw)) Either.Right(HexColor(raw))
            else Either.Left(DomainError.Value.InvalidColorFormat(raw))
    }
}

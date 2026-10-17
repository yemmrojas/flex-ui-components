package com.libs.flex.ui.core.domain.value

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either

public sealed interface FontSize {
    public class Sp private constructor(public val value: Float) : FontSize {
        override fun equals(other: Any?): Boolean = other is Sp && value == other.value
        override fun hashCode(): Int = value.hashCode()
        override fun toString(): String = "Sp($value)"

        public companion object {
            public fun of(value: Float): Either<DomainError.Value.InvalidDimension, Sp> {
                return if (!value.isFinite() || value <= 0f) {
                    Either.Left(DomainError.Value.InvalidDimension("FontSize must be finite and positive, but was: $value"))
                } else {
                    Either.Right(Sp(value))
                }
            }
        }
    }
}

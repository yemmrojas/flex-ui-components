package com.libs.flex.ui.core.domain.value

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either

public sealed interface Dimension {

    public class Dp private constructor(public val value: Float) : Dimension {
        override fun equals(other: Any?): Boolean = other is Dp && value == other.value
        override fun hashCode(): Int = value.hashCode()
        override fun toString(): String = "Dp($value)"

        public companion object {
            public fun of(value: Float): Either<DomainError.Value.InvalidDimension, Dp> {
                return if (!value.isFinite() || value < 0f) {
                    Either.Left(DomainError.Value.InvalidDimension("Dp must be finite and non-negative, but was: $value"))
                } else {
                    Either.Right(Dp(value))
                }
            }
        }
    }

    public object MatchParent : Dimension
    public object WrapContent : Dimension
}

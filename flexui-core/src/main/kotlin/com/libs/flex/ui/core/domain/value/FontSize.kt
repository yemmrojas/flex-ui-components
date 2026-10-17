package com.libs.flex.ui.core.domain.value

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either

public class FontSize private constructor(public val value: Float) {
    override fun equals(other: Any?): Boolean = other is FontSize && value == other.value
    override fun hashCode(): Int = value.hashCode()
    override fun toString(): String = "FontSize(${value}sp)"

    public companion object {
        public fun of(value: Float): Either<DomainError.Value.InvalidDimension, FontSize> {
            return if (!value.isFinite() || value <= 0f) {
                Either.Left(DomainError.Value.InvalidDimension("FontSize must be finite and positive, but was: $value"))
            } else {
                Either.Right(FontSize(value))
            }
        }
    }
}

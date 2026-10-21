package com.libs.flex.ui.core.domain.value

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either

public class NumberRange private constructor(
    public val min: Double,
    public val max: Double,
    public val step: Double
) {
    override fun equals(other: Any?): Boolean =
        other is NumberRange && min == other.min && max == other.max && step == other.step

    override fun hashCode(): Int = 31 * (31 * min.hashCode() + max.hashCode()) + step.hashCode()

    override fun toString(): String = "NumberRange(min=$min, max=$max, step=$step)"

    public companion object {
        public fun of(min: Double, max: Double, step: Double): Either<DomainError.Value.InvalidNumberRange, NumberRange> {
            if (!min.isFinite() || !max.isFinite() || !step.isFinite()) {
                return Either.Left(DomainError.Value.InvalidNumberRange("Values must be finite (not NaN/Infinity)"))
            }
            if (min >= max) {
                return Either.Left(DomainError.Value.InvalidNumberRange("min ($min) must be strictly less than max ($max)"))
            }
            if (step <= 0.0 || step > (max - min)) {
                return Either.Left(DomainError.Value.InvalidNumberRange("step ($step) must be positive and <= (max - min)"))
            }
            return Either.Right(NumberRange(min, max, step))
        }
    }
}

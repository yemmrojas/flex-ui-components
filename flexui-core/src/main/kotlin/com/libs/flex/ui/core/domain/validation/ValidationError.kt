package com.libs.flex.ui.core.domain.validation

import com.libs.flex.ui.core.domain.value.NumberRange

public sealed interface ValidationError {
    public val message: String

    public data class Required(
        override val message: String = "Field is required"
    ) : ValidationError

    public data class MinLength(
        public val min: Int,
        public val actual: Int,
        override val message: String = "Length must be at least $min, but was $actual"
    ) : ValidationError

    public data class MaxLength(
        public val max: Int,
        public val actual: Int,
        override val message: String = "Length must be at most $max, but was $actual"
    ) : ValidationError

    public data class PatternMismatch(
        public val pattern: String,
        override val message: String = "Value does not match required pattern: $pattern"
    ) : ValidationError

    public data class NumberOutOfRange(
        public val range: NumberRange,
        public val actual: Double,
        override val message: String = "Number must be between ${range.min} and ${range.max}, but was $actual"
    ) : ValidationError

    public data class InvalidType(
        public val expected: String,
        override val message: String = "Expected $expected value"
    ) : ValidationError
}

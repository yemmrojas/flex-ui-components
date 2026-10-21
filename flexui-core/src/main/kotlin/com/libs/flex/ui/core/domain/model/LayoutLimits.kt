package com.libs.flex.ui.core.domain.model

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either

public class LayoutLimits private constructor(
    public val maxDepth: Int,
    public val maxNodes: Int,
    public val maxErrors: Int
) {
    override fun equals(other: Any?): Boolean =
        other is LayoutLimits &&
            maxDepth == other.maxDepth &&
            maxNodes == other.maxNodes &&
            maxErrors == other.maxErrors

    override fun hashCode(): Int =
        31 * (31 * maxDepth.hashCode() + maxNodes.hashCode()) + maxErrors.hashCode()

    override fun toString(): String =
        "LayoutLimits(maxDepth=$maxDepth, maxNodes=$maxNodes, maxErrors=$maxErrors)"

    public companion object {
        public const val DEFAULT_MAX_DEPTH: Int = 20
        public const val DEFAULT_MAX_NODES: Int = 500
        public const val DEFAULT_MAX_ERRORS: Int = 50

        public val DEFAULT: LayoutLimits =
            LayoutLimits(
                DEFAULT_MAX_DEPTH,
                DEFAULT_MAX_NODES,
                DEFAULT_MAX_ERRORS
            )

        public fun of(
            maxDepth: Int = DEFAULT_MAX_DEPTH,
            maxNodes: Int = DEFAULT_MAX_NODES,
            maxErrors: Int = DEFAULT_MAX_ERRORS
        ): Either<DomainError.Value.InvalidLayoutLimit, LayoutLimits> {
            if (maxDepth <= 0) {
                return Either.Left(DomainError.Value.InvalidLayoutLimit("maxDepth must be > 0, was: $maxDepth"))
            }
            if (maxNodes <= 0) {
                return Either.Left(DomainError.Value.InvalidLayoutLimit("maxNodes must be > 0, was: $maxNodes"))
            }
            if (maxErrors <= 0) {
                return Either.Left(DomainError.Value.InvalidLayoutLimit("maxErrors must be > 0, was: $maxErrors"))
            }
            return Either.Right(LayoutLimits(maxDepth, maxNodes, maxErrors))
        }
    }
}

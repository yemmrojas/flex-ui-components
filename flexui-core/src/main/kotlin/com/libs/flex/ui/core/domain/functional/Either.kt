package com.libs.flex.ui.core.domain.functional

public sealed class Either<out L, out R> {
    public data class Left<out L>(public val value: L) : Either<L, Nothing>()
    public data class Right<out R>(public val value: R) : Either<Nothing, R>()

    public val isRight: Boolean get() = this is Right
    public val isLeft: Boolean get() = this is Left

    public fun getOrNull(): R? = when (this) {
        is Right -> value
        is Left -> null
    }

    public fun leftOrNull(): L? = when (this) {
        is Left -> value
        is Right -> null
    }

    public inline fun <T> fold(onLeft: (L) -> T, onRight: (R) -> T): T = when (this) {
        is Left -> onLeft(value)
        is Right -> onRight(value)
    }

    public inline fun <T> map(transform: (R) -> T): Either<L, T> = when (this) {
        is Left -> this
        is Right -> Right(transform(value))
    }

    public inline fun <T> mapLeft(transform: (L) -> T): Either<T, R> = when (this) {
        is Left -> Left(transform(value))
        is Right -> this
    }

    public inline fun <T> flatMap(transform: (R) -> Either<@UnsafeVariance L, T>): Either<L, T> = when (this) {
        is Left -> this
        is Right -> transform(value)
    }

    public companion object {
        public fun <R> right(value: R): Either<Nothing, R> = Right(value)
        public fun <L> left(value: L): Either<L, Nothing> = Left(value)
    }
}

public fun <E, A, B, C> Either<Nel<E>, A>.zip(
    other: Either<Nel<E>, B>,
    combine: (A, B) -> C
): Either<Nel<E>, C> = when (this) {
    is Either.Left -> when (other) {
        is Either.Left -> Either.Left(this.value + other.value)
        is Either.Right -> this
    }

    is Either.Right -> when (other) {
        is Either.Left -> other
        is Either.Right -> Either.Right(combine(this.value, other.value))
    }
}

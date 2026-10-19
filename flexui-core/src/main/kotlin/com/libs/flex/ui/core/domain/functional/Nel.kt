package com.libs.flex.ui.core.domain.functional

public data class Nel<out E>(
    public val head: E,
    public val tail: List<E> = emptyList()
) {
    public val size: Int get() = 1 + tail.size

    public fun toList(): List<E> = ArrayList<E>(size).apply {
        add(head)
        addAll(tail)
    }

    public operator fun plus(other: Nel<@UnsafeVariance E>): Nel<E> {
        return Nel(head, tail + other.toList())
    }

    public inline fun <R> map(transform: (E) -> R): Nel<R> {
        return Nel(transform(head), tail.map(transform))
    }

    public companion object {
        public fun <E> of(head: E, vararg tail: E): Nel<E> = Nel(head, tail.toList())

        public fun <E> fromListOrNull(list: List<E>): Nel<E>? =
            if (list.isEmpty()) null
            else Nel(list.first(), list.drop(1))
    }
}

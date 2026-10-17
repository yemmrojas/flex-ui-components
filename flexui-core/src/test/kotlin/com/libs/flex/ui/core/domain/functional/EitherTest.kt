package com.libs.flex.ui.core.domain.functional

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EitherTest {

    @Test
    fun `Right fold should execute right lambda`() {
        val either: Either<String, Int> = Either.Right(42)
        val result = either.fold(
            onLeft = { "Error: $it" },
            onRight = { "Success: $it" }
        )
        assertEquals("Success: 42", result)
    }

    @Test
    fun `Left fold should execute left lambda`() {
        val either: Either<String, Int> = Either.Left("Boom")
        val result = either.fold(
            onLeft = { "Error: $it" },
            onRight = { "Success: $it" }
        )
        assertEquals("Error: Boom", result)
    }

    @Test
    fun `map transforms Right and ignores Left`() {
        val right: Either<String, Int> = Either.Right(10)
        val left: Either<String, Int> = Either.Left("fail")

        val mappedRight = right.map { it * 2 }
        val mappedLeft = left.map { it * 2 }

        assertTrue(mappedRight.isRight)
        assertEquals(20, (mappedRight as Either.Right).value)

        assertTrue(mappedLeft.isLeft)
        assertEquals("fail", (mappedLeft as Either.Left).value)
    }

    @Test
    fun `flatMap chains Rights and stops on Left`() {
        val right: Either<String, Int> = Either.Right(10)

        val chainedRight = right.flatMap { Either.Right(it + 5) }
        val chainedLeft = right.flatMap { Either.Left("stopped") }

        assertEquals(15, (chainedRight as Either.Right).value)
        assertEquals("stopped", (chainedLeft as Either.Left).value)
    }

    @Test
    fun `mapLeft transforms Left value and keeps Right intact`() {
        val left: Either<Int, String> = Either.Left(404)
        val right: Either<Int, String> = Either.Right("OK")

        val mappedLeft = left.mapLeft { "Code: $it" }
        val mappedRight = right.mapLeft { "Code: $it" }

        assertEquals("Code: 404", (mappedLeft as Either.Left).value)
        assertEquals("OK", (mappedRight as Either.Right).value)
    }

    @Test
    fun `zip accumulates errors using Nel combiner when both are Left`() {
        val left1: Either<Nel<String>, Int> = Either.Left(Nel.of("Error 1"))
        val left2: Either<Nel<String>, String> = Either.Left(Nel.of("Error 2"))

        val zipped = left1.zip(left2, { a, b -> "$a-$b" })

        assertTrue(zipped.isLeft)
        val errors = (zipped as Either.Left).value
        assertEquals(listOf("Error 1", "Error 2"), errors.toList())
    }

    @Test
    fun `zip combines results when both are Right`() {
        val right1: Either<Nel<String>, Int> = Either.Right(10)
        val right2: Either<Nel<String>, String> = Either.Right("items")

        val zipped = right1.zip(right2, { a, b -> "$a $b" })

        assertTrue(zipped.isRight)
        assertEquals("10 items", (zipped as Either.Right).value)
    }

    @Test
    fun `zip returns Left when only one is Left`() {
        val right: Either<Nel<String>, Int> = Either.Right(10)
        val left: Either<Nel<String>, String> = Either.Left(Nel.of("Single Error"))

        val zipped1 = right.zip(left, { a, b -> "$a $b" })
        val zipped2 = left.zip(right, { a, b -> "$a $b" })

        assertEquals(listOf("Single Error"), (zipped1 as Either.Left).value.toList())
        assertEquals(listOf("Single Error"), (zipped2 as Either.Left).value.toList())
    }
}

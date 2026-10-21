package com.libs.flex.ui.core.domain.model

import com.libs.flex.ui.core.domain.error.DomainError
import com.libs.flex.ui.core.domain.functional.Either
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

public class LayoutLimitsTest {

    @Test
    public fun `DEFAULT contains canonical limit constants`() {
        val defaultLimits = LayoutLimits.DEFAULT
        assertEquals(20, defaultLimits.maxDepth)
        assertEquals(500, defaultLimits.maxNodes)
        assertEquals(50, defaultLimits.maxErrors)
    }

    @Test
    public fun `of creates valid LayoutLimits with custom parameters`() {
        val result = LayoutLimits.of(maxDepth = 15, maxNodes = 100, maxErrors = 10)
        assertTrue(result.isRight)
        val limits = (result as Either.Right).value
        assertEquals(15, limits.maxDepth)
        assertEquals(100, limits.maxNodes)
        assertEquals(10, limits.maxErrors)
    }

    @Test
    public fun `of rejects maxDepth less than or equal to zero`() {
        val zero = LayoutLimits.of(maxDepth = 0)
        assertTrue(zero.isLeft)
        val errorZero = (zero as Either.Left).value
        assertTrue(errorZero.reason.contains("maxDepth"))

        val negative = LayoutLimits.of(maxDepth = -5)
        assertTrue(negative.isLeft)
    }

    @Test
    public fun `of rejects maxNodes less than or equal to zero`() {
        val zero = LayoutLimits.of(maxNodes = 0)
        assertTrue(zero.isLeft)
        val errorZero = (zero as Either.Left).value
        assertTrue(errorZero.reason.contains("maxNodes"))

        val negative = LayoutLimits.of(maxNodes = -1)
        assertTrue(negative.isLeft)
    }

    @Test
    public fun `of rejects maxErrors less than or equal to zero`() {
        val zero = LayoutLimits.of(maxErrors = 0)
        assertTrue(zero.isLeft)
        val errorZero = (zero as Either.Left).value
        assertTrue(errorZero.reason.contains("maxErrors"))

        val negative = LayoutLimits.of(maxErrors = -10)
        assertTrue(negative.isLeft)
    }

    @Test
    public fun `equals hashCode and toString behave consistently`() {
        val limitsA = LayoutLimits.of(10, 200, 30).getOrNull()!!
        val limitsB = LayoutLimits.of(10, 200, 30).getOrNull()!!
        val limitsC = LayoutLimits.of(12, 200, 30).getOrNull()!!

        assertEquals(limitsA, limitsB)
        assertEquals(limitsA.hashCode(), limitsB.hashCode())
        assertNotEquals(limitsA, limitsC)
        assertTrue(limitsA.toString().contains("maxDepth=10"))
    }
}

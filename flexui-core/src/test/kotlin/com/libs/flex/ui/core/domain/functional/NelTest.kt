package com.libs.flex.ui.core.domain.functional

import kotlin.test.Test
import kotlin.test.assertEquals

class NelTest {

    @Test
    fun `Nel of creates single element list`() {
        val nel = Nel.of("first")
        assertEquals("first", nel.head)
        assertEquals(emptyList<String>(), nel.tail)
        assertEquals(1, nel.size)
        assertEquals(listOf("first"), nel.toList())
    }

    @Test
    fun `Nel with tail maintains order and size`() {
        val nel = Nel("a", listOf("b", "c"))
        assertEquals("a", nel.head)
        assertEquals(listOf("b", "c"), nel.tail)
        assertEquals(3, nel.size)
        assertEquals(listOf("a", "b", "c"), nel.toList())
    }

    @Test
    fun `plus concatenates two Nels`() {
        val nel1 = Nel.of("1")
        val nel2 = Nel("2", listOf("3"))

        val combined = nel1 + nel2
        assertEquals(listOf("1", "2", "3"), combined.toList())
    }

    @Test
    fun `map transforms Nel preserving non-emptiness`() {
        val nel = Nel(1, listOf(2, 3))
        val mapped = nel.map { it * 10 }

        assertEquals(10, mapped.head)
        assertEquals(listOf(20, 30), mapped.tail)
        assertEquals(listOf(10, 20, 30), mapped.toList())
    }
}

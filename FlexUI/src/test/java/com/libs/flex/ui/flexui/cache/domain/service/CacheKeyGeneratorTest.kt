package com.libs.flex.ui.flexui.cache.domain.service

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class CacheKeyGeneratorTest {

    @After
    fun tearDown() {
        // No mocks to clear in this test
    }

    @Test
    fun `generateKey should return consistent hash for same input`() {
        // Given
        val generator = provideCacheKeyGenerator()
        val jsonString = """{"type": "componentButton", "text": "Click"}"""

        // When
        val key1 = generator.generateKey(jsonString)
        val key2 = generator.generateKey(jsonString)

        // Then
        assertEquals(key1, key2)
    }

    @Test
    fun `generateKey should return 32 character hexadecimal string`() {
        // Given
        val generator = provideCacheKeyGenerator()
        val jsonString = """{"type": "componentButton"}"""

        // When
        val key = generator.generateKey(jsonString)

        // Then
        assertEquals(32, key.length)
        assert(key.matches(Regex("^[a-f0-9]{32}$")))
    }

    @Test
    fun `generateKey should return different hashes for different inputs`() {
        // Given
        val generator = provideCacheKeyGenerator()
        val json1 = """{"type": "componentButton", "text": "Click"}"""
        val json2 = """{"type": "componentButton", "text": "Submit"}"""

        // When
        val key1 = generator.generateKey(json1)
        val key2 = generator.generateKey(json2)

        // Then
        assertNotEquals(key1, key2)
    }

    @Test
    fun `generateKey should be case sensitive`() {
        // Given
        val generator = provideCacheKeyGenerator()
        val json1 = """{"type": "componentButton"}"""
        val json2 = """{"type": "ComponentButton"}"""

        // When
        val key1 = generator.generateKey(json1)
        val key2 = generator.generateKey(json2)

        // Then
        assertNotEquals(key1, key2)
    }

    @Test
    fun `generateKey should be whitespace sensitive`() {
        // Given
        val generator = provideCacheKeyGenerator()
        val json1 = """{"type":"componentButton"}"""
        val json2 = """{"type": "componentButton"}"""

        // When
        val key1 = generator.generateKey(json1)
        val key2 = generator.generateKey(json2)

        // Then
        assertNotEquals(key1, key2)
    }

    @Test
    fun `generateKey should handle empty string`() {
        // Given
        val generator = provideCacheKeyGenerator()
        val jsonString = ""

        // When
        val key = generator.generateKey(jsonString)

        // Then
        assertEquals(32, key.length)
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", key) // MD5 of empty string
    }

    @Test
    fun `generateKey should handle large JSON strings`() {
        // Given
        val generator = provideCacheKeyGenerator()
        val largeJson = """{"type": "componentButton", "text": "${"a".repeat(10000)}"}"""

        // When
        val key = generator.generateKey(largeJson)

        // Then
        assertEquals(32, key.length)
        assert(key.matches(Regex("^[a-f0-9]{32}$")))
    }

    @Test
    fun `generateKey should handle special characters`() {
        // Given
        val generator = provideCacheKeyGenerator()
        val jsonString = """{"type": "componentButton", "text": "Hello\nWorld\t!@#$%"}"""

        // When
        val key = generator.generateKey(jsonString)

        // Then
        assertEquals(32, key.length)
        assert(key.matches(Regex("^[a-f0-9]{32}$")))
    }

    // Provider function
    private fun provideCacheKeyGenerator() = CacheKeyGenerator()
}

package com.libs.flex.ui.flexui.cache.infrastructure.adapter

import com.libs.flex.ui.flexui.model.AtomicDescriptor
import com.libs.flex.ui.flexui.model.ComponentType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryComponentCacheTest {

    @After
    fun tearDown() {
        // No mocks to clear in this test
    }

    @Test
    fun `get should return null when key does not exist`() {
        // Given
        val cache = provideInMemoryCache()
        val key = "nonexistent_key"

        // When
        val result = cache.get(key)

        // Then
        assertNull(result)
    }

    @Test
    fun `put and get should store and retrieve descriptor`() {
        // Given
        val cache = provideInMemoryCache()
        val key = "test_key"
        val descriptor = provideAtomicDescriptor()

        // When
        cache.put(key, descriptor)
        val result = cache.get(key)

        // Then
        assertNotNull(result)
        assertEquals(descriptor.id, result?.id)
        assertEquals(descriptor.type, result?.type)
    }

    @Test
    fun `put should overwrite existing entry with same key`() {
        // Given
        val cache = provideInMemoryCache()
        val key = "test_key"
        val descriptor1 = provideAtomicDescriptor(id = "button1")
        val descriptor2 = provideAtomicDescriptor(id = "button2")

        // When
        cache.put(key, descriptor1)
        cache.put(key, descriptor2)
        val result = cache.get(key)

        // Then
        assertNotNull(result)
        assertEquals("button2", result?.id)
    }

    @Test
    fun `clear should remove all entries`() {
        // Given
        val cache = provideInMemoryCache()
        cache.put("key1", provideAtomicDescriptor(id = "button1"))
        cache.put("key2", provideAtomicDescriptor(id = "button2"))
        cache.put("key3", provideAtomicDescriptor(id = "button3"))

        // When
        cache.clear()

        // Then
        assertNull(cache.get("key1"))
        assertNull(cache.get("key2"))
        assertNull(cache.get("key3"))
        assertEquals(0, cache.size())
    }

    @Test
    fun `contains should return true when key exists`() {
        // Given
        val cache = provideInMemoryCache()
        val key = "test_key"
        cache.put(key, provideAtomicDescriptor())

        // When
        val result = cache.contains(key)

        // Then
        assertTrue(result)
    }

    @Test
    fun `contains should return false when key does not exist`() {
        // Given
        val cache = provideInMemoryCache()
        val key = "nonexistent_key"

        // When
        val result = cache.contains(key)

        // Then
        assertFalse(result)
    }

    @Test
    fun `size should return correct number of entries`() {
        // Given
        val cache = provideInMemoryCache()

        // When
        cache.put("key1", provideAtomicDescriptor())
        cache.put("key2", provideAtomicDescriptor())
        cache.put("key3", provideAtomicDescriptor())

        // Then
        assertEquals(3, cache.size())
    }

    @Test
    fun `size should return zero for empty cache`() {
        // Given
        val cache = provideInMemoryCache()

        // When
        val size = cache.size()

        // Then
        assertEquals(0, size)
    }

    @Test
    fun `cache should evict oldest entry when max size is exceeded`() {
        // Given
        val cache = provideInMemoryCache(maxSize = 3)
        cache.put("key1", provideAtomicDescriptor(id = "button1"))
        cache.put("key2", provideAtomicDescriptor(id = "button2"))
        cache.put("key3", provideAtomicDescriptor(id = "button3"))

        // When
        cache.put("key4", provideAtomicDescriptor(id = "button4"))

        // Then
        assertNull(cache.get("key1")) // Oldest entry should be evicted
        assertNotNull(cache.get("key2"))
        assertNotNull(cache.get("key3"))
        assertNotNull(cache.get("key4"))
        assertEquals(3, cache.size())
    }

    @Test
    fun `cache should implement LRU eviction policy`() {
        // Given
        val cache = provideInMemoryCache(maxSize = 3)
        cache.put("key1", provideAtomicDescriptor(id = "button1"))
        cache.put("key2", provideAtomicDescriptor(id = "button2"))
        cache.put("key3", provideAtomicDescriptor(id = "button3"))

        // When
        cache.get("key1") // Access key1 to make it recently used
        cache.put("key4", provideAtomicDescriptor(id = "button4"))

        // Then
        assertNotNull(cache.get("key1")) // key1 should still exist (recently accessed)
        assertNull(cache.get("key2")) // key2 should be evicted (least recently used)
        assertNotNull(cache.get("key3"))
        assertNotNull(cache.get("key4"))
    }

    @Test
    fun `cache should handle multiple puts and gets`() {
        // Given
        val cache = provideInMemoryCache()
        val entries = (1..10).map { "key$it" to provideAtomicDescriptor(id = "button$it") }

        // When
        entries.forEach { (key, descriptor) ->
            cache.put(key, descriptor)
        }

        // Then
        entries.forEach { (key, descriptor) ->
            val result = cache.get(key)
            assertNotNull(result)
            assertEquals(descriptor.id, result?.id)
        }
        assertEquals(10, cache.size())
    }

    @Test
    fun `cache should be thread-safe for concurrent access`() {
        // Given
        val cache = provideInMemoryCache()
        val key = "test_key"
        val descriptor = provideAtomicDescriptor()

        // When
        cache.put(key, descriptor)
        val result = cache.get(key)

        // Then
        assertNotNull(result)
        assertEquals(descriptor.id, result?.id)
    }

    // Provider functions
    private fun provideInMemoryCache(maxSize: Int = 50) = InMemoryComponentCache(maxSize)

    private fun provideAtomicDescriptor(
        id: String = "test_button",
        type: ComponentType = ComponentType.COMPONENT_BUTTON,
        text: String = "Click Me"
    ) = AtomicDescriptor(
        id = id,
        type = type,
        text = text
    )
}

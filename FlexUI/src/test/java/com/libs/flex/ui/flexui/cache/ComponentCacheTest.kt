package com.libs.flex.ui.flexui.cache

import com.libs.flex.ui.flexui.cache.domain.ports.ComponentCachePort
import com.libs.flex.ui.flexui.cache.domain.service.CacheKeyGenerator
import com.libs.flex.ui.flexui.model.AtomicDescriptor
import com.libs.flex.ui.flexui.model.ComponentDescriptor
import com.libs.flex.ui.flexui.model.ComponentType
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ComponentCacheTest {

    @After
    fun tearDown() {
        unmockkAll()
        clearAllMocks()
    }

    @Test
    fun `getByJson should return null when cache is empty`() {
        // Given
        val cache = provideComponentCache()
        val jsonString = """{"type": "componentButton"}"""

        // When
        val result = cache.getByJson(jsonString)

        // Then
        assertNull(result)
    }

    @Test
    fun `getByJson should return cached descriptor when exists`() {
        // Given
        val descriptor = provideAtomicDescriptor()
        val cache = provideComponentCache(cachedDescriptor = descriptor)
        val jsonString = """{"type": "componentButton"}"""

        // When
        val result = cache.getByJson(jsonString)

        // Then
        assertNotNull(result)
        assertEquals(descriptor.id, result?.id)
    }

    @Test
    fun `putByJson should store descriptor in cache`() {
        // Given
        val mockPort = provideMockCachePort()
        val keyGenerator = provideCacheKeyGenerator()
        val cache = ComponentCache(mockPort, keyGenerator)
        val jsonString = """{"type": "componentButton"}"""
        val descriptor = provideAtomicDescriptor()
        val expectedKey = keyGenerator.generateKey(jsonString)

        // When
        cache.putByJson(jsonString, descriptor)

        // Then
        verify { mockPort.put(expectedKey, descriptor) }
    }

    @Test
    fun `get should delegate to cache port`() {
        // Given
        val descriptor = provideAtomicDescriptor()
        val mockPort = provideMockCachePort(cachedDescriptor = descriptor)
        val cache = ComponentCache(mockPort, provideCacheKeyGenerator())
        val key = "test_key"

        // When
        val result = cache.get(key)

        // Then
        assertNotNull(result)
        verify { mockPort.get(key) }
    }

    @Test
    fun `put should delegate to cache port`() {
        // Given
        val mockPort = provideMockCachePort()
        val cache = ComponentCache(mockPort, provideCacheKeyGenerator())
        val key = "test_key"
        val descriptor = provideAtomicDescriptor()

        // When
        cache.put(key, descriptor)

        // Then
        verify { mockPort.put(key, descriptor) }
    }

    @Test
    fun `clear should delegate to cache port`() {
        // Given
        val mockPort = provideMockCachePort()
        val cache = ComponentCache(mockPort, provideCacheKeyGenerator())

        // When
        cache.clear()

        // Then
        verify { mockPort.clear() }
    }

    @Test
    fun `containsJson should return true when descriptor exists`() {
        // Given
        val mockPort = provideMockCachePort(contains = true)
        val keyGenerator = provideCacheKeyGenerator()
        val cache = ComponentCache(mockPort, keyGenerator)
        val jsonString = """{"type": "componentButton"}"""

        // When
        val result = cache.containsJson(jsonString)

        // Then
        assertTrue(result)
    }

    @Test
    fun `containsJson should return false when descriptor does not exist`() {
        // Given
        val mockPort = provideMockCachePort(contains = false)
        val keyGenerator = provideCacheKeyGenerator()
        val cache = ComponentCache(mockPort, keyGenerator)
        val jsonString = """{"type": "componentButton"}"""

        // When
        val result = cache.containsJson(jsonString)

        // Then
        assertFalse(result)
    }

    @Test
    fun `size should return cache size from port`() {
        // Given
        val mockPort = provideMockCachePort(size = 5)
        val cache = ComponentCache(mockPort, provideCacheKeyGenerator())

        // When
        val result = cache.size()

        // Then
        assertEquals(5, result)
    }

    @Test
    fun `generateKey should return MD5 hash of JSON string`() {
        // Given
        val cache = provideComponentCache()
        val jsonString = """{"type": "componentButton"}"""

        // When
        val key = cache.generateKey(jsonString)

        // Then
        assertEquals(32, key.length)
        assert(key.matches(Regex("^[a-f0-9]{32}$")))
    }

    @Test
    fun `getByJson should use same key as putByJson for same JSON`() {
        // Given
        val mockPort = provideMockCachePort()
        val keyGenerator = provideCacheKeyGenerator()
        val cache = ComponentCache(mockPort, keyGenerator)
        val jsonString = """{"type": "componentButton"}"""
        val descriptor = provideAtomicDescriptor()

        // When
        cache.putByJson(jsonString, descriptor)
        cache.getByJson(jsonString)

        // Then
        val expectedKey = keyGenerator.generateKey(jsonString)
        verify { mockPort.put(expectedKey, descriptor) }
        verify { mockPort.get(expectedKey) }
    }

    @Test
    fun `different JSON strings should generate different keys`() {
        // Given
        val cache = provideComponentCache()
        val json1 = """{"type": "componentButton", "text": "Click"}"""
        val json2 = """{"type": "componentButton", "text": "Submit"}"""

        // When
        val key1 = cache.generateKey(json1)
        val key2 = cache.generateKey(json2)

        // Then
        assertNotNull(key1)
        assertNotNull(key2)
        assertTrue(key1 != key2)
    }

    // Provider functions
    private fun provideComponentCache(
        cachedDescriptor: ComponentDescriptor? = null
    ): ComponentCache {
        val mockPort = provideMockCachePort(cachedDescriptor = cachedDescriptor)
        val keyGenerator = provideCacheKeyGenerator()
        return ComponentCache(mockPort, keyGenerator)
    }

    private fun provideMockCachePort(
        cachedDescriptor: ComponentDescriptor? = null,
        contains: Boolean = false,
        size: Int = 0
    ): ComponentCachePort {
        val mock = mockk<ComponentCachePort>(relaxed = true)
        every { mock.get(any()) } returns cachedDescriptor
        every { mock.contains(any()) } returns contains
        every { mock.size() } returns size
        return mock
    }

    private fun provideCacheKeyGenerator() = CacheKeyGenerator()

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

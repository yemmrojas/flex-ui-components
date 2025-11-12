package com.libs.flex.ui.flexui.cache

import com.libs.flex.ui.flexui.cache.domain.ports.ComponentCachePort
import com.libs.flex.ui.flexui.cache.domain.service.CacheKeyGenerator
import com.libs.flex.ui.flexui.model.ComponentDescriptor
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Public API for component caching.
 * Provides a simplified interface for caching parsed component descriptors.
 *
 * This class acts as a facade, combining the cache port and key generator
 * to provide a convenient API for the rest of the application.
 *
 * Example usage:
 * ```
 * val cache = ComponentCache()
 * val jsonString = """{"type": "componentButton", "text": "Click"}"""
 *
 * // Check cache first
 * val cached = cache.getByJson(jsonString)
 * if (cached != null) {
 *     // Use cached descriptor
 * } else {
 *     // Parse JSON and cache result
 *     val descriptor = parser.parse(jsonString)
 *     cache.putByJson(jsonString, descriptor)
 * }
 * ```
 */
@Singleton
class ComponentCache @Inject constructor(
    private val cachePort: ComponentCachePort,
    private val keyGenerator: CacheKeyGenerator
) {
    /**
     * Retrieves a cached component descriptor using the JSON string as key.
     * Automatically generates MD5 hash from the JSON string.
     *
     * @param jsonString The JSON string to look up
     * @return The cached ComponentDescriptor if found, null otherwise
     */
    fun getByJson(jsonString: String): ComponentDescriptor? {
        val key = keyGenerator.generateKey(jsonString)
        return cachePort.get(key)
    }

    /**
     * Stores a component descriptor in cache using the JSON string as key.
     * Automatically generates MD5 hash from the JSON string.
     *
     * @param jsonString The JSON string used as key
     * @param descriptor The ComponentDescriptor to cache
     */
    fun putByJson(jsonString: String, descriptor: ComponentDescriptor) {
        val key = keyGenerator.generateKey(jsonString)
        cachePort.put(key, descriptor)
    }

    /**
     * Retrieves a cached component descriptor by its hash key.
     *
     * @param key The MD5 hash key
     * @return The cached ComponentDescriptor if found, null otherwise
     */
    fun get(key: String): ComponentDescriptor? {
        return cachePort.get(key)
    }

    /**
     * Stores a component descriptor in cache with a specific key.
     *
     * @param key The cache key
     * @param descriptor The ComponentDescriptor to cache
     */
    fun put(key: String, descriptor: ComponentDescriptor) {
        cachePort.put(key, descriptor)
    }

    /**
     * Removes all entries from the cache.
     */
    fun clear() {
        cachePort.clear()
    }

    /**
     * Checks if a component descriptor exists in cache for the given JSON.
     *
     * @param jsonString The JSON string to check
     * @return true if cached, false otherwise
     */
    fun containsJson(jsonString: String): Boolean {
        val key = keyGenerator.generateKey(jsonString)
        return cachePort.contains(key)
    }

    /**
     * Returns the current number of cached entries.
     *
     * @return The cache size
     */
    fun size(): Int {
        return cachePort.size()
    }

    /**
     * Generates a cache key from a JSON string without accessing the cache.
     * Useful for debugging or manual cache management.
     *
     * @param jsonString The JSON string to hash
     * @return The MD5 hash key
     */
    fun generateKey(jsonString: String): String {
        return keyGenerator.generateKey(jsonString)
    }
}

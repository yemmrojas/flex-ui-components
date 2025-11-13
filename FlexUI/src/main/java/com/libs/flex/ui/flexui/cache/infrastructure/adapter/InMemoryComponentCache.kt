package com.libs.flex.ui.flexui.cache.infrastructure.adapter

import com.libs.flex.ui.flexui.cache.domain.ports.ComponentCachePort
import com.libs.flex.ui.flexui.model.ComponentDescriptor
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory implementation of ComponentCachePort.
 * Stores parsed component descriptors in memory with LRU eviction policy.
 *
 * This implementation uses a LinkedHashMap with access-order to implement
 * Least Recently Used (LRU) cache eviction when the cache reaches max capacity.
 *
 * Thread-safe implementation using synchronized blocks.
 *
 * @param maxSize Maximum number of entries to store (default: 50)
 */
@Singleton
class InMemoryComponentCache @Inject constructor(
    private val maxSize: Int = DEFAULT_MAX_SIZE
) : ComponentCachePort {

    private val cache = object : LinkedHashMap<String, CacheEntry>(
        maxSize,
        LOAD_FACTOR,
        true // access-order (LRU)
    ) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, CacheEntry>?): Boolean {
            return size > maxSize
        }
    }

    /**
     * Data class representing a cache entry with metadata.
     *
     * @property descriptor The cached ComponentDescriptor
     * @property timestamp When the entry was created (for debugging/monitoring)
     */
    private data class CacheEntry(
        val descriptor: ComponentDescriptor,
        val timestamp: Long = System.currentTimeMillis()
    )

    override fun get(key: String): ComponentDescriptor? = synchronized(cache) {
        cache[key]?.descriptor
    }

    override fun put(key: String, descriptor: ComponentDescriptor) = synchronized(cache) {
        cache[key] = CacheEntry(descriptor)
    }

    override fun clear() = synchronized(cache) {
        cache.clear()
    }

    override fun contains(key: String): Boolean = synchronized(cache) {
        cache.containsKey(key)
    }

    override fun size(): Int = synchronized(cache) {
        cache.size
    }

    companion object {
        private const val DEFAULT_MAX_SIZE = 50
        private const val LOAD_FACTOR = 0.75f
    }
}

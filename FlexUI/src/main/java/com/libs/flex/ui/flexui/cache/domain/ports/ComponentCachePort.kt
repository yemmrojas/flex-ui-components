package com.libs.flex.ui.flexui.cache.domain.ports

import com.libs.flex.ui.flexui.model.ComponentDescriptor

/**
 * Port interface for component caching operations.
 * Defines the contract for storing and retrieving parsed component descriptors.
 *
 * This interface follows the hexagonal architecture pattern, allowing different
 * cache implementations (in-memory, disk, etc.) without affecting the domain logic.
 */
interface ComponentCachePort {
    /**
     * Retrieves a cached component descriptor by its key.
     *
     * @param key The unique identifier for the cached component (typically MD5 hash of JSON)
     * @return The cached ComponentDescriptor if found, null otherwise
     */
    fun get(key: String): ComponentDescriptor?

    /**
     * Stores a component descriptor in the cache.
     *
     * @param key The unique identifier for the component (typically MD5 hash of JSON)
     * @param descriptor The ComponentDescriptor to cache
     */
    fun put(key: String, descriptor: ComponentDescriptor)

    /**
     * Removes all entries from the cache.
     * Useful for memory management or when cache invalidation is needed.
     */
    fun clear()

    /**
     * Checks if a component descriptor exists in the cache.
     *
     * @param key The unique identifier to check
     * @return true if the key exists in cache, false otherwise
     */
    fun contains(key: String): Boolean

    /**
     * Returns the current number of cached entries.
     *
     * @return The size of the cache
     */
    fun size(): Int
}

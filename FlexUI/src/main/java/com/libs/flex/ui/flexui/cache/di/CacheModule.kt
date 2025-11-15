package com.libs.flex.ui.flexui.cache.di

import com.libs.flex.ui.flexui.cache.domain.ports.ComponentCachePort
import com.libs.flex.ui.flexui.cache.infrastructure.adapter.InMemoryComponentCache
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for component cache dependency injection.
 *
 * This module provides bindings for the caching system following
 * hexagonal architecture principles.
 *
 * ## Architecture
 *
 * The cache system uses:
 * - ComponentCachePort: Domain interface (port)
 * - InMemoryComponentCache: Infrastructure implementation (adapter)
 *
 * ## Configuration
 *
 * The cache is configured with a default max size of 50 entries.
 * This can be adjusted by modifying the DEFAULT_CACHE_SIZE constant.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class CacheModule {

    /**
     * Binds the InMemoryComponentCache implementation to the ComponentCachePort interface.
     *
     * This follows the Dependency Inversion Principle by depending on
     * the abstraction (port) rather than the concrete implementation.
     *
     * @param cache The InMemoryComponentCache implementation
     * @return ComponentCachePort interface
     */
    @Binds
    @Singleton
    abstract fun bindComponentCache(
        cache: InMemoryComponentCache
    ): ComponentCachePort

    companion object {
        private const val DEFAULT_CACHE_SIZE = 50

        /**
         * Provides the maximum cache size configuration.
         *
         * This value determines how many parsed component descriptors
         * will be stored in memory before LRU eviction occurs.
         *
         * @return Maximum number of cache entries
         */
        @Provides
        fun provideCacheMaxSize(): Int = DEFAULT_CACHE_SIZE
    }
}

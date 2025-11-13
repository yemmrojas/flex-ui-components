package com.libs.flex.ui.flexui.cache.di

import com.libs.flex.ui.flexui.cache.domain.ports.ComponentCachePort
import com.libs.flex.ui.flexui.cache.domain.service.CacheKeyGenerator
import com.libs.flex.ui.flexui.cache.infrastructure.adapter.InMemoryComponentCache
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger Hilt module for cache-related dependencies.
 * Provides cache implementation and key generator following hexagonal architecture.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class CacheModule {

    /**
     * Binds the in-memory cache implementation to the ComponentCachePort interface.
     * This allows the domain layer to depend on the abstraction, not the implementation.
     */
    @Binds
    @Singleton
    abstract fun bindComponentCache(
        cache: InMemoryComponentCache
    ): ComponentCachePort

    companion object {
        /**
         * Provides a singleton instance of CacheKeyGenerator.
         * Uses @Provides because it's a concrete class without interface.
         */
        @Provides
        @Singleton
        fun provideCacheKeyGenerator(): CacheKeyGenerator {
            return CacheKeyGenerator()
        }
    }
}

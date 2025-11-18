package com.libs.flex.ui.flexuicomponents.di

import com.libs.flex.ui.flexuicomponents.data.repository.JsonAssetRepositoryImpl
import com.libs.flex.ui.flexuicomponents.domain.repository.JsonAssetRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for app-level dependency injection.
 * Provides bindings for repositories and use cases.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    /**
     * Binds the JsonAssetRepository implementation.
     *
     * @param impl The implementation to bind
     * @return The repository interface
     */
    @Binds
    @Singleton
    abstract fun bindJsonAssetRepository(
        impl: JsonAssetRepositoryImpl
    ): JsonAssetRepository
}

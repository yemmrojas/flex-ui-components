package com.libs.flex.ui.flexui.di

import com.libs.flex.ui.flexui.cache.ComponentCache
import com.libs.flex.ui.flexui.components.ComponentFactory
import com.libs.flex.ui.flexui.parser.JsonParser
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt EntryPoint for accessing JsonRenderer dependencies from composables.
 *
 * This EntryPoint allows JsonRenderer to access Hilt-provided dependencies
 * without requiring the composable itself to be injected. This is the
 * recommended approach for accessing DI in composables that need to remain
 * simple and stateless.
 *
 * ## Purpose
 *
 * Composables cannot use constructor injection directly. This EntryPoint
 * provides a way to access Hilt dependencies from within a composable
 * using `EntryPointAccessors.fromApplication()`.
 *
 * ## Usage
 *
 * This EntryPoint is used automatically within JsonRenderer. No user
 * action is required. The JsonRenderer composable accesses it like this:
 *
 * ```kotlin
 * val context = LocalContext.current
 * val dependencies = remember {
 *     EntryPointAccessors.fromApplication(
 *         context.applicationContext,
 *         JsonRendererEntryPoint::class.java
 *     )
 * }
 * val cache = remember { dependencies.componentCache() }
 * val parser = remember { dependencies.jsonParser() }
 * val factory = remember { dependencies.componentFactory() }
 * ```
 *
 * ## Dependencies Provided
 *
 * - **ComponentCache**: Caches parsed component descriptors for performance
 * - **JsonParser**: Parses JSON strings into component descriptors
 * - **ComponentFactory**: Creates Compose components from descriptors
 *
 * @see com.libs.flex.ui.flexui.JsonRenderer
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface JsonRendererEntryPoint {
    /**
     * Provides the ComponentCache instance for caching parsed descriptors.
     */
    fun componentCache(): ComponentCache

    /**
     * Provides the JsonParser instance for parsing JSON strings.
     */
    fun jsonParser(): JsonParser

    /**
     * Provides the ComponentFactory instance for creating components.
     */
    fun componentFactory(): ComponentFactory
}

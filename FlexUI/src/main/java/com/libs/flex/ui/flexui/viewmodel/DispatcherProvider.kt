package com.libs.flex.ui.flexui.viewmodel

import kotlinx.coroutines.CoroutineDispatcher

/**
 * Abstraction of coroutine dispatchers to allow deterministic testing with StandardTestDispatcher.
 */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val mainImmediate: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
}

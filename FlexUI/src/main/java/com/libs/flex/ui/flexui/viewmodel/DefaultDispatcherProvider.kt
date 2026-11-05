package com.libs.flex.ui.flexui.viewmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Default production implementation of [DispatcherProvider] using standard kotlinx.coroutines Dispatchers.
 */
class DefaultDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher get() = Dispatchers.Main
    override val mainImmediate: CoroutineDispatcher get() = Dispatchers.Main.immediate
    override val io: CoroutineDispatcher get() = Dispatchers.IO
    override val default: CoroutineDispatcher get() = Dispatchers.Default
}


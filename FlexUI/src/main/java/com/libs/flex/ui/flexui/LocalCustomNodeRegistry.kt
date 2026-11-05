package com.libs.flex.ui.flexui

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf

/**
 * CompositionLocal providing access to the custom node renderers registered in [FlexUiConfig.customNodeRegistry].
 *
 * Prevents lambda instability in `@Immutable` models and enables seamless rendering of [com.libs.flex.ui.core.domain.model.node.CustomNode].
 */
val LocalCustomNodeRegistry: ProvidableCompositionLocal<Map<String, CustomNodeComposable>> =
    compositionLocalOf { emptyMap() }


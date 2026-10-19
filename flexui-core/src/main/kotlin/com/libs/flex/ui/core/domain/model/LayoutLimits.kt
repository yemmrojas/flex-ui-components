package com.libs.flex.ui.core.domain.model

public data class LayoutLimits(
    public val maxDepth: Int = DEFAULT_MAX_DEPTH,
    public val maxNodes: Int = DEFAULT_MAX_NODES,
    public val maxErrors: Int = DEFAULT_MAX_ERRORS
) {
    init {
        require(maxDepth > 0) { "maxDepth must be > 0" }
        require(maxNodes > 0) { "maxNodes must be > 0" }
        require(maxErrors > 0) { "maxErrors must be > 0" }
    }

    public companion object {
        public const val DEFAULT_MAX_DEPTH: Int = 20
        public const val DEFAULT_MAX_NODES: Int = 500
        public const val DEFAULT_MAX_ERRORS: Int = 50

        public val DEFAULT: LayoutLimits = LayoutLimits()
    }
}

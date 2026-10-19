package com.libs.flex.ui.core.domain.value

public data class SchemaVersion(public val value: String) {
    public companion object {
        public val CURRENT: SchemaVersion = SchemaVersion("1.0.0")
        public val SUPPORTED_VERSIONS: Set<String> = setOf("1.0.0")
        public val SUPPORTED_MIN: String = "1.0.0"
        public val SUPPORTED_MAX: String = "1.0.0"
    }
}

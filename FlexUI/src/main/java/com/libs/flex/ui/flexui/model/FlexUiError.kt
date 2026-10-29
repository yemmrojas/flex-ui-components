package com.libs.flex.ui.flexui.model

/**
 * Typed hierarchy of errors that can occur during layout parsing, session initialization,
 * or UI action execution in FlexUI.
 */
public sealed interface FlexUiError {

    /**
     * Error emitted when the input JSON string is malformed or schema validation fails.
     *
     * @property message Human-readable error description.
     * @property path Exact JSON path where the error occurred (e.g., "$.root.children[0]").
     */
    public data class InvalidJson(
        public val message: String,
        public val path: String? = null
    ) : FlexUiError

    /**
     * Error emitted when the layout document requests a schema version not supported by this runtime.
     *
     * @property version The unsupported schema version string.
     */
    public data class UnsupportedVersion(
        public val version: String
    ) : FlexUiError

    /**
     * Error emitted when an interactive action dispatched to the host exceeds the configured timeout.
     *
     * @property actionId Identifier of the timed-out action.
     */
    public data class ActionTimeout(
        public val actionId: String
    ) : FlexUiError

    /**
     * Error emitted when a core session operation or aggregate state transition fails.
     *
     * @property userMessage Safe message appropriate for display to end-users.
     * @property technicalCause Diagnostic or technical reason (e.g. exception class name) for logging.
     */
    public data class SessionError(
        public val userMessage: String,
        public val technicalCause: String? = null
    ) : FlexUiError

    /**
     * Error emitted when a layout asset or remote reference cannot be found.
     *
     * @property layoutId The missing layout identifier.
     */
    public data class LayoutNotFound(
        public val layoutId: String
    ) : FlexUiError
}


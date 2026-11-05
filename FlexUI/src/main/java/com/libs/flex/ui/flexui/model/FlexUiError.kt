package com.libs.flex.ui.flexui.model

/**
 * Typed hierarchy of errors that can occur during layout parsing, session initialization,
 * or UI action execution in FlexUI.
 */
sealed interface FlexUiError {

    /**
     * Error emitted when the input JSON string is malformed or schema validation fails.
     *
     * @property message Human-readable error description.
     * @property path Exact JSON path where the error occurred (e.g., "$.root.children[0]").
     */
    data class InvalidJson(
        val message: String,
        val path: String? = null
    ) : FlexUiError

    /**
     * Error emitted when the layout document requests a schema version not supported by this runtime.
     *
     * @property version The unsupported schema version string.
     */
    data class UnsupportedVersion(
        val version: String
    ) : FlexUiError

    /**
     * Error emitted when an interactive action dispatched to the host exceeds the configured timeout.
     *
     * @property actionId Identifier of the timed-out action.
     */
    data class ActionTimeout(
        val actionId: String
    ) : FlexUiError

    /**
     * Error emitted when a core session operation or aggregate state transition fails.
     *
     * @property userMessage Safe message appropriate for display to end-users.
     * @property technicalCause Diagnostic or technical reason (e.g. exception class name) for logging.
     */
    data class SessionError(
        val userMessage: String,
        val technicalCause: String? = null
    ) : FlexUiError

    /**
     * Error emitted when a layout asset or remote reference cannot be found.
     *
     * @property layoutId The missing layout identifier.
     */
    data class LayoutNotFound(
        val layoutId: String
    ) : FlexUiError
}

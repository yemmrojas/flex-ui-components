package com.libs.flex.ui.flexui.model

/**
 * Sealed interface representing the top-level UI state observed by FlexUI composables.
 *
 * It is completely immutable and contains no callbacks or lambdas, ensuring stable equality
 * across Jetpack Compose recompositions.
 */
public sealed interface FlexUiViewState {

    /**
     * Initial loading state while the layout is being parsed or the session is initializing.
     */
    public data object Loading : FlexUiViewState

    /**
     * Active state when the layout is successfully parsed and ready for user interaction.
     *
     * @property state The immutable content state containing the component tree, values, and errors.
     */
    public data class Content(
        public val state: FlexUiContentState
    ) : FlexUiViewState

    /**
     * Terminal or retryable error state when parsing, initialization, or session operations fail.
     *
     * @property error The typed error describing the failure.
     */
    public data class Error(
        public val error: FlexUiError
    ) : FlexUiViewState
}


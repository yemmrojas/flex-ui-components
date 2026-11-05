package com.libs.flex.ui.flexui.model

import com.libs.flex.ui.core.domain.ports.ActionResult

/**
 * Sealed interface representing one-shot, ephemeral UI events dispatched through a Channel.
 *
 * These events are triggered as discrete occurrences (e.g. notifications, navigation, action outcomes)
 * and must not be stored in [FlexUiViewState] to prevent unintended replay across recompositions or configuration changes.
 */
sealed interface FlexUiEvent {

    /**
     * Event emitted when an interactive user action (e.g. submit button click) has completed
     * execution via the command bus and host handler.
     *
     * @property result Outcome reported by the host action handler.
     */
    data class ActionExecuted(
        val result: ActionResult
    ) : FlexUiEvent

    /**
     * Event emitted when a transient feedback message (Snackbar or Toast) should be presented to the user.
     *
     * @property message The text message to display.
     */
    data class ToastMessage(
        val message: String
    ) : FlexUiEvent

    /**
     * Event emitted when a flow or action requests navigation to another destination route or screen.
     *
     * @property target Destination route or URI identifier.
     */
    data class NavigateTo(
        val target: String
    ) : FlexUiEvent
}

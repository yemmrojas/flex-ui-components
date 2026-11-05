package com.libs.flex.ui.flexui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.libs.flex.ui.core.application.bus.CommandBus
import com.libs.flex.ui.core.application.command.ProcessUserActionCommand
import com.libs.flex.ui.core.application.command.UpdateInputValueCommand
import com.libs.flex.ui.core.domain.functional.Either
import com.libs.flex.ui.core.domain.model.FormSession
import com.libs.flex.ui.core.domain.model.node.UIComponentNode
import com.libs.flex.ui.core.domain.ports.ActionResult
import com.libs.flex.ui.core.domain.value.ActionId
import com.libs.flex.ui.core.domain.value.FieldValue
import com.libs.flex.ui.core.domain.value.NodeKey
import com.libs.flex.ui.core.domain.value.SessionId
import com.libs.flex.ui.flexui.FlexUiConfig
import com.libs.flex.ui.flexui.adapter.session.ObservableFormSessionPort
import com.libs.flex.ui.flexui.model.FlexUiContentState
import com.libs.flex.ui.flexui.model.FlexUiError
import com.libs.flex.ui.flexui.model.FlexUiEvent
import com.libs.flex.ui.flexui.model.FlexUiViewState
import com.libs.flex.ui.flexui.usecase.InitializeLayoutSessionUseCase
import com.libs.flex.ui.flexui.usecase.InitializedLayoutSession
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

/**
 * Android ViewModel orchestrating UI state and user interactions for FlexUI forms.
 *
 * Adheres strictly to MVVM/MVI: delegates application workflows to Use Cases and CommandBus,
 * focusing purely on reactive state management and event propagation.
 */
class FlexUiViewModel(
    private val initializeLayoutSessionUseCase: InitializeLayoutSessionUseCase,
    private val sessionPort: ObservableFormSessionPort,
    private val commandBus: CommandBus,
    private val config: FlexUiConfig = FlexUiConfig()
) : ViewModel() {

    private val _viewState = MutableStateFlow<FlexUiViewState>(FlexUiViewState.Loading)
    val viewState: StateFlow<FlexUiViewState> = _viewState.asStateFlow()

    private val _events = Channel<FlexUiEvent>(Channel.BUFFERED)
    val events: Flow<FlexUiEvent> = _events.receiveAsFlow()

    private var currentSessionId: SessionId? = null
    private var currentJsonHash: Int? = null
    private var lastRawJson: String? = null
    private var sessionObservationJob: Job? = null

    private val currentContentState: FlexUiContentState?
        get() = (_viewState.value as? FlexUiViewState.Content)?.state

    /**
     * Loads a layout from a JSON string, leveraging hash reconciliation to preserve
     * state across recompositions, and delegating initialization to [InitializeLayoutSessionUseCase].
     */
    fun loadJson(jsonString: String) {
        if (isEquivalentLayout(jsonString)) return
        recordLayoutRequest(jsonString)

        viewModelScope.launch(config.dispatcherProvider.io) {
            _viewState.value = FlexUiViewState.Loading
            when (val outcome = initializeLayoutSessionUseCase(jsonString)) {
                is Either.Left -> _viewState.value = FlexUiViewState.Error(outcome.value)
                is Either.Right -> onSessionInitialized(outcome.value)
            }
        }
    }

    private fun isEquivalentLayout(jsonString: String): Boolean =
        jsonString.hashCode() == currentJsonHash && _viewState.value is FlexUiViewState.Content

    private fun recordLayoutRequest(jsonString: String) {
        currentJsonHash = jsonString.hashCode()
        lastRawJson = jsonString
    }

    private fun onSessionInitialized(sessionData: InitializedLayoutSession) {
        currentSessionId = sessionData.sessionId
        observeSession(sessionData.sessionId, sessionData.rootNode, sessionData.layoutId)
    }

    private fun observeSession(
        sessionId: SessionId,
        rootNode: UIComponentNode,
        layoutId: String
    ) {
        sessionObservationJob?.cancel()
        sessionObservationJob = viewModelScope.launch(config.dispatcherProvider.main) {
            sessionPort.observeSession(sessionId)
                .flowOn(config.dispatcherProvider.io)
                .collect { session ->
                    updateContentState(layoutId, rootNode, session)
                }
        }
    }

    private fun updateContentState(
        layoutId: String,
        rootNode: UIComponentNode,
        session: FormSession
    ) {
        val isSubmitting = currentContentState?.isSubmitting ?: false
        _viewState.value = FlexUiViewState.Content(
            FlexUiContentState(
                layoutId = layoutId,
                rootNode = rootNode,
                values = session.formValues,
                errors = session.formErrors,
                isSubmitting = isSubmitting
            )
        )
    }

    /**
     * Dispatches an input update command immediately to ensure responsive UI updates.
     */
    fun onInputValueChanged(nodeKey: NodeKey, value: FieldValue) {
        val sessionId = currentSessionId ?: return
        viewModelScope.launch(config.dispatcherProvider.mainImmediate) {
            commandBus.dispatch(
                UpdateInputValueCommand(
                    sessionId = sessionId.value,
                    componentId = nodeKey.componentId.value,
                    value = value,
                    instanceIndex = nodeKey.instanceIndex
                )
            )
        }
    }

    /**
     * Dispatches an interactive user action command with debounce/double-click protection
     * and configurable timeout.
     */
    fun onActionClicked(actionId: ActionId, nodeKey: NodeKey) {
        val sessionId = currentSessionId ?: return
        val content = currentContentState ?: return
        if (content.isSubmitting) return

        viewModelScope.launch(config.dispatcherProvider.main) {
            setSubmitting(true)
            val actionResult = withTimeoutOrNull(config.actionTimeoutMs.milliseconds) {
                dispatchActionCommand(sessionId, nodeKey)
            }

            if (actionResult == null) {
                _viewState.value = FlexUiViewState.Error(FlexUiError.ActionTimeout(actionId.value))
            } else {
                _events.send(FlexUiEvent.ActionExecuted(actionResult))
            }
            setSubmitting(false)
        }
    }

    private fun setSubmitting(isSubmitting: Boolean) {
        val current = currentContentState ?: return
        _viewState.value = FlexUiViewState.Content(current.copy(isSubmitting = isSubmitting))
    }

    private suspend fun dispatchActionCommand(
        sessionId: SessionId,
        nodeKey: NodeKey
    ): ActionResult? {
        val command = ProcessUserActionCommand(
            sessionId = sessionId.value,
            componentId = nodeKey.componentId.value,
            instanceIndex = nodeKey.instanceIndex
        )
        return commandBus.dispatch(command).getOrNull()
    }

    /**
     * Retries loading the last requested layout.
     * Consumed by UI error state retry buttons.
     */
    @Suppress("unused")
    fun retry() {
        val json = lastRawJson ?: return
        currentJsonHash = null
        loadJson(json)
    }

    override fun onCleared() {
        super.onCleared()
        sessionObservationJob?.cancel()
        deleteCurrentSession()
    }

    private fun deleteCurrentSession() {
        val sessionId = currentSessionId ?: return
        viewModelScope.launch(NonCancellable + config.dispatcherProvider.io) {
            sessionPort.delete(sessionId)
        }
    }

    internal fun clearForTesting() {
        onCleared()
    }
}
package com.libs.flex.ui.flexuicomponents.presentation.screens.demo

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.libs.flex.ui.flexui.model.ComponentEvent
import com.libs.flex.ui.flexuicomponents.domain.model.DemoType
import com.libs.flex.ui.flexuicomponents.domain.usecase.LoadDemoJsonUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Demo screen.
 * Manages loading and displaying JSON-driven UI demos.
 */
@HiltViewModel
class DemoViewModel @Inject constructor(
    private val loadDemoJsonUseCase: LoadDemoJsonUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val demoTypeRoute: String = checkNotNull(savedStateHandle["demoType"])

    private val _uiState = MutableStateFlow<DemoUiState>(DemoUiState.Loading)
    val uiState: StateFlow<DemoUiState> = _uiState.asStateFlow()

    init {
        loadDemo()
    }

    private fun loadDemo() {
        viewModelScope.launch {
            _uiState.value = DemoUiState.Loading

            val demoType = DemoType.fromRoute(demoTypeRoute)
            if (demoType == null) {
                _uiState.value = DemoUiState.Error("Invalid demo type: $demoTypeRoute")
                return@launch
            }

            val jsonContent = loadDemoJsonUseCase(demoType)
            if (jsonContent != null) {
                _uiState.value = DemoUiState.Success(
                    demoType = demoType,
                    jsonContent = jsonContent
                )
            } else {
                _uiState.value = DemoUiState.Error("Failed to load demo: ${demoType.title}")
            }
        }
    }

    /**
     * Handles component interaction events from JsonRenderer.
     * Logs all events for demonstration purposes.
     */
    fun onComponentEvent(event: ComponentEvent) {
        when (event) {
            is ComponentEvent.Click -> {
                Log.i(TAG, "Click Event - Component: ${event.componentId}, Action: ${event.actionId}, Timestamp: ${event.timestamp}")
            }

            is ComponentEvent.ValueChange -> {
                Log.i(TAG, "ValueChange Event - Component: ${event.componentId}, Value: ${event.value}, Timestamp: ${event.timestamp}")
            }

            is ComponentEvent.Selection -> {
                Log.i(TAG, "Selection Event - Component: ${event.componentId}, Selected: ${event.selectedValue}, Timestamp: ${event.timestamp}")
            }
        }
    }

    companion object {
        private const val TAG = "FlexUIDemo"
    }
}

/**
 * UI state for the Demo screen.
 */
sealed interface DemoUiState {
    data object Loading : DemoUiState
    
    data class Success(
        val demoType: DemoType,
        val jsonContent: String
    ) : DemoUiState
    
    data class Error(val message: String) : DemoUiState
}

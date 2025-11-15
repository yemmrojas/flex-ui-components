package com.libs.flex.ui.flexuicomponents.presentation.screens.home

import androidx.lifecycle.ViewModel
import com.libs.flex.ui.flexuicomponents.domain.model.DemoType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * ViewModel for the Home screen.
 * Manages the list of available demos.
 */
@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDemos()
    }

    private fun loadDemos() {
        _uiState.value = HomeUiState(
            demos = DemoType.entries
        )
    }
}

/**
 * UI state for the Home screen.
 *
 * @property demos List of available demo types
 */
data class HomeUiState(
    val demos: List<DemoType> = emptyList()
)

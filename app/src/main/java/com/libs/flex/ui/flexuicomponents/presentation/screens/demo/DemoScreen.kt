package com.libs.flex.ui.flexuicomponents.presentation.screens.demo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.libs.flex.ui.flexui.JsonRenderer

/**
 * Demo screen displaying a JSON-driven UI.
 *
 * @param demoTypeRoute Route parameter for the demo type
 * @param onNavigateBack Callback to navigate back
 * @param viewModel ViewModel for the demo screen
 */
@Composable
fun DemoScreen(
    demoTypeRoute: String,
    onNavigateBack: () -> Unit,
    viewModel: DemoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DemoContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onComponentEvent = viewModel::onComponentEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DemoContent(
    uiState: DemoUiState,
    onNavigateBack: () -> Unit,
    onComponentEvent: (com.libs.flex.ui.flexui.model.ComponentEvent) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (uiState) {
                            is DemoUiState.Success -> uiState.demoType.title
                            is DemoUiState.Loading -> "Loading..."
                            is DemoUiState.Error -> "Error"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is DemoUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is DemoUiState.Success -> {
                    JsonRenderer(
                        jsonString = uiState.jsonContent,
                        onEvent = onComponentEvent,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is DemoUiState.Error -> {
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

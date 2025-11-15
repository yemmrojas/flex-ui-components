package com.libs.flex.ui.flexuicomponents.presentation.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.libs.flex.ui.flexuicomponents.domain.model.DemoType

/**
 * Home screen displaying list of available demos.
 *
 * @param onDemoSelected Callback when a demo is selected
 * @param viewModel ViewModel for the home screen
 */
@Composable
fun HomeScreen(
    onDemoSelected: (DemoType) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        demos = uiState.demos,
        onDemoSelected = onDemoSelected
    )
}

@Composable
private fun HomeContent(
    demos: List<DemoType>,
    onDemoSelected: (DemoType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "FlexUI Demo Selector",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Select a demo to see FlexUI in action:",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        demos.forEach { demo ->
            Button(
                onClick = { onDemoSelected(demo) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(demo.title)
            }
        }
    }
}

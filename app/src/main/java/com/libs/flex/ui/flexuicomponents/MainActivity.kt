package com.libs.flex.ui.flexuicomponents

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.libs.flex.ui.flexuicomponents.presentation.navigation.DemoNavigation
import com.libs.flex.ui.flexuicomponents.ui.theme.FlexUIComponentsTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for FlexUI demo application.
 * Uses Navigation Compose and Clean Architecture with MVVM pattern.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlexUIComponentsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    DemoNavigation(navController = navController)
                }
            }
        }
    }
}
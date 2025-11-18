package com.libs.flex.ui.flexuicomponents.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.libs.flex.ui.flexuicomponents.presentation.screens.demo.DemoScreen
import com.libs.flex.ui.flexuicomponents.presentation.screens.home.HomeScreen

/**
 * Navigation routes for the demo app.
 */
object DemoRoutes {
    const val HOME = "home"
    const val DEMO = "demo/{demoType}"
    
    fun createDemoRoute(demoType: String) = "demo/$demoType"
}

/**
 * Main navigation graph for the demo app.
 *
 * @param navController Navigation controller for managing navigation
 * @param modifier Modifier to be applied to the NavHost
 */
@Composable
fun DemoNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = DemoRoutes.HOME,
        modifier = modifier
    ) {
        composable(DemoRoutes.HOME) {
            HomeScreen(
                onDemoSelected = { demoType ->
                    navController.navigate(DemoRoutes.createDemoRoute(demoType.route))
                }
            )
        }

        composable(
            route = DemoRoutes.DEMO,
            arguments = listOf(
                navArgument("demoType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val demoTypeRoute = backStackEntry.arguments?.getString("demoType") ?: return@composable
            
            DemoScreen(
                demoTypeRoute = demoTypeRoute,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

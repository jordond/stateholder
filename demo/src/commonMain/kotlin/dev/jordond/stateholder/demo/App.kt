package dev.jordond.stateholder.demo

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import dev.jordond.stateholder.demo.di.DI
import dev.jordond.stateholder.demo.navigation.AppRoute
import dev.jordond.stateholder.demo.navigation.navSavedStateConfiguration
import dev.jordond.stateholder.demo.screens.dashboard.DashboardScreen
import dev.jordond.stateholder.demo.screens.dashboard.DashboardViewModel

@Composable
fun App() {
    MaterialTheme {
        val backStack = rememberNavBackStack(navSavedStateConfiguration, AppRoute.Dashboard)

        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryProvider =
                entryProvider {
                    entry<AppRoute.Dashboard> {
                        val viewModel =
                            viewModel {
                                DashboardViewModel(
                                    stateProvider = DI.dashboardStateProvider,
                                    userRepository = DI.userRepository,
                                    taskRepository = DI.taskRepository,
                                )
                            }
                        DashboardScreen(viewModel)
                    }
                },
        )
    }
}

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
import dev.jordond.stateholder.demo.screens.dashboard.DashboardModel
import dev.jordond.stateholder.demo.screens.notifications.NotificationsScreen
import dev.jordond.stateholder.demo.screens.notifications.NotificationsModel
import dev.jordond.stateholder.demo.screens.profile.ProfileScreen
import dev.jordond.stateholder.demo.screens.profile.ProfileModel
import dev.jordond.stateholder.demo.screens.settings.SettingsScreen
import dev.jordond.stateholder.demo.screens.settings.SettingsModel

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
                                DashboardModel(
                                    stateProvider = DI.dashboardStateProvider,
                                    userRepository = DI.userRepository,
                                    taskRepository = DI.taskRepository,
                                )
                            }
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToSettings = { backStack.add(AppRoute.Settings) },
                            onNavigateToNotifications = { backStack.add(AppRoute.Notifications) },
                            onNavigateToProfile = { backStack.add(AppRoute.Profile) },
                        )
                    }
                    entry<AppRoute.Settings> {
                        val viewModel =
                            viewModel {
                                SettingsModel(
                                    stateProvider = DI.settingsStateProvider,
                                    settingsRepository = DI.settingsRepository,
                                )
                            }
                        SettingsScreen(
                            viewModel = viewModel,
                            onNavigateBack = { backStack.removeLastOrNull() },
                        )
                    }
                    entry<AppRoute.Notifications> {
                        val viewModel =
                            viewModel {
                                NotificationsModel(
                                    stateProvider = DI.notificationsStateProvider,
                                    notificationRepository = DI.notificationRepository,
                                )
                            }
                        NotificationsScreen(
                            viewModel = viewModel,
                            onNavigateBack = { backStack.removeLastOrNull() },
                        )
                    }
                    entry<AppRoute.Profile> {
                        val viewModel =
                            viewModel {
                                ProfileModel(
                                    stateProvider = DI.profileStateProvider,
                                    userRepository = DI.userRepository,
                                )
                            }
                        ProfileScreen(
                            viewModel = viewModel,
                            onNavigateBack = { backStack.removeLastOrNull() },
                        )
                    }
                },
        )
    }
}

package dev.jordond.stateholder.demo.di

import dev.jordond.stateholder.demo.data.NotificationRepository
import dev.jordond.stateholder.demo.data.SettingsRepository
import dev.jordond.stateholder.demo.data.TaskRepository
import dev.jordond.stateholder.demo.data.UserRepository
import dev.jordond.stateholder.demo.provider.UserStateProvider
import dev.jordond.stateholder.demo.screens.dashboard.DashboardState

object DI {
    val userRepository: UserRepository by lazy { UserRepository() }
    val taskRepository: TaskRepository by lazy { TaskRepository() }
    val notificationRepository: NotificationRepository by lazy { NotificationRepository() }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository() }

    val userStateProvider: UserStateProvider by lazy {
        UserStateProvider(userRepository)
    }

    val dashboardStateProvider: DashboardState.Provider by lazy {
        DashboardState.Provider(
            userStateProvider = userStateProvider,
            userRepository = userRepository,
            taskRepository = taskRepository,
            notificationRepository = notificationRepository,
            settingsRepository = settingsRepository,
        )
    }
}

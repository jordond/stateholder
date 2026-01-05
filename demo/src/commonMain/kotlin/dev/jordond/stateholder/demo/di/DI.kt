package dev.jordond.stateholder.demo.di

import dev.jordond.stateholder.demo.data.NotificationRepository
import dev.jordond.stateholder.demo.data.SettingsRepository
import dev.jordond.stateholder.demo.data.TaskRepository
import dev.jordond.stateholder.demo.data.UserRepository
import dev.jordond.stateholder.demo.provider.UserStateProvider
import dev.jordond.stateholder.demo.screens.dashboard.DashboardState
import dev.jordond.stateholder.demo.screens.notifications.NotificationsState
import dev.jordond.stateholder.demo.screens.profile.ProfileState
import dev.jordond.stateholder.demo.screens.settings.SettingsState

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

    val settingsStateProvider: SettingsState.Provider by lazy {
        SettingsState.Provider(settingsRepository)
    }

    val notificationsStateProvider: NotificationsState.Provider by lazy {
        NotificationsState.Provider(notificationRepository)
    }

    val profileStateProvider: ProfileState.Provider by lazy {
        ProfileState.Provider(
            userStateProvider = userStateProvider,
            userRepository = userRepository,
        )
    }
}

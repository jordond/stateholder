package dev.jordond.stateholder.demo.screens.dashboard

import androidx.compose.runtime.Immutable
import dev.jordond.stateholder.demo.data.AppSettings
import dev.jordond.stateholder.demo.data.NotificationRepository
import dev.jordond.stateholder.demo.data.SettingsRepository
import dev.jordond.stateholder.demo.data.Task
import dev.jordond.stateholder.demo.data.TaskRepository
import dev.jordond.stateholder.demo.data.User
import dev.jordond.stateholder.demo.data.UserRepository
import dev.jordond.stateholder.demo.provider.UserStateProvider
import dev.stateholder.provider.ComposedStateProvider
import dev.stateholder.provider.composedStateProvider
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

@Immutable
data class DashboardState(
    val user: User?,
    val isUserLoading: Boolean = false,
    val tasks: PersistentList<Task> = persistentListOf(),
    val completedTaskCount: Int = 0,
    val pendingTaskCount: Int = 0,
    val urgentTasks: PersistentList<Task> = persistentListOf(),
    val unreadNotificationCount: Int = 0,
    val settings: AppSettings = AppSettings(),
) {
    class Provider(
        userStateProvider: UserStateProvider,
        userRepository: UserRepository,
        taskRepository: TaskRepository,
        notificationRepository: NotificationRepository,
        settingsRepository: SettingsRepository,
    ) : ComposedStateProvider<DashboardState> by composedStateProvider(
        initialState = DashboardState(userRepository.currentUserOrNull()),
        composer = {
            // StateProvider
            userStateProvider into { copy(user = it) }

            // StateHolder
            settingsRepository into { copy(settings = it) }

            userRepository.isLoading into { copy(isUserLoading = it) }
            taskRepository.tasks into { copy(tasks = it.toPersistentList()) }
            taskRepository.completedCount into { copy(completedTaskCount = it) }
            taskRepository.pendingCount into { copy(pendingTaskCount = it) }
            taskRepository.urgentTasks into { copy(urgentTasks = it.toPersistentList()) }
            notificationRepository.unreadCount into { copy(unreadNotificationCount = it) }
        }
    )
}
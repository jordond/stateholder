package dev.jordond.stateholder.demo.screens.settings

import androidx.compose.runtime.Immutable
import dev.jordond.stateholder.demo.data.AppSettings
import dev.jordond.stateholder.demo.data.SettingsRepository
import dev.jordond.stateholder.demo.data.TaskSortOrder
import dev.stateholder.provider.ComposedStateProvider
import dev.stateholder.provider.composedStateProvider

@Immutable
data class SettingsState(
    val darkMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val taskSortOrder: TaskSortOrder = TaskSortOrder.CreatedAt,
) {
    class Provider(
        settingsRepository: SettingsRepository,
    ) : ComposedStateProvider<SettingsState> by composedStateProvider(
            initialState = settingsRepository.state.value.toSettingsState(),
            composer = {
                settingsRepository into { settings -> settings.toSettingsState() }
            },
        )
}

private fun AppSettings.toSettingsState(): SettingsState =
    SettingsState(
        darkMode = darkMode,
        notificationsEnabled = notificationsEnabled,
        taskSortOrder = taskSortOrder,
    )

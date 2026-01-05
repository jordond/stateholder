package dev.jordond.stateholder.demo.screens.settings

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import dev.jordond.stateholder.demo.data.SettingsRepository
import dev.jordond.stateholder.demo.data.TaskSortOrder
import dev.stateholder.extensions.viewmodel.UiStateViewModel

@Stable
class SettingsModel(
    stateProvider: SettingsState.Provider,
    private val settingsRepository: SettingsRepository,
) : UiStateViewModel<SettingsState, SettingsModel.Event>(stateProvider) {
    fun setDarkMode(enabled: Boolean) {
        settingsRepository.setDarkMode(enabled)
        emit(Event.ShowSnackbar(if (enabled) "Dark mode enabled" else "Dark mode disabled"))
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        settingsRepository.setNotificationsEnabled(enabled)
        emit(
            Event.ShowSnackbar(
                if (enabled) "Notifications enabled" else "Notifications disabled",
            ),
        )
    }

    fun setSortOrder(order: TaskSortOrder) {
        settingsRepository.setSortOrder(order)
        emit(Event.ShowSnackbar("Sort order changed to ${order.name}"))
    }

    @Immutable
    sealed interface Event {
        data class ShowSnackbar(
            val message: String,
        ) : Event
    }
}

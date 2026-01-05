package dev.jordond.stateholder.demo.data

import dev.stateholder.StateHolder
import dev.stateholder.stateContainer
import kotlinx.coroutines.flow.StateFlow

class SettingsRepository : StateHolder<AppSettings> {
    private val container = stateContainer(AppSettings())

    override val state: StateFlow<AppSettings> by container

    fun setDarkMode(enabled: Boolean) {
        container.update { it.copy(darkMode = enabled) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        container.update { it.copy(notificationsEnabled = enabled) }
    }

    fun setSortOrder(order: TaskSortOrder) {
        container.update { it.copy(taskSortOrder = order) }
    }
}

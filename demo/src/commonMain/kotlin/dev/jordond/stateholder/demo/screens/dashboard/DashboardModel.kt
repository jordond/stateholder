package dev.jordond.stateholder.demo.screens.dashboard

import cafe.adriel.voyager.core.model.screenModelScope
import dev.jordond.stateholder.demo.data.TaskPriority
import dev.jordond.stateholder.demo.data.TaskRepository
import dev.jordond.stateholder.demo.data.UserRepository
import dev.stateholder.extensions.voyager.UiStateScreenModel
import kotlinx.coroutines.launch

class DashboardModel(
    stateProvider: DashboardState.Provider,
    private val userRepository: UserRepository,
    private val taskRepository: TaskRepository,
) : UiStateScreenModel<DashboardState, DashboardEvent>(stateProvider) {
    fun login() {
        screenModelScope.launch {
            userRepository.login("demo@stateholder.dev")
            emit(DashboardEvent.ShowSnackbar("Welcome back!"))
        }
    }

    fun logout() {
        userRepository.logout()
        emit(DashboardEvent.ShowSnackbar("Logged out successfully"))
    }

    fun toggleTaskComplete(taskId: String) {
        screenModelScope.launch {
            taskRepository.toggleComplete(taskId)
        }
    }

    fun deleteTask(taskId: String) {
        screenModelScope.launch {
            taskRepository.deleteTask(taskId)
            emit(DashboardEvent.ShowSnackbar("Task deleted"))
        }
    }

    fun addTask(
        title: String,
        priority: TaskPriority,
    ) {
        if (title.isBlank()) {
            emit(DashboardEvent.ShowSnackbar("Task title cannot be empty"))
            return
        }
        screenModelScope.launch {
            taskRepository.addTask(title = title, priority = priority)
            emit(DashboardEvent.ShowSnackbar("Task added"))
            emit(DashboardEvent.TaskAdded)
        }
    }

    fun showAddTaskDialog() {
        emit(DashboardEvent.ShowAddTaskDialog)
    }
}

sealed interface DashboardEvent {
    data class ShowSnackbar(
        val message: String,
    ) : DashboardEvent

    data object ShowAddTaskDialog : DashboardEvent

    data object TaskAdded : DashboardEvent
}

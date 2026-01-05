package dev.jordond.stateholder.demo.screens.dashboard

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import dev.jordond.stateholder.demo.data.TaskPriority
import dev.jordond.stateholder.demo.data.TaskRepository
import dev.jordond.stateholder.demo.data.UserRepository
import dev.stateholder.extensions.viewmodel.UiStateViewModel
import kotlinx.coroutines.launch

@Stable
class DashboardModel(
    stateProvider: DashboardState.Provider,
    private val userRepository: UserRepository,
    private val taskRepository: TaskRepository,
) : UiStateViewModel<DashboardState, DashboardModel.Event>(stateProvider) {
    fun login() {
        viewModelScope.launch {
            userRepository.login("demo@stateholder.dev")
            emit(Event.ShowSnackbar("Welcome back!"))
        }
    }

    fun logout() {
        userRepository.logout()
        emit(Event.ShowSnackbar("Logged out successfully"))
    }

    fun toggleTaskComplete(taskId: String) {
        viewModelScope.launch {
            taskRepository.toggleComplete(taskId)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.deleteTask(taskId)
            emit(Event.ShowSnackbar("Task deleted: $taskId"))
        }
    }

    fun addTask(
        title: String,
        priority: TaskPriority,
    ) {
        if (title.isBlank()) {
            emit(Event.ShowSnackbar("Task title cannot be empty"))
            return
        }
        viewModelScope.launch {
            taskRepository.addTask(title = title, priority = priority)
            emit(Event.ShowSnackbar("Task added: $title"))
            emit(Event.TaskAdded)
        }
    }

    fun showAddTaskDialog() {
        emit(Event.ShowAddTaskDialog)
    }

    @Immutable
    sealed interface Event {
        data class ShowSnackbar(
            val message: String,
        ) : Event

        data object ShowAddTaskDialog : Event

        data object TaskAdded : Event
    }
}



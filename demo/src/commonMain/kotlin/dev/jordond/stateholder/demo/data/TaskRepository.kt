package dev.jordond.stateholder.demo.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class TaskRepository {
    private val _tasks = MutableStateFlow(sampleTasks())
    val tasks: Flow<List<Task>> = _tasks.asStateFlow()

    val completedCount: Flow<Int> = _tasks.map { list -> list.count { it.isCompleted } }
    val pendingCount: Flow<Int> = _tasks.map { list -> list.count { !it.isCompleted } }
    val urgentTasks: Flow<List<Task>> =
        _tasks.map { list ->
            list.filter { it.priority == TaskPriority.Urgent && !it.isCompleted }
        }

    suspend fun addTask(
        title: String,
        description: String = "",
        priority: TaskPriority = TaskPriority.Medium,
    ) {
        delay(300)
        val task =
            Task(
                id = Uuid.random().toString(),
                title = title,
                description = description,
                priority = priority,
            )
        _tasks.update { it + task }
    }

    suspend fun toggleComplete(taskId: String) {
        delay(100)
        _tasks.update { tasks ->
            tasks.map { task ->
                if (task.id == taskId) task.copy(isCompleted = !task.isCompleted) else task
            }
        }
    }

    suspend fun deleteTask(taskId: String) {
        delay(200)
        _tasks.update { tasks -> tasks.filter { it.id != taskId } }
    }

    suspend fun updatePriority(
        taskId: String,
        priority: TaskPriority,
    ) {
        delay(100)
        _tasks.update { tasks ->
            tasks.map { task ->
                if (task.id == taskId) task.copy(priority = priority) else task
            }
        }
    }

    private fun sampleTasks(): List<Task> =
        listOf(
            Task(
                id = "task-1",
                title = "Review StateHolder documentation",
                description = "Go through all new API documentation",
                priority = TaskPriority.High,
            ),
            Task(
                id = "task-2",
                title = "Implement FlowStateProvider",
                description = "Create a custom FlowStateProvider for the demo",
                priority = TaskPriority.Urgent,
            ),
            Task(
                id = "task-3",
                title = "Write unit tests",
                priority = TaskPriority.Medium,
                isCompleted = true,
            ),
            Task(
                id = "task-4",
                title = "Update README",
                description = "Add examples for new compose DSL",
                priority = TaskPriority.Low,
            ),
            Task(
                id = "task-5",
                title = "Fix critical bug in event handling",
                priority = TaskPriority.Urgent,
            ),
        )
}

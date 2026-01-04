package dev.jordond.stateholder.demo.data

import androidx.compose.runtime.Immutable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class User(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String? = null,
)

enum class TaskPriority {
    Low,
    Medium,
    High,
    Urgent,
}

@OptIn(ExperimentalUuidApi::class)
@Immutable
data class Task(
    val id: String = Uuid.random().toString(),
    val title: String,
    val description: String = "",
    val isCompleted: Boolean = false,
    val priority: TaskPriority = TaskPriority.Medium,
    val createdAt: Long = currentTimeMillis(),
    val dueDate: Long? = null,
)

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false,
)

data class AppSettings(
    val darkMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val taskSortOrder: TaskSortOrder = TaskSortOrder.CreatedAt,
)

enum class TaskSortOrder {
    CreatedAt,
    Priority,
    DueDate,
    Alphabetical,
}

expect fun currentTimeMillis(): Long

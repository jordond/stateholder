package dev.jordond.stateholder.demo.data

import dev.stateholder.StateHolder
import dev.stateholder.stateContainer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class NotificationRepository : StateHolder<List<Notification>> {
    private val container = stateContainer(sampleNotifications())
    override val state: StateFlow<List<Notification>> by container

    val unreadCount: Flow<Int> = container.state.map { list -> list.count { !it.isRead } }

    fun markAsRead(notificationId: String) {
        container.update { notifications ->
            notifications.map { notification ->
                if (notification.id == notificationId) notification.copy(isRead = true) else notification
            }
        }
    }

    fun markAllAsRead() {
        container.update { notifications ->
            notifications.map { it.copy(isRead = true) }
        }
    }

    fun addNotification(
        title: String,
        message: String,
    ) {
        val notification =
            Notification(
                id = Uuid.random().toString(),
                title = title,
                message = message,
                timestamp = currentTimeMillis(),
            )
        container.update { listOf(notification) + it }
    }

    fun clearAll() {
        container.update { emptyList() }
    }

    private fun sampleNotifications(): List<Notification> =
        listOf(
            Notification(
                id = "notif-1",
                title = "Welcome!",
                message = "Thanks for trying StateHolder demo",
                timestamp = currentTimeMillis() - 3600000,
            ),
            Notification(
                id = "notif-2",
                title = "New Feature",
                message = "StateComposer DSL is now available",
                timestamp = currentTimeMillis() - 1800000,
                isRead = true,
            ),
            Notification(
                id = "notif-3",
                title = "Reminder",
                message = "You have 2 urgent tasks pending",
                timestamp = currentTimeMillis() - 600000,
            ),
        )
}

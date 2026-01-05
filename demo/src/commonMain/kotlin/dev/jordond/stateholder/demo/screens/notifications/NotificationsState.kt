package dev.jordond.stateholder.demo.screens.notifications

import androidx.compose.runtime.Immutable
import dev.jordond.stateholder.demo.data.Notification
import dev.jordond.stateholder.demo.data.NotificationRepository
import dev.stateholder.provider.ComposedStateProvider
import dev.stateholder.provider.composedStateProvider
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

@Immutable
data class NotificationsState(
    val notifications: PersistentList<Notification> = persistentListOf(),
    val unreadCount: Int = 0,
) {
    val hasUnread: Boolean = unreadCount > 0
    val isEmpty: Boolean = notifications.isEmpty()

    class Provider(
        notificationRepository: NotificationRepository,
    ) : ComposedStateProvider<NotificationsState> by composedStateProvider(
        initialState =
            NotificationsState(
                notifications = notificationRepository.state.value.toPersistentList(),
                unreadCount = notificationRepository.state.value.count { !it.isRead },
            ),
        composer = {
            notificationRepository into { list ->
                NotificationsState(
                    notifications = list.toPersistentList(),
                    unreadCount = list.count { !it.isRead },
                )
            }
        },
    )
}

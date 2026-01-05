package dev.jordond.stateholder.demo.screens.notifications

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import dev.jordond.stateholder.demo.data.NotificationRepository
import dev.stateholder.extensions.viewmodel.UiStateViewModel

@Stable
class NotificationsModel(
    stateProvider: NotificationsState.Provider,
    private val notificationRepository: NotificationRepository,
) : UiStateViewModel<NotificationsState, NotificationsModel.Event>(stateProvider) {
    fun markAsRead(notificationId: String) {
        notificationRepository.markAsRead(notificationId)
    }

    fun markAllAsRead() {
        notificationRepository.markAllAsRead()
        emit(Event.ShowSnackbar("All notifications marked as read"))
    }

    fun clearAll() {
        notificationRepository.clearAll()
        emit(Event.ShowSnackbar("All notifications cleared"))
    }

    fun addTestNotification() {
        notificationRepository.addNotification(
            title = "Test Notification",
            message = "This is a test notification from the demo app",
        )
        emit(Event.ShowSnackbar("Test notification added"))
    }

    @Immutable
    sealed interface Event {
        data class ShowSnackbar(
            val message: String,
        ) : Event
    }
}

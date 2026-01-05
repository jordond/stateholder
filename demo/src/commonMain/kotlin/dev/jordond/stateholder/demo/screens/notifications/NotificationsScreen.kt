package dev.jordond.stateholder.demo.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.jordond.stateholder.demo.data.Notification
import dev.jordond.stateholder.demo.data.currentTimeMillis
import dev.jordond.stateholder.demo.screens.notifications.NotificationsModel.Event
import dev.stateholder.dispatcher.Dispatcher
import dev.stateholder.dispatcher.rememberDebounceDispatcher
import dev.stateholder.dispatcher.rememberRelay
import dev.stateholder.extensions.HandleEvents
import dev.stateholder.extensions.collectAsState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

@Composable
fun NotificationsScreen(
    viewModel: NotificationsModel,
    onNavigateBack: () -> Unit,
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    HandleEvents(viewModel) { event ->
        when (event) {
            is Event.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
        }
    }

    val dispatcher =
        rememberDebounceDispatcher<NotificationsAction>(debounce = 100L) { action ->
            when (action) {
                is NotificationsAction.MarkAsRead -> viewModel.markAsRead(action.notificationId)
                is NotificationsAction.MarkAllAsRead -> viewModel.markAllAsRead()
                is NotificationsAction.ClearAll -> viewModel.clearAll()
                is NotificationsAction.AddTestNotification -> viewModel.addTestNotification()
                is NotificationsAction.NavigateBack -> onNavigateBack()
            }
        }

    NotificationsContent(
        state = state,
        dispatcher = dispatcher,
        snackbarHostState = snackbarHostState,
    )
}

@Immutable
sealed interface NotificationsAction {
    data class MarkAsRead(
        val notificationId: String,
    ) : NotificationsAction

    data object MarkAllAsRead : NotificationsAction

    data object ClearAll : NotificationsAction

    data object AddTestNotification : NotificationsAction

    data object NavigateBack : NotificationsAction
}

@Composable
private fun NotificationsContent(
    state: NotificationsState,
    dispatcher: Dispatcher<NotificationsAction>,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Notifications")
                        if (state.hasUnread) {
                            Spacer(Modifier.width(8.dp))
                            UnreadBadge(count = state.unreadCount)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = dispatcher.rememberRelay(NotificationsAction.NavigateBack)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.hasUnread) {
                        IconButton(
                            onClick = dispatcher.rememberRelay(NotificationsAction.MarkAllAsRead),
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = "Mark all as read")
                        }
                    }
                    if (!state.isEmpty) {
                        IconButton(onClick = dispatcher.rememberRelay(NotificationsAction.ClearAll)) {
                            Icon(Icons.Default.ClearAll, contentDescription = "Clear all")
                        }
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = dispatcher.rememberRelay(NotificationsAction.AddTestNotification),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add test notification")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (state.isEmpty) {
            EmptyNotificationsContent(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {
                items(state.notifications, key = { it.id }) { notification ->
                    NotificationItem(
                        notification = notification,
                        onClick = {
                            if (!notification.isRead) {
                                dispatcher(NotificationsAction.MarkAsRead(notification.id))
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyNotificationsContent(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsNone,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No notifications",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Tap + to add a test notification",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun UnreadBadge(count: Int) {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.error),
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onError,
        )
    }
}

@Composable
private fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit,
) {
    Card(
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (notification.isRead) {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    },
            ),
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector =
                    if (notification.isRead) {
                        Icons.Default.NotificationsNone
                    } else {
                        Icons.Default.Notifications
                    },
                contentDescription = null,
                tint =
                    if (notification.isRead) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = formatRelativeTime(notification.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }
            if (!notification.isRead) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier =
                        Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                )
            }
        }
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val now = currentTimeMillis()
    val diff = now - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> "$days day${if (days > 1) "s" else ""} ago"
        hours > 0 -> "$hours hour${if (hours > 1) "s" else ""} ago"
        minutes > 0 -> "$minutes minute${if (minutes > 1) "s" else ""} ago"
        else -> "Just now"
    }
}

@Preview
@Composable
private fun NotificationsContentPreview() {
    val state =
        NotificationsState(
            notifications =
                listOf(
                    Notification(
                        id = "1",
                        title = "Welcome!",
                        message = "Thanks for trying StateHolder demo",
                        timestamp = currentTimeMillis() - 3600000,
                    ),
                    Notification(
                        id = "2",
                        title = "New Feature",
                        message = "StateComposer DSL is now available",
                        timestamp = currentTimeMillis() - 1800000,
                        isRead = true,
                    ),
                    Notification(
                        id = "3",
                        title = "Reminder",
                        message = "You have 2 urgent tasks pending",
                        timestamp = currentTimeMillis() - 600000,
                    ),
                ).toPersistentList(),
            unreadCount = 2,
        )

    MaterialTheme {
        NotificationsContent(
            state = state,
            dispatcher = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview
@Composable
private fun NotificationsContentEmptyPreview() {
    val state =
        NotificationsState(
            notifications = persistentListOf(),
            unreadCount = 0,
        )

    MaterialTheme {
        NotificationsContent(
            state = state,
            dispatcher = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview
@Composable
private fun NotificationItemUnreadPreview() {
    val notification =
        Notification(
            id = "1",
            title = "New Message",
            message = "You have a new message from John",
            timestamp = currentTimeMillis() - 300000,
        )

    MaterialTheme {
        NotificationItem(
            notification = notification,
            onClick = {},
        )
    }
}

@Preview
@Composable
private fun NotificationItemReadPreview() {
    val notification =
        Notification(
            id = "2",
            title = "Task Completed",
            message = "Your task 'Review documentation' has been completed",
            timestamp = currentTimeMillis() - 7200000,
            isRead = true,
        )

    MaterialTheme {
        NotificationItem(
            notification = notification,
            onClick = {},
        )
    }
}

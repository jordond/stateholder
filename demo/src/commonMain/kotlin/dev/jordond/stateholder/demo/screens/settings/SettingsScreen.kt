package dev.jordond.stateholder.demo.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.jordond.stateholder.demo.data.TaskSortOrder
import dev.jordond.stateholder.demo.screens.settings.SettingsModel.Event
import dev.stateholder.dispatcher.Dispatcher
import dev.stateholder.dispatcher.rememberDebounceDispatcher
import dev.stateholder.dispatcher.rememberRelay
import dev.stateholder.extensions.HandleEvents
import dev.stateholder.extensions.collectAsState

@Composable
fun SettingsScreen(
    viewModel: SettingsModel,
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
        rememberDebounceDispatcher<SettingsAction>(debounce = 100L) { action ->
            when (action) {
                is SettingsAction.SetDarkMode -> {
                    viewModel.setDarkMode(action.enabled)
                }

                is SettingsAction.SetNotificationsEnabled -> {
                    viewModel.setNotificationsEnabled(action.enabled)
                }

                is SettingsAction.SetSortOrder -> {
                    viewModel.setSortOrder(action.order)
                }

                is SettingsAction.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }

    SettingsContent(
        state = state,
        dispatcher = dispatcher,
        snackbarHostState = snackbarHostState,
    )
}

@Immutable
sealed interface SettingsAction {
    data class SetDarkMode(
        val enabled: Boolean,
    ) : SettingsAction

    data class SetNotificationsEnabled(
        val enabled: Boolean,
    ) : SettingsAction

    data class SetSortOrder(
        val order: TaskSortOrder,
    ) : SettingsAction

    data object NavigateBack : SettingsAction
}

@Composable
private fun SettingsContent(
    state: SettingsState,
    dispatcher: Dispatcher<SettingsAction>,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = dispatcher.rememberRelay(SettingsAction.NavigateBack)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            item {
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            item {
                SettingsSwitchItem(
                    title = "Dark Mode",
                    description = "Enable dark theme for the app",
                    icon = Icons.Default.DarkMode,
                    checked = state.darkMode,
                    onCheckedChange = { dispatcher(SettingsAction.SetDarkMode(it)) },
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            item {
                SettingsSwitchItem(
                    title = "Push Notifications",
                    description = "Receive notifications for task reminders",
                    icon = Icons.Default.Notifications,
                    checked = state.notificationsEnabled,
                    onCheckedChange = { dispatcher(SettingsAction.SetNotificationsEnabled(it)) },
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Tasks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            item {
                SortOrderDropdown(
                    currentOrder = state.taskSortOrder,
                    onOrderSelected = { dispatcher(SettingsAction.SetSortOrder(it)) },
                )
            }
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}

@Composable
private fun SortOrderDropdown(
    currentOrder: TaskSortOrder,
    onOrderSelected: (TaskSortOrder) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Sort,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            ) {
                Text(
                    text = "Task Sort Order",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "Choose how tasks are sorted in the list",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                ) {
                    OutlinedTextField(
                        value = currentOrder.displayName(),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        TaskSortOrder.entries.forEach { order ->
                            DropdownMenuItem(
                                text = { Text(order.displayName()) },
                                onClick = {
                                    onOrderSelected(order)
                                    expanded = false
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun TaskSortOrder.displayName(): String =
    when (this) {
        TaskSortOrder.CreatedAt -> "Created Date"
        TaskSortOrder.Priority -> "Priority"
        TaskSortOrder.DueDate -> "Due Date"
        TaskSortOrder.Alphabetical -> "Alphabetical"
    }

@Preview
@Composable
private fun SettingsContentPreview() {
    val state =
        SettingsState(
            darkMode = true,
            notificationsEnabled = true,
            taskSortOrder = TaskSortOrder.Priority,
        )

    MaterialTheme {
        SettingsContent(
            state = state,
            dispatcher = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview
@Composable
private fun SettingsSwitchItemPreview() {
    MaterialTheme {
        SettingsSwitchItem(
            title = "Dark Mode",
            description = "Enable dark theme for the app",
            icon = Icons.Default.DarkMode,
            checked = true,
            onCheckedChange = {},
        )
    }
}

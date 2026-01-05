package dev.jordond.stateholder.demo.screens.dashboard

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.jordond.stateholder.demo.data.Task
import dev.jordond.stateholder.demo.data.TaskPriority
import dev.jordond.stateholder.demo.data.User
import dev.stateholder.dispatcher.Dispatcher
import dev.stateholder.dispatcher.rememberDebounceDispatcher
import dev.stateholder.dispatcher.rememberRelay
import dev.stateholder.extensions.HandleEvents
import dev.stateholder.extensions.collectAsState
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }

    HandleEvents(viewModel) { event ->
        when (event) {
            is DashboardEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
            is DashboardEvent.ShowAddTaskDialog -> showAddDialog = true
            is DashboardEvent.TaskAdded -> showAddDialog = false
        }
    }

    val dispatcher =
        rememberDebounceDispatcher<DashboardAction>(debounce = 100L) { action ->
            when (action) {
                is DashboardAction.Login -> viewModel.login()
                is DashboardAction.Logout -> viewModel.logout()
                is DashboardAction.ToggleTask -> viewModel.toggleTaskComplete(action.taskId)
                is DashboardAction.DeleteTask -> viewModel.deleteTask(action.taskId)
                is DashboardAction.AddTask -> viewModel.addTask(action.title, action.priority)
                is DashboardAction.ShowAddTaskDialog -> viewModel.showAddTaskDialog()
            }
        }

    DashboardContent(
        state = state,
        dispatcher = dispatcher,
        snackbarHostState = snackbarHostState,
        showAddDialog = showAddDialog,
        onDismissDialog = { showAddDialog = false },
    )
}

@Immutable
sealed interface DashboardAction {
    data object Login : DashboardAction

    data object Logout : DashboardAction

    data class ToggleTask(
        val taskId: String,
    ) : DashboardAction

    data class DeleteTask(
        val taskId: String,
    ) : DashboardAction

    data class AddTask(
        val title: String,
        val priority: TaskPriority,
    ) : DashboardAction

    data object ShowAddTaskDialog : DashboardAction
}

@Composable
private fun DashboardContent(
    state: DashboardState,
    dispatcher: Dispatcher<DashboardAction>,
    snackbarHostState: SnackbarHostState,
    showAddDialog: Boolean,
    onDismissDialog: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("StateHolder Demo") },
                actions = {
                    UserSection(
                        user = state.user,
                        isLoading = state.isUserLoading,
                        onLogin = dispatcher.rememberRelay(DashboardAction.Login),
                        onLogout = dispatcher.rememberRelay(DashboardAction.Logout),
                    )
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = dispatcher.rememberRelay(DashboardAction.ShowAddTaskDialog)) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
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
                    text = "Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            item {
                StatsRow(
                    completedCount = state.completedTaskCount,
                    pendingCount = state.pendingTaskCount,
                    urgentCount = state.urgentTasks.size,
                )
            }

            if (state.urgentTasks.isNotEmpty()) {
                item {
                    UrgentTasksSection(
                        tasks = state.urgentTasks,
                        onToggle = { dispatcher(DashboardAction.ToggleTask(it)) },
                    )
                }
            }

            item {
                Text(
                    text = "All Tasks",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            items(state.tasks, key = { it.id }) { task ->
                TaskItem(
                    task = task,
                    onToggle = { dispatcher(DashboardAction.ToggleTask(task.id)) },
                    onDelete = { dispatcher(DashboardAction.DeleteTask(task.id)) },
                )
            }

            if (state.tasks.isEmpty()) {
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                    ) {
                        Text(
                            text = "No tasks yet. Add one!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTaskDialog(
            onDismiss = onDismissDialog,
            onConfirm = { title, priority ->
                dispatcher(DashboardAction.AddTask(title, priority))
            },
        )
    }
}

@Composable
private fun UserSection(
    user: User?,
    isLoading: Boolean,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }

            user != null -> {
                Icon(Icons.Default.Person, contentDescription = null)
                Text(user.name, style = MaterialTheme.typography.bodyMedium)
                IconButton(onClick = onLogout) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                }
            }

            else -> {
                IconButton(onClick = onLogin) {
                    Icon(Icons.AutoMirrored.Filled.Login, contentDescription = "Login")
                }
            }
        }
    }
}

@Composable
private fun StatsRow(
    completedCount: Int,
    pendingCount: Int,
    urgentCount: Int,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        StatsCard(
            title = "Completed",
            count = completedCount,
            icon = Icons.Default.CheckCircle,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
        StatsCard(
            title = "Pending",
            count = pendingCount,
            icon = Icons.Default.Pending,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f),
        )
        StatsCard(
            title = "Urgent",
            count = urgentCount,
            icon = Icons.Default.Warning,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatsCard(
    title: String,
    count: Int,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        modifier = modifier,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun UrgentTasksSection(
    tasks: PersistentList<Task>,
    onToggle: (String) -> Unit,
) {
    Column {
        Text(
            text = "Urgent Tasks",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.error,
        )
        Spacer(Modifier.height(8.dp))
        tasks.forEach { task ->
            Surface(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(12.dp),
                ) {
                    Checkbox(
                        checked = task.isCompleted,
                        onCheckedChange = { onToggle(task.id) },
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    PriorityBadge(task.priority)
                }
            }
        }
    }
}

@Composable
private fun TaskItem(
    task: Task,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp),
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            PriorityBadge(task.priority)
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun PriorityBadge(priority: TaskPriority) {
    val (color, text) =
        when (priority) {
            TaskPriority.Low -> MaterialTheme.colorScheme.outline to "Low"
            TaskPriority.Medium -> MaterialTheme.colorScheme.primary to "Med"
            TaskPriority.High -> MaterialTheme.colorScheme.tertiary to "High"
            TaskPriority.Urgent -> MaterialTheme.colorScheme.error to "Urgent"
        }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, TaskPriority) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(TaskPriority.Medium) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                ) {
                    OutlinedTextField(
                        value = priority.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Priority") },
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
                        TaskPriority.entries.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p.name) },
                                onClick = {
                                    priority = p
                                    expanded = false
                                },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, priority) },
                enabled = title.isNotBlank(),
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

private val sampleTasks =
    persistentListOf(
        Task(id = "1", title = "Review documentation", priority = TaskPriority.High),
        Task(
            id = "2",
            title = "Write unit tests",
            priority = TaskPriority.Medium,
            isCompleted = true,
        ),
        Task(id = "3", title = "Fix critical bug", priority = TaskPriority.Urgent),
        Task(id = "4", title = "Update README", priority = TaskPriority.Low),
    )

@Preview
@Composable
private fun DashboardContentPreview() {
    val state =
        DashboardState(
            user = User(id = "1", name = "Demo User", email = "demo@stateholder.dev"),
            tasks = sampleTasks,
            completedTaskCount = 1,
            pendingTaskCount = 3,
            urgentTasks =
                sampleTasks
                    .filter { it.priority == TaskPriority.Urgent }
                    .toPersistentList(),
        )

    MaterialTheme {
        DashboardContent(
            state = state,
            dispatcher = {},
            snackbarHostState = remember { SnackbarHostState() },
            showAddDialog = false,
            onDismissDialog = {},
        )
    }
}

@Preview
@Composable
private fun DashboardContentEmptyPreview() {
    val state =
        DashboardState(
            user = null,
            tasks = persistentListOf(),
            completedTaskCount = 0,
            pendingTaskCount = 0,
            urgentTasks = persistentListOf(),
        )

    MaterialTheme {
        DashboardContent(
            state = state,
            dispatcher = {},
            snackbarHostState = remember { SnackbarHostState() },
            showAddDialog = false,
            onDismissDialog = {},
        )
    }
}

@Preview
@Composable
private fun DashboardContentLoadingPreview() {
    val state =
        DashboardState(
            user = null,
            isUserLoading = true,
            tasks =
                persistentListOf(
                    Task(id = "1", title = "Sample task", priority = TaskPriority.Medium),
                ),
            completedTaskCount = 0,
            pendingTaskCount = 1,
            urgentTasks = persistentListOf(),
        )

    MaterialTheme {
        DashboardContent(
            state = state,
            dispatcher = {},
            snackbarHostState = remember { SnackbarHostState() },
            showAddDialog = false,
            onDismissDialog = {},
        )
    }
}

@Preview
@Composable
private fun AddTaskDialogPreview() {
    MaterialTheme {
        AddTaskDialog(
            onDismiss = {},
            onConfirm = { _, _ -> },
        )
    }
}

@Preview
@Composable
private fun TaskItemPreview() {
    val task =
        Task(
            id = "1",
            title = "Review StateHolder documentation",
            description = "Go through all new API documentation",
            priority = TaskPriority.High,
        )

    MaterialTheme {
        TaskItem(
            task = task,
            onToggle = {},
            onDelete = {},
        )
    }
}

@Preview
@Composable
private fun TaskItemCompletedPreview() {
    val task =
        Task(
            id = "1",
            title = "Write unit tests",
            description = "Add tests for new features",
            priority = TaskPriority.Medium,
            isCompleted = true,
        )

    MaterialTheme {
        TaskItem(
            task = task,
            onToggle = {},
            onDelete = {},
        )
    }
}

@Preview
@Composable
private fun StatsRowPreview() {
    MaterialTheme {
        StatsRow(
            completedCount = 5,
            pendingCount = 12,
            urgentCount = 3,
        )
    }
}

@Preview
@Composable
private fun PriorityBadgePreview() {
    MaterialTheme {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PriorityBadge(TaskPriority.Low)
            PriorityBadge(TaskPriority.Medium)
            PriorityBadge(TaskPriority.High)
            PriorityBadge(TaskPriority.Urgent)
        }
    }
}

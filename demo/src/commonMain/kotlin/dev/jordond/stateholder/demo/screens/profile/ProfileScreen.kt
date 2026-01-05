package dev.jordond.stateholder.demo.screens.profile

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.jordond.stateholder.demo.data.User
import dev.jordond.stateholder.demo.screens.profile.ProfileModel.Event
import dev.stateholder.dispatcher.Dispatcher
import dev.stateholder.dispatcher.rememberDebounceDispatcher
import dev.stateholder.dispatcher.rememberRelay
import dev.stateholder.extensions.HandleEvents
import dev.stateholder.extensions.collectAsState

@Composable
fun ProfileScreen(
    viewModel: ProfileModel,
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
        rememberDebounceDispatcher<ProfileAction>(debounce = 100L) { action ->
            when (action) {
                is ProfileAction.Login -> viewModel.login()
                is ProfileAction.Logout -> viewModel.logout()
                is ProfileAction.StartEditing -> viewModel.startEditing()
                is ProfileAction.CancelEditing -> viewModel.cancelEditing()
                is ProfileAction.UpdateName -> viewModel.updateName(action.name)
                is ProfileAction.UpdateEmail -> viewModel.updateEmail(action.email)
                is ProfileAction.SaveChanges -> viewModel.saveChanges()
                is ProfileAction.NavigateBack -> onNavigateBack()
            }
        }

    ProfileContent(
        state = state,
        dispatcher = dispatcher,
        snackbarHostState = snackbarHostState,
    )
}

@Immutable
sealed interface ProfileAction {
    data object Login : ProfileAction

    data object Logout : ProfileAction

    data object StartEditing : ProfileAction

    data object CancelEditing : ProfileAction

    data class UpdateName(
        val name: String,
    ) : ProfileAction

    data class UpdateEmail(
        val email: String,
    ) : ProfileAction

    data object SaveChanges : ProfileAction

    data object NavigateBack : ProfileAction
}

@Composable
private fun ProfileContent(
    state: ProfileState,
    dispatcher: Dispatcher<ProfileAction>,
    snackbarHostState: SnackbarHostState,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = dispatcher.rememberRelay(ProfileAction.NavigateBack)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.isLoggedIn) {
                        if (state.isEditing) {
                            IconButton(
                                onClick = dispatcher.rememberRelay(ProfileAction.CancelEditing),
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel editing")
                            }
                            IconButton(
                                onClick = dispatcher.rememberRelay(ProfileAction.SaveChanges),
                                enabled = state.hasChanges,
                            ) {
                                Icon(Icons.Default.Save, contentDescription = "Save changes")
                            }
                        } else {
                            IconButton(
                                onClick = dispatcher.rememberRelay(ProfileAction.StartEditing),
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit profile")
                            }
                            IconButton(onClick = dispatcher.rememberRelay(ProfileAction.Logout)) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Logout",
                                )
                            }
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        when {
            state.isLoading -> {
                LoadingContent(modifier = Modifier.padding(padding))
            }

            state.isLoggedIn -> {
                LoggedInContent(
                    state = state,
                    dispatcher = dispatcher,
                    modifier = Modifier.padding(padding),
                )
            }

            else -> {
                LoggedOutContent(
                    dispatcher = dispatcher,
                    modifier = Modifier.padding(padding),
                )
            }
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun LoggedOutContent(
    dispatcher: Dispatcher<ProfileAction>,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier =
                    Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(48.dp),
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Not logged in",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Log in to view and edit your profile",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = dispatcher.rememberRelay(ProfileAction.Login),
            ) {
                Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Log In")
            }
        }
    }
}

@Composable
private fun LoggedInContent(
    state: ProfileState,
    dispatcher: Dispatcher<ProfileAction>,
    modifier: Modifier = Modifier,
) {
    val user = state.user ?: return

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        item {
            ProfileHeader(user = user)
        }

        item {
            if (state.isEditing) {
                EditProfileForm(
                    name = state.editedName,
                    email = state.editedEmail,
                    onNameChange = { dispatcher(ProfileAction.UpdateName(it)) },
                    onEmailChange = { dispatcher(ProfileAction.UpdateEmail(it)) },
                )
            } else {
                ProfileInfoCard(user = user)
            }
        }

        if (state.isEditing) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedButton(
                        onClick = dispatcher.rememberRelay(ProfileAction.CancelEditing),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = dispatcher.rememberRelay(ProfileAction.SaveChanges),
                        enabled = state.hasChanges,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(user: User) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier =
                Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
        ) {
            Text(
                text = user.name.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = user.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = user.email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ProfileInfoCard(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "Profile Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(16.dp))
            ProfileInfoRow(
                icon = Icons.Default.Person,
                label = "Name",
                value = user.name,
            )
            Spacer(Modifier.height(12.dp))
            ProfileInfoRow(
                icon = Icons.Default.Email,
                label = "Email",
                value = user.email,
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.size(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
private fun EditProfileForm(
    name: String,
    email: String,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "Edit Profile",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Email cannot be changed in this demo",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
private fun ProfileContentLoggedOutPreview() {
    val state = ProfileState(user = null)

    MaterialTheme {
        ProfileContent(
            state = state,
            dispatcher = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview
@Composable
private fun ProfileContentLoggedInPreview() {
    val state =
        ProfileState(
            user = User(id = "1", name = "Demo User", email = "demo@stateholder.dev"),
            editedName = "Demo User",
            editedEmail = "demo@stateholder.dev",
        )

    MaterialTheme {
        ProfileContent(
            state = state,
            dispatcher = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview
@Composable
private fun ProfileContentEditingPreview() {
    val state =
        ProfileState(
            user = User(id = "1", name = "Demo User", email = "demo@stateholder.dev"),
            editedName = "Updated Name",
            editedEmail = "demo@stateholder.dev",
            isEditing = true,
        )

    MaterialTheme {
        ProfileContent(
            state = state,
            dispatcher = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

@Preview
@Composable
private fun ProfileContentLoadingPreview() {
    val state = ProfileState(isLoading = true)

    MaterialTheme {
        ProfileContent(
            state = state,
            dispatcher = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

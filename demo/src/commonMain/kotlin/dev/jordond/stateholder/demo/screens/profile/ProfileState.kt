package dev.jordond.stateholder.demo.screens.profile

import androidx.compose.runtime.Immutable
import dev.jordond.stateholder.demo.data.User
import dev.jordond.stateholder.demo.data.UserRepository
import dev.jordond.stateholder.demo.provider.UserStateProvider
import dev.stateholder.provider.ComposedStateProvider
import dev.stateholder.provider.composedStateProvider

@Immutable
data class ProfileState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val editedName: String = "",
    val editedEmail: String = "",
    val isEditing: Boolean = false,
) {
    val isLoggedIn: Boolean = user != null
    val hasChanges: Boolean =
        isEditing && user != null &&
                (editedName != user.name || editedEmail != user.email)

    class Provider(
        userStateProvider: UserStateProvider,
        userRepository: UserRepository,
    ) : ComposedStateProvider<ProfileState> by composedStateProvider(
        initialState = userRepository.currentUserOrNull().toProfileState(),
        composer = {
            userStateProvider into { user -> user.toProfileState() }
            userRepository.isLoading into { copy(isLoading = it) }
        },
    )
}

private fun User?.toProfileState(): ProfileState =
    ProfileState(
        user = this,
        editedName = this?.name.orEmpty(),
        editedEmail = this?.email.orEmpty(),
    )

package dev.jordond.stateholder.demo.screens.profile

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import dev.jordond.stateholder.demo.data.UserRepository
import dev.stateholder.extensions.viewmodel.UiStateViewModel
import kotlinx.coroutines.launch

@Stable
class ProfileModel(
    stateProvider: ProfileState.Provider,
    private val userRepository: UserRepository,
) : UiStateViewModel<ProfileState, ProfileModel.Event>(stateProvider) {
    fun login() {
        viewModelScope.launch {
            userRepository.login("demo@stateholder.dev")
            emit(Event.ShowSnackbar("Welcome back!"))
        }
    }

    fun logout() {
        userRepository.logout()
        updateState { it.copy(isEditing = false) }
        emit(Event.ShowSnackbar("Logged out successfully"))
    }

    fun startEditing() {
        val user = state.value.user ?: return
        updateState {
            it.copy(
                isEditing = true,
                editedName = user.name,
                editedEmail = user.email,
            )
        }
    }

    fun cancelEditing() {
        val user = state.value.user
        updateState {
            it.copy(
                isEditing = false,
                editedName = user?.name.orEmpty(),
                editedEmail = user?.email.orEmpty(),
            )
        }
    }

    fun updateName(name: String) {
        updateState { it.copy(editedName = name) }
    }

    fun updateEmail(email: String) {
        updateState { it.copy(editedEmail = email) }
    }

    fun saveChanges() {
        val currentState = state.value
        if (!currentState.hasChanges) {
            cancelEditing()
            return
        }

        // For demo purposes, we only update the name as that's what the repository supports
        userRepository.updateProfile(currentState.editedName)
        updateState { it.copy(isEditing = false) }
        emit(Event.ShowSnackbar("Profile updated successfully"))
    }

    @Immutable
    sealed interface Event {
        data class ShowSnackbar(
            val message: String,
        ) : Event
    }
}

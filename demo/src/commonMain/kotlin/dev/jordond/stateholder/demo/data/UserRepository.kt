package dev.jordond.stateholder.demo.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update

class UserRepository {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: Flow<User?> = _currentUser.asStateFlow()
    fun currentUserOrNull() = _currentUser.value

    private val _isLoading = MutableStateFlow(false)
    val isLoading: Flow<Boolean> = _isLoading.asStateFlow()

    suspend fun login(email: String) {
        _isLoading.value = true
        delay(1000)
        _currentUser.value =
            User(
                id = "user-1",
                name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                email = email,
            )
        _isLoading.value = false
    }

    fun logout() {
        _currentUser.value = null
    }

    fun updateProfile(name: String) {
        _currentUser.update { user ->
            user?.copy(name = name)
        }
    }
}

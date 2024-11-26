package com.mockingbird.sleeveup.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mockingbird.sleeveup.entity.User
import com.mockingbird.sleeveup.repository.UserRepository
import com.mockingbird.sleeveup.service.AuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authService: AuthService, private val userRepository: UserRepository
) : ViewModel() {

    private val _registrationState = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val registrationState: StateFlow<RegistrationState> = _registrationState.asStateFlow()

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                val newUser = User(
                    id = "",
                    name = name,
                    email = email,
                    password = password,
                    providerId = "password"
                )

                val firebaseUser = authService.register(newUser)

                if (firebaseUser != null) {
                    val userWithId = newUser.copy(id = firebaseUser.uid)
                    userRepository.saveUser(userWithId)
                    _registrationState.value = RegistrationState.Success(userWithId)
                } else {
                    _registrationState.value = RegistrationState.Error("Registration failed")
                }
            } catch (e: Exception) {
                _registrationState.value =
                    RegistrationState.Error(e.message ?: "Registration failed")
            } finally {
                if (_registrationState.value is RegistrationState.Loading) {
                    _registrationState.value = RegistrationState.Idle
                }
            }
        }
    }
}

sealed class RegistrationState {
    object Idle : RegistrationState()
    object Loading : RegistrationState()
    data class Success(val user: User) : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}
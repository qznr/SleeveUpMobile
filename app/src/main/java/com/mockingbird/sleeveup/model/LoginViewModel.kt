package com.mockingbird.sleeveup.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import com.mockingbird.sleeveup.service.AuthService
import kotlinx.coroutines.launch

class LoginViewModel (private val authService: AuthService) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _loginState.value = LoginState.Loading

                val task = authService.login(email, password)

                task.addOnCompleteListener { authResult ->
                    if (authResult.isSuccessful) {
                        _loginState.value = LoginState.Success(authResult.result?.user)
                    } else {

                        _loginState.value = LoginState.Error(authResult.exception?.message ?: "Unknown error")
                    }

                }

            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }


}

sealed class LoginState {
    object Loading : LoginState()
    data class Success(val user: FirebaseUser?) : LoginState()
    data class Error(val message: String) : LoginState()
}
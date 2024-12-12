package com.mockingbird.sleeveup.model

import android.app.Activity
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import com.mockingbird.sleeveup.entity.User
import com.mockingbird.sleeveup.navigation.Screen
import com.mockingbird.sleeveup.repository.UserRepository
import com.mockingbird.sleeveup.service.AuthService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class RegisterViewModel(
    private val authService: AuthService,
    private val userRepository: UserRepository,
    private val navController: NavController
) : ViewModel() {

    private val _registrationState =
        MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val registrationState: StateFlow<RegistrationState> = _registrationState.asStateFlow()

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _registrationState.value = RegistrationState.Loading
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
                    saveUser(userWithId, firebaseUser.email ?: "")
                    navController.navigate(Screen.Profile.createRoute(email))
                } else {
                    _registrationState.value = RegistrationState.Error("Registration failed")
                }

            } catch (e: Exception) {
                _registrationState.value =
                    RegistrationState.Error(e.message ?: "Registration failed")
            }
        }
    }

    private suspend fun saveUser(user: User, email : String) {
        try {
            userRepository.saveUser(user)
            _registrationState.value = RegistrationState.Success(user)
        } catch (e: Exception) {
            _registrationState.value =
                RegistrationState.Error(e.message ?: "An unknown error occurred")
        }
    }

    fun handleSignInResult(
        result: ActivityResult, onComplete: (FirebaseUser?) -> Unit
    ) {
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d("RegisterScreen", "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!, onComplete)
            } catch (e: ApiException) {
                Log.w("RegisterScreen", "Google sign in failed", e)
                onComplete(null)
            }
        } else {
            onComplete(null)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, onComplete: (FirebaseUser?) -> Unit) {
        viewModelScope.launch {
            _registrationState.value = RegistrationState.Loading
            try {
                val task = authService.firebaseAuthWithGoogle(idToken).await()
                val firebaseUser = task.user
                if (firebaseUser != null) {
                    Log.d("RegisterScreen", "signInWithCredential:success")
                    val user = User(
                        id = firebaseUser.uid,
                        name = firebaseUser.displayName ?: "",
                        email = firebaseUser.email ?: "",
                        providerId = "google"
                    )
                    saveUser(user, firebaseUser.email ?: "")
                    onComplete(firebaseUser)
                } else {
                    _registrationState.value =
                        RegistrationState.Error("Firebase auth failed after Google sign-in")
                    onComplete(null)
                }

            } catch (e: Exception) {
                Log.w("RegisterScreen", "signInWithCredential:failure", e)
                _registrationState.value =
                    RegistrationState.Error(e.message ?: "An unknown error occurred")
                onComplete(null)
            }
        }
    }

    sealed class RegistrationState {
        object Idle : RegistrationState()
        object Loading : RegistrationState()
        data class Success(val user: User) : RegistrationState()
        data class Error(val message: String) : RegistrationState()
    }
}
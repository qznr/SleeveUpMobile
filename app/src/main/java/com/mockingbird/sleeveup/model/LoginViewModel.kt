package com.mockingbird.sleeveup.model

import android.app.Activity
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import com.mockingbird.sleeveup.navigation.Screen
import com.mockingbird.sleeveup.service.AuthService
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

class LoginViewModel(
    private val authService: AuthService, private val navController: NavController
) : ViewModel() {

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
                        navController.navigate(Screen.Profile.createRoute(email))
                    } else {
                        _loginState.value =
                            LoginState.Error(authResult.exception?.message ?: "Unknown error")
                    }
                }

            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun handleSignInResult(
        result: ActivityResult, onComplete: (FirebaseUser?) -> Unit
    ) {
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d("LoginScreen", "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!, onComplete)
            } catch (e: ApiException) {
                Log.w("LoginScreen", "Google sign in failed", e)
                onComplete(null) // Important: Call onComplete with null on failure
            }
        } else {
            onComplete(null) // Also handle cases where the result is not OK
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, onComplete: (FirebaseUser?) -> Unit) {
        viewModelScope.launch {
            try {
                _loginState.value = LoginState.Loading
                val task = authService.firebaseAuthWithGoogle(idToken)
                task.addOnCompleteListener { result ->
                    if (result.isSuccessful) {
                        Log.d("LoginScreen", "signInWithCredential:success")
                        _loginState.value = LoginState.Success(result.result.user)
                        onComplete(FirebaseAuth.getInstance().currentUser)
                    } else {
                        Log.w("LoginScreen", "signInWithCredential:failure", result.exception)
                        _loginState.value = LoginState.Error(result.exception?.message ?: "Unknown Error")
                        onComplete(null)
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
package com.mockingbird.sleeveup.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.mockingbird.sleeveup.model.LoginViewModel
import com.mockingbird.sleeveup.service.AuthService

class LoginViewModelFactory(
    private val authService: AuthService,
    private val navController: NavController
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return LoginViewModel(authService, navController) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
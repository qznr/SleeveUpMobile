package com.mockingbird.sleeveup.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mockingbird.sleeveup.model.RegisterViewModel
import com.mockingbird.sleeveup.repository.UserRepository
import com.mockingbird.sleeveup.service.AuthService

class RegisterViewModelFactory(
    private val authService: AuthService,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(authService, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
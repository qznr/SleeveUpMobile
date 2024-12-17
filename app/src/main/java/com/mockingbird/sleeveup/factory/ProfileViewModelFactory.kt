package com.mockingbird.sleeveup.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mockingbird.sleeveup.model.ProfileViewModel
import com.mockingbird.sleeveup.repository.UserRepository
import com.mockingbird.sleeveup.service.StorageService

class ProfileViewModelFactory(
    private val userRepository: UserRepository,
    private val storageService: StorageService,
    private val userId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(userRepository, storageService, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
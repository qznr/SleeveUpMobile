package com.mockingbird.sleeveup.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.mockingbird.sleeveup.model.EditProfileViewModel
import com.mockingbird.sleeveup.repository.UserRepository
import com.mockingbird.sleeveup.service.StorageService

class EditProfileViewModelFactory(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val storageService: StorageService,
    private val navController: NavController
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditProfileViewModel(auth, userRepository, storageService, navController) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
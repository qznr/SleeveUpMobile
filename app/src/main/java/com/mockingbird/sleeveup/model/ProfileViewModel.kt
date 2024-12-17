package com.mockingbird.sleeveup.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mockingbird.sleeveup.entity.User
import com.mockingbird.sleeveup.repository.UserRepository
import com.mockingbird.sleeveup.service.StorageService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val storageService: StorageService,
    private val userId: String // Added userId to the constructor
) : ViewModel() {

    private val _userState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val userState: StateFlow<ProfileState> = _userState.asStateFlow()

    private val _imageState = MutableStateFlow<ImageState>(ImageState.Idle)
    val imageState: StateFlow<ImageState> = _imageState.asStateFlow()

    init {
        fetchUser()
    }
    private fun fetchUser() {
        viewModelScope.launch {
            _userState.value = ProfileState.Loading
            try {
                val user = userRepository.getUser(userId)
                _userState.value = ProfileState.Success(user)
                fetchProfileImage(user)
            } catch (e: Exception) {
                _userState.value = ProfileState.Error(e.message ?: "Failed to fetch user")
            }
        }
    }

    private fun fetchProfileImage(user: User) {
        viewModelScope.launch {
            _imageState.value = ImageState.Loading
            try {
                if (!user.photoUrl.isNullOrBlank()) {
                    val bytes = storageService.fetchImage(user.photoUrl!!)
                    _imageState.value = ImageState.Success(bytes)
                } else {
                    val bytes = storageService.fetchImage("placeholder.png")
                    _imageState.value = ImageState.Success(bytes)
                    Log.d("ProfileVM", "No profile picture for user ${user.id}")
                }
            } catch (e: Exception) {
                _imageState.value = ImageState.Error(e.message ?: "Failed to fetch profile image")
                Log.e("ProfileVM", "Error fetching profile image", e)
            }
        }
    }

    sealed class ProfileState {
        object Idle : ProfileState()
        object Loading : ProfileState()
        data class Success(val user: User) : ProfileState()
        data class Error(val message: String) : ProfileState()
    }

    sealed class ImageState {
        object Idle : ImageState()
        object Loading : ImageState()
        data class Success(val imageBytes: ByteArray?) : ImageState()
        data class Error(val message: String) : ImageState()
    }
}
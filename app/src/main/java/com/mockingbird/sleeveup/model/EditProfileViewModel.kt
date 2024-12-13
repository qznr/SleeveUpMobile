package com.mockingbird.sleeveup.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.mockingbird.sleeveup.entity.User
import com.mockingbird.sleeveup.repository.UserRepository
import com.mockingbird.sleeveup.service.StorageService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.ViewModelProvider
import com.mockingbird.sleeveup.entity.JobOffer
import com.mockingbird.sleeveup.model.EditProfileViewModel

class EditProfileViewModel(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val storageService: StorageService,
    private val navController: NavController
) : ViewModel() {
    private val _userState = MutableStateFlow<EditProfileState>(EditProfileState.Idle)
    val userState: StateFlow<EditProfileState> = _userState.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _pendingJobOfferStates =
        MutableStateFlow<MutableMap<String, PendingState>>(mutableMapOf())
    val pendingJobOfferStates: StateFlow<MutableMap<String, PendingState>> =
        _pendingJobOfferStates.asStateFlow()


    fun fetchUser(userId: String) {
        viewModelScope.launch {
            _userState.value = EditProfileState.Loading
            try {
                val user = userRepository.getUser(userId)
                _userState.value = EditProfileState.Success(user)
                initializePendingStates(user)
            } catch (e: Exception) {
                _userState.value = EditProfileState.Error(e.message ?: "Failed to fetch user")
            }
        }
    }

    private fun initializePendingStates(user: User) {
        val initialStates = user.pendingJobApplication?.mapValues {
            PendingState.Success(true)
        } ?: emptyMap()
        _pendingJobOfferStates.value = initialStates.toMutableMap()
    }


    fun updateUser(user: User) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Loading
            try {
                userRepository.updateUser(user)
                _updateState.value = UpdateState.Success(user)
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error(e.message ?: "Failed to update user")
            }
        }
    }

    fun updateJobApplication(user: User?, jobOfferId: String, jobOffer: JobOffer) {
        viewModelScope.launch {
            val currentStates = _pendingJobOfferStates.value.toMutableMap()
            currentStates[jobOfferId] = PendingState.Loading
            _pendingJobOfferStates.value = currentStates
            try {
                if (user != null) {
                    val updatedUser = user.copy(
                        pendingJobApplication = (user.pendingJobApplication
                            ?: emptyMap()) + (jobOfferId to jobOffer)
                    )
                    userRepository.updateUser(updatedUser)
                    _updateState.value = UpdateState.Success(updatedUser)
                    _pendingJobOfferStates.value[jobOfferId] = PendingState.Success(true)
                } else {
                    _pendingJobOfferStates.value[jobOfferId] =
                        PendingState.Error("User data not available")
                }

            } catch (e: Exception) {
                _pendingJobOfferStates.value[jobOfferId] =
                    PendingState.Error(e.message ?: "Failed to update job application")
            }
        }
    }

    fun removeJobApplication(user: User, jobOfferId: String) {
        viewModelScope.launch {
            val currentStates = _pendingJobOfferStates.value.toMutableMap()
            currentStates[jobOfferId] = PendingState.Loading
            _pendingJobOfferStates.value = currentStates

            try {
                val updatedPendingApps = user.pendingJobApplication?.toMutableMap()
                updatedPendingApps?.remove(jobOfferId)

                val updatedUser = user.copy(pendingJobApplication = updatedPendingApps)
                userRepository.updateUser(updatedUser)
                _updateState.value = UpdateState.Success(updatedUser)
                _pendingJobOfferStates.value[jobOfferId] = PendingState.Success(false)

            } catch (e: Exception) {
                _pendingJobOfferStates.value[jobOfferId] =
                    PendingState.Error(e.message ?: "Failed to remove job application")
            }
        }
    }

    sealed class EditProfileState {
        object Idle : EditProfileState()
        object Loading : EditProfileState()
        data class Success(val user: User) : EditProfileState()
        data class Error(val message: String) : EditProfileState()
    }

    sealed class UpdateState {
        object Idle : UpdateState()
        object Loading : UpdateState()
        data class Success(val user: User) : UpdateState()
        data class Error(val message: String) : UpdateState()
    }

    sealed class PendingState {
        object Idle : PendingState()
        object Loading : PendingState()
        data class Success(val isPending: Boolean) : PendingState()
        data class Error(val message: String) : PendingState()
    }
}
package com.mockingbird.sleeveup.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mockingbird.sleeveup.entity.Company
import com.mockingbird.sleeveup.entity.JobOffer
import com.mockingbird.sleeveup.entity.User
import com.mockingbird.sleeveup.repository.UserRepository
import com.mockingbird.sleeveup.retrofit.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class JobDetailsViewModel(
    private val apiService: ApiService,
    private val userRepository: UserRepository,
    private val jobId: String
) : ViewModel() {

    private val _jobOfferState = MutableStateFlow<JobOfferState>(JobOfferState.Idle)
    val jobOfferState: StateFlow<JobOfferState> = _jobOfferState.asStateFlow()

    private val _companyState = MutableStateFlow<CompanyState>(CompanyState.Idle)
    val companyState: StateFlow<CompanyState> = _companyState.asStateFlow()

    private val _pendingState = MutableStateFlow<PendingState>(PendingState.Idle)
    val pendingState: StateFlow<PendingState> = _pendingState.asStateFlow()

    init {
        fetchJobDetails()
    }

    private fun fetchJobDetails() {
        viewModelScope.launch {
            _jobOfferState.value = JobOfferState.Loading
            _companyState.value = CompanyState.Loading
            try {
                val fetchedJobOffer = apiService.getJobOfferById(jobId)
                if (fetchedJobOffer != null) {
                    _jobOfferState.value = JobOfferState.Success(fetchedJobOffer)
                    val fetchedCompany = apiService.getCompanyById(fetchedJobOffer.company_id)
                    _companyState.value = CompanyState.Success(fetchedCompany)
                }
            } catch (e: Exception) {
                _jobOfferState.value =
                    JobOfferState.Error(e.message ?: "Failed to fetch job offer")
                _companyState.value =
                    CompanyState.Error(e.message ?: "Failed to fetch company")
                Log.e("JobDetailsViewModel", "Error fetching job offer or company", e)
            }
        }
    }

    fun fetchUserPendingState(user: User?) {
        viewModelScope.launch {
            _pendingState.value = PendingState.Loading
            try {
                if(user != null) {
                    val isPending = user.pendingJobApplication?.contains(jobId)
                    _pendingState.value = PendingState.Success(isPending ?: false)
                } else {
                    _pendingState.value = PendingState.Error("User data not available")
                }

            } catch (e: Exception) {
                _pendingState.value = PendingState.Error("Failed to retrieve pending state")
            }
        }
    }


    fun applyJob(user: User?, jobOffer: JobOffer) {
        viewModelScope.launch {
            _pendingState.value = PendingState.Loading
            try {
                if (user != null) {
                    val updatedUser = user.copy(
                        pendingJobApplication = (user.pendingJobApplication
                            ?: emptyMap()) + (jobId to jobOffer)
                    )
                    userRepository.updateUser(updatedUser)
                    _pendingState.value = PendingState.Success(true)
                } else {
                    _pendingState.value = PendingState.Error("User data not available")
                }
            } catch (e: Exception) {
                _pendingState.value =
                    PendingState.Error(e.message ?: "Failed to apply job")
            }
        }
    }

    fun removeJobApplication(user: User) {
        viewModelScope.launch {
            _pendingState.value = PendingState.Loading
            try {
                val updatedPendingApps = user.pendingJobApplication?.toMutableMap()
                updatedPendingApps?.remove(jobId)

                val updatedUser = user.copy(pendingJobApplication = updatedPendingApps)
                userRepository.updateUser(updatedUser)
                _pendingState.value = PendingState.Success(false)

            } catch (e: Exception) {
                _pendingState.value =
                    PendingState.Error(e.message ?: "Failed to remove job application")
            }
        }
    }

    sealed class JobOfferState {
        object Idle : JobOfferState()
        object Loading : JobOfferState()
        data class Success(val jobOffer: JobOffer) : JobOfferState()
        data class Error(val message: String) : JobOfferState()
    }

    sealed class CompanyState {
        object Idle : CompanyState()
        object Loading : CompanyState()
        data class Success(val company: Company) : CompanyState()
        data class Error(val message: String) : CompanyState()
    }

    sealed class PendingState {
        object Idle : PendingState()
        object Loading : PendingState()
        data class Success(val isPending: Boolean) : PendingState()
        data class Error(val message: String) : PendingState()
    }
}
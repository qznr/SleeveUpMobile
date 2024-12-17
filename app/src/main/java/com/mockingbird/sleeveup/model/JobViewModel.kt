package com.mockingbird.sleeveup.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mockingbird.sleeveup.entity.JobOffer
import com.mockingbird.sleeveup.retrofit.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class JobViewModel(private val apiService: ApiService) : ViewModel() {

    private val _jobOffersState = MutableStateFlow<JobOffersState>(JobOffersState.Idle)
    val jobOffersState: StateFlow<JobOffersState> = _jobOffersState.asStateFlow()

    init {
        fetchJobOffers()
    }
    private fun fetchJobOffers() {
        viewModelScope.launch {
            _jobOffersState.value = JobOffersState.Loading
            try {
                val jobOffers = apiService.getJobOffers()
                _jobOffersState.value = JobOffersState.Success(jobOffers)
            } catch (e: Exception) {
                _jobOffersState.value = JobOffersState.Error(e.message ?: "Failed to fetch job offers")
                Log.e("JobViewModel", "Error fetching job offers", e)
            }
        }
    }

    sealed class JobOffersState {
        object Idle : JobOffersState()
        object Loading : JobOffersState()
        data class Success(val jobOffers: Map<String, JobOffer>) : JobOffersState()
        data class Error(val message: String) : JobOffersState()
    }
}
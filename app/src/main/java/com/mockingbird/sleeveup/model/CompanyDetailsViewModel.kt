package com.mockingbird.sleeveup.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mockingbird.sleeveup.entity.Company
import com.mockingbird.sleeveup.entity.JobOffer
import com.mockingbird.sleeveup.retrofit.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CompanyDetailsViewModel(private val apiService: ApiService, private val companyId: String) : ViewModel() {

    private val _companyState = MutableStateFlow<CompanyState>(CompanyState.Idle)
    val companyState: StateFlow<CompanyState> = _companyState.asStateFlow()

    private val _jobOffersState = MutableStateFlow<JobOffersState>(JobOffersState.Idle)
    val jobOffersState: StateFlow<JobOffersState> = _jobOffersState.asStateFlow()

    init {
        fetchCompanyDetails()
    }

    private fun fetchCompanyDetails() {
        viewModelScope.launch {
            _companyState.value = CompanyState.Loading
            _jobOffersState.value = JobOffersState.Loading
            try {
                val fetchedCompany = apiService.getCompanyById(companyId)
                // Wrap the companyId in quotes
                val fetchedJobOffers = apiService.getJobOffersByCompanyId(companyId = "\"$companyId\"")
                _companyState.value = CompanyState.Success(fetchedCompany)
                _jobOffersState.value = JobOffersState.Success(fetchedJobOffers)
            } catch (e: Exception) {
                _companyState.value = CompanyState.Error(e.message ?: "Failed to fetch company")
                _jobOffersState.value = JobOffersState.Error(e.message ?: "Failed to fetch job offers")
                Log.e("CompanyDetailsVM", "Error fetching company or job offers", e)
            }
        }
    }

    sealed class CompanyState {
        object Idle : CompanyState()
        object Loading : CompanyState()
        data class Success(val company: Company) : CompanyState()
        data class Error(val message: String) : CompanyState()
    }

    sealed class JobOffersState {
        object Idle : JobOffersState()
        object Loading : JobOffersState()
        data class Success(val jobOffers: Map<String, JobOffer>) : JobOffersState()
        data class Error(val message: String) : JobOffersState()
    }
}
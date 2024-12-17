package com.mockingbird.sleeveup.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mockingbird.sleeveup.entity.Company
import com.mockingbird.sleeveup.retrofit.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CompanyViewModel(private val apiService: ApiService) : ViewModel() {

    private val _companiesState = MutableStateFlow<CompaniesState>(CompaniesState.Idle)
    val companiesState: StateFlow<CompaniesState> = _companiesState.asStateFlow()

    init {
        fetchCompanies()
    }

    private fun fetchCompanies() {
        viewModelScope.launch {
            _companiesState.value = CompaniesState.Loading
            try {
                val companies = apiService.getCompanies()
                _companiesState.value = CompaniesState.Success(companies)
            } catch (e: Exception) {
                _companiesState.value =
                    CompaniesState.Error(e.message ?: "Failed to fetch companies")
                Log.e("CompanyViewModel", "Error fetching companies", e)
            }
        }
    }

    sealed class CompaniesState {
        object Idle : CompaniesState()
        object Loading : CompaniesState()
        data class Success(val companies: Map<String, Company>) : CompaniesState()
        data class Error(val message: String) : CompaniesState()
    }
}
package com.mockingbird.sleeveup.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mockingbird.sleeveup.model.CompanyDetailsViewModel
import com.mockingbird.sleeveup.retrofit.ApiService

class CompanyDetailsViewModelFactory(private val apiService: ApiService, private val companyId: String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CompanyDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CompanyDetailsViewModel(apiService, companyId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
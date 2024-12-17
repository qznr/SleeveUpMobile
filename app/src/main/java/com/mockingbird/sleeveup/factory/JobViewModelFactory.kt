package com.mockingbird.sleeveup.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mockingbird.sleeveup.model.JobViewModel
import com.mockingbird.sleeveup.retrofit.ApiService

class JobViewModelFactory(private val apiService: ApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JobViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JobViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
package com.mockingbird.sleeveup.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mockingbird.sleeveup.model.JobDetailsViewModel
import com.mockingbird.sleeveup.repository.UserRepository
import com.mockingbird.sleeveup.retrofit.ApiService

class JobDetailsViewModelFactory(
    private val apiService: ApiService,
    private val userRepository: UserRepository,
    private val jobId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JobDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return JobDetailsViewModel(apiService, userRepository, jobId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
package com.mockingbird.sleeveup.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mockingbird.sleeveup.model.EventDetailsViewModel
import com.mockingbird.sleeveup.retrofit.ApiService

class EventDetailsViewModelFactory(
    private val apiService: ApiService,
    private val eventId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventDetailsViewModel(apiService, eventId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
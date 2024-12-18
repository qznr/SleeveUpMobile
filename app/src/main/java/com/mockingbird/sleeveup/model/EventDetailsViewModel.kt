package com.mockingbird.sleeveup.model

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mockingbird.sleeveup.entity.Event
import com.mockingbird.sleeveup.retrofit.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EventDetailsViewModel(private val apiService: ApiService, private val eventId: String) :
    ViewModel() {

    private val _eventState = MutableStateFlow<EventState>(EventState.Idle)
    val eventState: StateFlow<EventState> = _eventState.asStateFlow()

    init {
        fetchEventDetails()
    }

    private fun fetchEventDetails() {
        viewModelScope.launch {
            _eventState.value = EventState.Loading
            try {
                val fetchedEvent = apiService.getEventById(eventId)
                _eventState.value = EventState.Success(fetchedEvent)
            } catch (e: Exception) {
                _eventState.value = EventState.Error(e.message ?: "Failed to fetch event")
                Log.e("EventDetailsViewModel", "Error fetching event", e)
            }
        }
    }

    sealed class EventState {
        object Idle : EventState()
        object Loading : EventState()
        data class Success(val event: Event) : EventState()
        data class Error(val message: String) : EventState()
    }
}
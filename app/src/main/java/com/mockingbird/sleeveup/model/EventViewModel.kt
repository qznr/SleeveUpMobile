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

class EventViewModel(private val apiService: ApiService) : ViewModel() {

    private val _eventsState = MutableStateFlow<EventsState>(EventsState.Idle)
    val eventsState: StateFlow<EventsState> = _eventsState.asStateFlow()

    init {
        fetchEvents()
    }

    private fun fetchEvents() {
        viewModelScope.launch {
            _eventsState.value = EventsState.Loading
            try {
                val events = apiService.getEvents()
                _eventsState.value = EventsState.Success(events)
            } catch (e: Exception) {
                _eventsState.value = EventsState.Error(e.message ?: "Failed to fetch events")
                Log.e("EventViewModel", "Error fetching events", e)
            }
        }
    }

    sealed class EventsState {
        object Idle : EventsState()
        object Loading : EventsState()
        data class Success(val events: Map<String, Event>) : EventsState()
        data class Error(val message: String) : EventsState()
    }
}
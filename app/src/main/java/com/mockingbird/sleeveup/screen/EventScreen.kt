// app/src/main/java/com/mockingbird/sleeveup/screen/EventScreen.kt
package com.mockingbird.sleeveup.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mockingbird.sleeveup.entity.Event
import com.mockingbird.sleeveup.factory.EventViewModelFactory
import com.mockingbird.sleeveup.model.EventViewModel
import com.mockingbird.sleeveup.retrofit.ApiConfig

@Composable
fun EventScreen(navController: NavController) {
    val apiService = ApiConfig.getApiService()
    val viewModelFactory = EventViewModelFactory(apiService)
    val viewModel: EventViewModel = viewModel(factory = viewModelFactory)

    val eventsState by viewModel.eventsState.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        when (eventsState) {
            is EventViewModel.EventsState.Loading -> {
                CircularProgressIndicator()
            }
            is EventViewModel.EventsState.Success -> {
                val events = (eventsState as EventViewModel.EventsState.Success).events
                EventList(events = events, navController = navController)
            }
            is EventViewModel.EventsState.Error -> {
                val errorMessage = (eventsState as EventViewModel.EventsState.Error).message
                Text(text = "Error: $errorMessage")
            }
            else -> {
                Text(text = "Waiting for Initial State...")
            }
        }
    }
}

@Composable
fun EventList(events: Map<String, Event>, navController: NavController) {
    LazyColumn(contentPadding = PaddingValues(all = 8.dp)) {
        items(events.toList()) { (eventId, event) ->
            EventCard(event = event, eventId = eventId, navController = navController)
        }
    }
}
package com.mockingbird.sleeveup.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mockingbird.sleeveup.entity.Event
import com.mockingbird.sleeveup.factory.EventDetailsViewModelFactory
import com.mockingbird.sleeveup.model.EventDetailsViewModel
import com.mockingbird.sleeveup.retrofit.ApiConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(navController: NavController, eventId: String) {
    val apiService = ApiConfig.getApiService()
    val viewModelFactory = EventDetailsViewModelFactory(apiService, eventId)
    val viewModel: EventDetailsViewModel = viewModel(factory = viewModelFactory)

    val eventState by viewModel.eventState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "backIcon")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (eventState) {
                is EventDetailsViewModel.EventState.Loading -> {
                    CircularProgressIndicator()
                }
                is EventDetailsViewModel.EventState.Success -> {
                    val event =
                        (eventState as EventDetailsViewModel.EventState.Success).event
                    EventDetails(event = event)
                }
                is EventDetailsViewModel.EventState.Error -> {
                    val errorMessage =
                        (eventState as EventDetailsViewModel.EventState.Error).message
                    Text("Error fetching event details: $errorMessage")
                }
                else -> {
                    Text("Waiting for event details...")
                }
            }
        }
    }
}

@Composable
fun EventDetails(event: Event) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(text = event.name, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = event.description, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Date: ${event.date}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Time: ${event.time}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Location: ${event.location}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Organizer: ${event.eventOrganizer}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Presenter: ${event.presenter}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Materials: ${event.materials}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Event type: ${event.eventType}", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Button(onClick = { /* TODO open link in browser */ }) {
            Text(text = "Register: ${event.registerLink}")
        }

    }
}
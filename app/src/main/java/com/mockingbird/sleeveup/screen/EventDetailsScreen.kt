package com.mockingbird.sleeveup.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.mockingbird.sleeveup.R
import com.mockingbird.sleeveup.entity.Event
import com.mockingbird.sleeveup.factory.EventDetailsViewModelFactory
import com.mockingbird.sleeveup.model.EventDetailsViewModel
import com.mockingbird.sleeveup.retrofit.ApiConfig
import com.mockingbird.sleeveup.ui.theme.AlmostBlack
import com.mockingbird.sleeveup.ui.theme.MajorelieBlue
import com.mockingbird.sleeveup.ui.theme.White

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
                title = { Text("Detail Event", color = White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "backIcon", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AlmostBlack)
            )
        },
        containerColor = AlmostBlack
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
                    CircularProgressIndicator(color = White)
                }
                is EventDetailsViewModel.EventState.Success -> {
                    val event =
                        (eventState as EventDetailsViewModel.EventState.Success).event
                    EventDetailsContent(event = event)
                }
                is EventDetailsViewModel.EventState.Error -> {
                    val errorMessage =
                        (eventState as EventDetailsViewModel.EventState.Error).message
                    Text("Error fetching event details: $errorMessage", color = White)
                }
                else -> {
                    Text("Waiting for event details...", color = White)
                }
            }
        }
    }
}

@Composable
fun EventDetailsContent(event: Event) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Placeholder profile picture",
            modifier = Modifier
                .size(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Gray),
            tint = White
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { /* TODO open link in browser */ },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MajorelieBlue)
        ) {
            Text("Registrasi Sekarang", color = White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = event.name,
            style = MaterialTheme.typography.headlineSmall,
            color = MajorelieBlue,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "By ${event.eventOrganizer}",
            style = MaterialTheme.typography.bodySmall,
            color = Color.LightGray,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))
        Column(horizontalAlignment = Alignment.Start){
            if (event.presenter.isNotBlank()) {
                Text(
                    text = "Presenter: ${event.presenter}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = White,
                    modifier = Modifier.fillMaxWidth(),

                    )
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (event.description.isNotBlank()) {
                Text(
                    text = "Deskripsi: ${event.description}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = White,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            if(event.materials.isNotBlank()){
                Text(
                    text = "Materials: ${event.materials}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = White,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row{
                Icon(
                    painter = painterResource(id = R.drawable.baseline_calendar_24),
                    contentDescription = "Date",
                    modifier = Modifier.size(20.dp),
                    tint = White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Date: ${event.date}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = White,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row{
                Icon(
                    painter = painterResource(id = R.drawable.baseline_schedule_24),
                    contentDescription = "Time",
                    modifier = Modifier.size(20.dp),
                    tint = White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Time: ${event.time}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = White,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row{
                Icon(
                    painter = painterResource(id = R.drawable.baseline_location_on_24),
                    contentDescription = "Location",
                    modifier = Modifier.size(20.dp),
                    tint = White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Location: ${event.location}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = White
                )
            }
        }
    }
}
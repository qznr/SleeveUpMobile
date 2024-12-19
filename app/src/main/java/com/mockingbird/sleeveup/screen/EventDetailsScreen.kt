package com.mockingbird.sleeveup.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.mockingbird.sleeveup.navigation.Screen
import com.mockingbird.sleeveup.retrofit.ApiConfig
import com.mockingbird.sleeveup.ui.theme.AlmostBlack
import com.mockingbird.sleeveup.ui.theme.MajorelieBlue
import com.mockingbird.sleeveup.ui.theme.Moonstone
import com.mockingbird.sleeveup.ui.theme.TickleMePink
import com.mockingbird.sleeveup.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreen(navController: NavController, eventId: String) {
    val apiService = ApiConfig.getApiService()
    val viewModelFactory = EventDetailsViewModelFactory(apiService, eventId)
    val viewModel: EventDetailsViewModel = viewModel(factory = viewModelFactory)

    val eventState by viewModel.eventState.collectAsState()

    Surface(
        color = AlmostBlack,
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, "backIcon", tint = White)
                }
                Text(
                    text = "Detail Event",
                    style = MaterialTheme.typography.headlineSmall,
                    color = White
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
}

@Composable
fun EventDetailsContent(event: Event) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
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
            onClick = {
                // Open registration link in browser
                openUrl(context, event.registerLink)
            },
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
            fontWeight = FontWeight.Bold,
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
            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (event.presenter.isNotBlank()) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_person_24),
                        contentDescription = "Presenter",
                        tint = Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = event.presenter,
                        style = MaterialTheme.typography.bodyMedium,
                        color = White,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (event.description.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, color = Moonstone, shape = MaterialTheme.shapes.medium), // Border putih
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            if(event.materials.isNotBlank()){
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, color = Moonstone, shape = MaterialTheme.shapes.medium), // Border putih
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Text(
                        text = "Materials: ${event.materials}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = White,
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row{
                Icon(
                    painter = painterResource(id = R.drawable.baseline_calendar_24),
                    contentDescription = "Date",
                    modifier = Modifier.size(20.dp),
                    tint = Moonstone
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
                    tint = Moonstone
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
                    tint = Moonstone
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Location: ${event.location}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = White
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        Text(
            text = "Sosial Media",
            style = MaterialTheme.typography.headlineSmall,
            color = TickleMePink,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = {
                        // Open instagram link in browser
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.instagram),
                        contentDescription = "Instagram",
                        tint = White
                    )
                }
                IconButton(
                    onClick = {
                        // Open facebook link in browser
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.facebook),
                        contentDescription = "Facebook",
                        tint = White
                    )
                }
                IconButton(
                    onClick = {
                        // Open website link in browser
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.website),
                        contentDescription = "Website",
                        tint = White
                    )
                }
            }
        }
    }
}


private fun openUrl(context: android.content.Context, url: String) {
    if (url.isNotBlank()) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            //TODO show toast error when url not valid
        }
    }
}
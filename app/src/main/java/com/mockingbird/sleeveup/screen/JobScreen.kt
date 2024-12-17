package com.mockingbird.sleeveup.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import com.mockingbird.sleeveup.entity.JobOffer
import com.mockingbird.sleeveup.factory.JobViewModelFactory
import com.mockingbird.sleeveup.model.JobViewModel
import com.mockingbird.sleeveup.retrofit.ApiConfig
import com.mockingbird.sleeveup.navigation.Screen

@Composable
fun JobScreen(navController: NavController) {
    val apiService = ApiConfig.getApiService()
    val viewModelFactory = JobViewModelFactory(apiService)
    val viewModel: JobViewModel = viewModel(factory = viewModelFactory)

    val jobOffersState by viewModel.jobOffersState.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        when (jobOffersState) {
            is JobViewModel.JobOffersState.Loading -> {
                CircularProgressIndicator()
            }
            is JobViewModel.JobOffersState.Success -> {
                val jobOffers = (jobOffersState as JobViewModel.JobOffersState.Success).jobOffers
                JobList(jobOffers = jobOffers, navController = navController)
            }
            is JobViewModel.JobOffersState.Error -> {
                val errorMessage = (jobOffersState as JobViewModel.JobOffersState.Error).message
                Text(text = "Error: $errorMessage")
            }
            else -> {
                // Initial State
                Text(text = "Waiting for initial State...")
            }
        }
    }

}

@Composable
fun JobList(jobOffers: Map<String, JobOffer>, navController: NavController) {
    LazyColumn(contentPadding = PaddingValues(all = 8.dp)) {
        items(jobOffers.toList()) { (jobOfferId, jobOffer) ->
            JobCard(jobOffer = jobOffer, jobOfferId = jobOfferId, navController = navController)
        }
    }
}


@Composable
fun JobCard(jobOffer: JobOffer, jobOfferId: String, navController: NavController) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = jobOffer.profession,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = jobOffer.description,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            androidx.compose.material3.Button(onClick = {
                navController.navigate(Screen.JobDetails.createRoute(jobOfferId))
            }) {
                Text(text = "See job details")
            }
        }
    }
}
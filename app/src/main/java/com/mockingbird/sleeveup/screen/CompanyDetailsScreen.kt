package com.mockingbird.sleeveup.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.mockingbird.sleeveup.entity.Company
import com.mockingbird.sleeveup.entity.JobOffer
import com.mockingbird.sleeveup.factory.CompanyDetailsViewModelFactory
import com.mockingbird.sleeveup.model.CompanyDetailsViewModel
import com.mockingbird.sleeveup.retrofit.ApiConfig


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyDetailsScreen(navController: NavController, companyId: String) {
    val apiService = ApiConfig.getApiService()
    val viewModelFactory = CompanyDetailsViewModelFactory(apiService, companyId)
    val viewModel: CompanyDetailsViewModel = viewModel(factory = viewModelFactory)

    val companyState by viewModel.companyState.collectAsState()
    val jobOffersState by viewModel.jobOffersState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Company Details") },
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
            when (companyState) {
                is CompanyDetailsViewModel.CompanyState.Loading -> {
                    CircularProgressIndicator()
                }
                is CompanyDetailsViewModel.CompanyState.Success -> {
                    val company = (companyState as CompanyDetailsViewModel.CompanyState.Success).company
                    when (jobOffersState) {
                        is CompanyDetailsViewModel.JobOffersState.Loading -> {
                            CircularProgressIndicator()
                        }
                        is CompanyDetailsViewModel.JobOffersState.Success -> {
                            val jobOffers =
                                (jobOffersState as CompanyDetailsViewModel.JobOffersState.Success).jobOffers
                            CompanyDetails(
                                company = company,
                                jobOffers = jobOffers,
                                navController = navController
                            )
                        }
                        is CompanyDetailsViewModel.JobOffersState.Error -> {
                            val errorMessage =
                                (jobOffersState as CompanyDetailsViewModel.JobOffersState.Error).message
                            Text("Error fetching job offers: $errorMessage")
                        }
                        else -> {
                            Text("Waiting for job offers")
                        }
                    }
                }
                is CompanyDetailsViewModel.CompanyState.Error -> {
                    val errorMessage =
                        (companyState as CompanyDetailsViewModel.CompanyState.Error).message
                    Text("Error fetching company details: $errorMessage")
                }
                else -> {
                    Text("Waiting for company details...")
                }
            }
        }
    }
}

@Composable
fun CompanyDetails(
    company: Company,
    jobOffers: Map<String, JobOffer>,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // First Row: Image and Basic Company Info
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
//            AsyncImage(
//                model = ImageRequest.Builder(LocalContext.current)
//                    .data(company.logoUrl)
//                    .crossfade(true)
//                    .build(),
//                contentDescription = "Company Logo",
//                contentScale = ContentScale.Crop,
//                modifier = Modifier.size(100.dp)
//            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = company.name,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Email: ${company.email}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Number: ${company.number}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Address: ${company.address}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Second Row: Description
        Text(
            text = company.description,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Third Row: Job Offers
        if (jobOffers.isEmpty()) {
            Text("No job offers from this company.")
        } else {
            jobOffers.forEach { (jobOfferId, jobOffer) ->
                JobOfferCard(jobOfferId = jobOfferId, jobOffer = jobOffer, navController = navController)
            }
        }
    }
}
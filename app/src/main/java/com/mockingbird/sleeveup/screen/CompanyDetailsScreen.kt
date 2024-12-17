package com.mockingbird.sleeveup.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.mockingbird.sleeveup.entity.Company
import com.mockingbird.sleeveup.entity.JobOffer
import com.mockingbird.sleeveup.factory.CompanyDetailsViewModelFactory
import com.mockingbird.sleeveup.model.CompanyDetailsViewModel
import com.mockingbird.sleeveup.retrofit.ApiConfig

@Composable
fun CompanyDetailsScreen(navController: NavController, companyId: String) {
    val apiService = ApiConfig.getApiService()
    val viewModelFactory = CompanyDetailsViewModelFactory(apiService, companyId)
    val viewModel: CompanyDetailsViewModel = viewModel(factory = viewModelFactory)

    val companyState by viewModel.companyState.collectAsState()
    val jobOffersState by viewModel.jobOffersState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (companyState) {
            is CompanyDetailsViewModel.CompanyState.Loading -> {
                CircularProgressIndicator()
            }
            is CompanyDetailsViewModel.CompanyState.Success -> {
                val company = (companyState as CompanyDetailsViewModel.CompanyState.Success).company
                when(jobOffersState) {
                    is CompanyDetailsViewModel.JobOffersState.Loading -> {
                        CircularProgressIndicator()
                    }
                    is CompanyDetailsViewModel.JobOffersState.Success -> {
                        val jobOffers = (jobOffersState as CompanyDetailsViewModel.JobOffersState.Success).jobOffers
                        CompanyDetails(company = company, jobOffers = jobOffers, navController = navController)
                    }
                    is CompanyDetailsViewModel.JobOffersState.Error -> {
                        val errorMessage = (jobOffersState as CompanyDetailsViewModel.JobOffersState.Error).message
                        Text("Error fetching job offers: $errorMessage")
                    }
                    else -> {
                        Text("Waiting for job offers")
                    }
                }
            }
            is CompanyDetailsViewModel.CompanyState.Error -> {
                val errorMessage = (companyState as CompanyDetailsViewModel.CompanyState.Error).message
                Text("Error fetching company details: $errorMessage")
            }
            else -> {
                Text("Waiting for company details...")
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = company.name,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = company.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            if(jobOffers.isEmpty()) {
                Text("No job offers from this company.")
            } else {
                jobOffers.forEach { (jobOfferId, jobOffer) ->
                    JobOfferCard(jobOfferId = jobOfferId, jobOffer = jobOffer, navController = navController)
                }
            }
        }
    }
}
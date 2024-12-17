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
import com.mockingbird.sleeveup.entity.Company
import com.mockingbird.sleeveup.factory.CompanyViewModelFactory
import com.mockingbird.sleeveup.model.CompanyViewModel
import com.mockingbird.sleeveup.retrofit.ApiConfig

@Composable
fun CompanyScreen(navController: NavController) {
    val apiService = ApiConfig.getApiService()
    val viewModelFactory = CompanyViewModelFactory(apiService)
    val viewModel: CompanyViewModel = viewModel(factory = viewModelFactory)

    val companiesState by viewModel.companiesState.collectAsState()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        when (companiesState) {
            is CompanyViewModel.CompaniesState.Loading -> {
                CircularProgressIndicator()
            }
            is CompanyViewModel.CompaniesState.Success -> {
                val companies = (companiesState as CompanyViewModel.CompaniesState.Success).companies
                CompanyList(companies = companies)
            }
            is CompanyViewModel.CompaniesState.Error -> {
                val errorMessage =
                    (companiesState as CompanyViewModel.CompaniesState.Error).message
                Text(text = "Error: $errorMessage")
            }
            else -> {
                Text(text = "Waiting for Initial State...")
            }
        }
    }
}

@Composable
fun CompanyList(companies: Map<String, Company>) {
    LazyColumn(contentPadding = PaddingValues(all = 8.dp)) {
        items(companies.toList()) { (companyId, company) ->
            CompanyCard(company = company, companyId = companyId)
        }
    }
}

@Composable
fun CompanyCard(company: Company, companyId: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = company.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = company.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = companyId)
            Spacer(modifier = Modifier.height(8.dp))

        }
    }
}
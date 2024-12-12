package com.mockingbird.sleeveup.screen

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.mockingbird.sleeveup.R
import com.mockingbird.sleeveup.navigation.Screen
import com.mockingbird.sleeveup.retrofit.ApiConfig
import com.mockingbird.sleeveup.service.AuthService
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect
import com.mockingbird.sleeveup.entity.Company
import com.mockingbird.sleeveup.entity.JobOffer


@Composable
fun ProfileScreen(navController: NavController, email: String) {
    val authService = AuthService(FirebaseAuth.getInstance())
    val context = LocalContext.current
    val apiService = ApiConfig.getApiService()
    val scope = rememberCoroutineScope()

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id)).requestEmail().build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    var companies by remember { mutableStateOf<Map<String, Company>>(emptyMap()) }
    var expandedCompanyId by remember { mutableStateOf<String?>(null) }
    var jobOffers by remember { mutableStateOf<Map<String, JobOffer>>(emptyMap()) }

    LaunchedEffect(key1 = Unit) {
        scope.launch {
            try {
                val fetchedCompanies = apiService.getCompanies()
                companies = fetchedCompanies
            } catch (e: Exception) {
                Log.e("API_TEST", "Error fetching companies: ${e.message}")
            }
        }
    }



    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Hello, $email", modifier = Modifier.padding(16.dp))
        // spacer for the sign out button
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                authService.signOut()
                authService.signOutGoogle(googleSignInClient).addOnCompleteListener {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(0.5f)
        ) {
            Text(stringResource(R.string.logout))
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(companies.toList()) { (companyId, company) ->
                CompanyCard(
                    company_id = companyId,
                    company = company,
                    isExpanded = expandedCompanyId == companyId,
                    onCardClick = {
                        expandedCompanyId = if (expandedCompanyId == companyId) null else companyId
                        if (expandedCompanyId != null){
                            scope.launch {
                                try{
                                    jobOffers = apiService.getJobOffersByCompanyId(companyId = companyId)
                                } catch (e: Exception){
                                    Log.e("API_TEST", "Error fetching job offers for company id ${companyId}: ${e.message}")
                                }
                            }
                        }
                    }
                    ,
                    jobOffers = jobOffers.filter { it.value.company_id == companyId}.values.toList()
                )

            }
        }
    }
}



@Composable
fun CompanyCard(
    company_id: String,
    company: Company,
    isExpanded: Boolean,
    onCardClick: () -> Unit,
    jobOffers: List<JobOffer>

) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onCardClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = company.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = company_id,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = company.description,
                style = MaterialTheme.typography.bodyMedium,
            )
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300))
            ){
                Column{
                    if (jobOffers.isEmpty()){
                        Text(text = "No jobs offers for this company yet!")
                    }
                    else {
                        jobOffers.forEach{ jobOffer ->
                            JobOfferCard(jobOffer = jobOffer)
                        }
                    }

                }
            }
        }
    }
}


@Composable
fun JobOfferCard(jobOffer: JobOffer) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ){
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
        }

    }
}
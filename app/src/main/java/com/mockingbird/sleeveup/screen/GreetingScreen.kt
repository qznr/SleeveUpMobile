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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mockingbird.sleeveup.entity.Company
import com.mockingbird.sleeveup.entity.JobOffer
import com.mockingbird.sleeveup.entity.User
import com.mockingbird.sleeveup.repository.FirebaseUserRepository
import com.mockingbird.sleeveup.service.FirestoreService
import com.google.firebase.firestore.FirebaseFirestore
import com.mockingbird.sleeveup.factory.EditProfileViewModelFactory
import com.mockingbird.sleeveup.model.EditProfileViewModel
import com.mockingbird.sleeveup.service.StorageService

private const val TAG = "LandingScreen"

@Composable
fun LandingScreen(navController: NavController, email: String) {
    val apiService = ApiConfig.getApiService()
    val scope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()
    val firestoreService = FirestoreService(firestore)
    val userRepository = FirebaseUserRepository(firestoreService)
    val storageService = StorageService()
    val authService = FirebaseAuth.getInstance()
    val viewModelFactory =
        EditProfileViewModelFactory(authService, userRepository, storageService, navController)
    val viewModel: EditProfileViewModel = viewModel(factory = viewModelFactory)

    var companies by remember { mutableStateOf<Map<String, Company>>(emptyMap()) }
    var expandedCompanyId by remember { mutableStateOf<String?>(null) }
    var jobOffers by remember { mutableStateOf<Map<String, JobOffer>>(emptyMap()) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    val userState by viewModel.userState.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    LaunchedEffect(key1 = userId) {
        if (userId != null) {
            viewModel.fetchUser(userId)
        }
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
        modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Hello, $email", modifier = Modifier.padding(16.dp))
        // spacer for the sign out button
        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(
                onClick = {
                    userId?.let {
                        navController.navigate(Screen.UserProfile.createRoute(it))
                    }
                }, modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                Text(stringResource(R.string.my_profile))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(companies.toList()) { (companyId, company) ->
                CompanyCard(companyId = companyId,
                    company = company,
                    isExpanded = expandedCompanyId == companyId,
                    onCardClick = {
                        expandedCompanyId = if (expandedCompanyId == companyId) null else companyId
                        if (expandedCompanyId != null) {
                            scope.launch {
                                try {
                                    jobOffers =
                                        apiService.getJobOffersByCompanyId(companyId = "\"${companyId}\"")
                                } catch (e: Exception) {
                                    Log.e(
                                        "API_TEST",
                                        "Error fetching job offers for company id ${companyId}: ${e.message}"
                                    )
                                }
                            }
                        }
                    },
                    jobOffers = jobOffers.filter { it.value.company_id == companyId },
                    user = if (userState is EditProfileViewModel.EditProfileState.Success) {
                        (userState as EditProfileViewModel.EditProfileState.Success).user
                    } else null,
                    onJobOfferApply = { updatedUser ->
                        Log.d(TAG, "onJobOfferApply called with user: $updatedUser")
                        if (userId != null) {
                            viewModel.updateUser(updatedUser)
                        }
                    })
            }
        }
    }

    when (updateState) {
        is EditProfileViewModel.UpdateState.Success -> {
            Log.d(TAG, "Update successful: ${(updateState as EditProfileViewModel.UpdateState.Success).user}")
        }
        is EditProfileViewModel.UpdateState.Error -> {
            Log.d(TAG, "Update error: ${(updateState as EditProfileViewModel.UpdateState.Error).message}")
        }
        else -> {}
    }
}

@Composable
fun CompanyCard(
    companyId: String,
    company: Company,
    isExpanded: Boolean,
    onCardClick: () -> Unit,
    jobOffers: Map<String, JobOffer>,
    user: User?,
    onJobOfferApply: (User) -> Unit
) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .clickable { onCardClick() }) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = company.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = companyId,
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
            ) {
                Column {
                    if (jobOffers.isEmpty()) {
                        Text(text = "No jobs offers for this company yet!")
                    } else {
                        jobOffers.toList().forEach { (jobOfferId, jobOffer) ->
                            JobOfferCard(
                                jobOfferId = jobOfferId,
                                jobOffer = jobOffer,
                                onJobOfferApply = onJobOfferApply,
                                user = user
                            )
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun JobOfferCard(
    jobOfferId: String, jobOffer: JobOffer, onJobOfferApply: (User) -> Unit, user: User?
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    var isPending by remember(user, jobOfferId) {
        mutableStateOf(user?.pendingJobApplication?.containsKey(jobOfferId) == true)
    }


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
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
            Button(onClick = {
                Log.d(TAG, "Job offer button clicked, isPending: $isPending, user: $user, jobOfferId: $jobOfferId")
                if (userId != null && user != null) {
                    val updatedUser = user.copy(
                        pendingJobApplication = if (isPending) {
                            // Keeps the current list, do not do anything to it
                            // You can only remove pending job apps from the edit profile page
                            user.pendingJobApplication
                        } else {
                            (user.pendingJobApplication ?: emptyMap()) + (jobOfferId to jobOffer)
                        }
                    )
                    onJobOfferApply(updatedUser)
                    isPending = !isPending
                }
            },
                enabled = !isPending) { Text(if (isPending) "Sudah Dilamar!" else "Lamar sekarang!") }
        }
    }
}
package com.mockingbird.sleeveup.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mockingbird.sleeveup.entity.JobOffer
import com.mockingbird.sleeveup.entity.User
import com.mockingbird.sleeveup.factory.EditProfileViewModelFactory
import com.mockingbird.sleeveup.model.EditProfileViewModel
import com.mockingbird.sleeveup.repository.FirebaseUserRepository
import com.mockingbird.sleeveup.service.FirestoreService
import com.mockingbird.sleeveup.service.StorageService
import com.mockingbird.sleeveup.retrofit.ApiConfig
import kotlinx.coroutines.launch

@Composable
fun JobDetailsScreen(modifier: Modifier = Modifier, navController: NavController, jobId: String) {
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

    var jobOffer by remember { mutableStateOf<JobOffer?>(null) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val userState by viewModel.userState.collectAsState()
    val pendingStates by viewModel.pendingJobOfferStates.collectAsState()

    LaunchedEffect(key1 = jobId) {
        scope.launch {
            try {
                jobOffer = apiService.getJobOfferById(jobId)
            } catch (e: Exception) {
                Log.e("API_TEST", "Error fetching job offer with id $jobId: ${e.message}")
            }
        }

        if (userId != null) {
            viewModel.fetchUser(userId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (jobOffer != null) {
            JobOfferDetails(jobOffer = jobOffer!!,
                user = if (userState is EditProfileViewModel.EditProfileState.Success) {
                    (userState as EditProfileViewModel.EditProfileState.Success).user
                } else null,
                pendingState = pendingStates[jobId] ?: EditProfileViewModel.PendingState.Idle,
                onApplyJob = { user, jobOffer ->
                    if (userId != null && user != null) viewModel.updateJobApplication(
                        user, jobId, jobOffer
                    )
                })
        } else {
            Text("Loading job offer details...")
        }
    }
}

@Composable
fun JobOfferDetails(
    jobOffer: JobOffer,
    user: User?,
    pendingState: EditProfileViewModel.PendingState,
    onApplyJob: (User?, JobOffer) -> Unit
) {
    val isPending = when (pendingState) {
        is EditProfileViewModel.PendingState.Success -> pendingState.isPending
        else -> false
    }
    val isLoading = pendingState is EditProfileViewModel.PendingState.Loading
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
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = jobOffer.description,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Full Description: ${jobOffer.full_description}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Salary: ${jobOffer.salary}", style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Requirements: ${jobOffer.requirement}",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Remote: ${jobOffer.is_remote}", style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Type: ${jobOffer.type}", style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Benefits: ${jobOffer.benefits}", style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    onApplyJob(user, jobOffer)
                }, enabled = !isPending && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text(if (isPending) "Sudah dilamar!" else "Lamar sekarang!")
                }
            }
        }
    }
}
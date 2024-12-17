// app/src/main/java/com/mockingbird/sleeveup/screen/JobDetailsScreen.kt
package com.mockingbird.sleeveup.screen

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
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
import com.mockingbird.sleeveup.entity.Company
import com.mockingbird.sleeveup.entity.JobOffer
import com.mockingbird.sleeveup.entity.User
import com.mockingbird.sleeveup.factory.JobDetailsViewModelFactory
import com.mockingbird.sleeveup.model.JobDetailsViewModel
import com.mockingbird.sleeveup.repository.FirebaseUserRepository
import com.mockingbird.sleeveup.service.FirestoreService
import com.mockingbird.sleeveup.service.StorageService
import com.mockingbird.sleeveup.retrofit.ApiConfig
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailsScreen(modifier: Modifier = Modifier, navController: NavController, jobId: String) {
    val apiService = ApiConfig.getApiService()
    val scope = rememberCoroutineScope()

    val firestore = FirebaseFirestore.getInstance()
    val firestoreService = FirestoreService(firestore)
    val userRepository = FirebaseUserRepository(firestoreService)

    val authService = FirebaseAuth.getInstance()
    val viewModelFactory =
        JobDetailsViewModelFactory(apiService, userRepository, jobId)
    val viewModel: JobDetailsViewModel = viewModel(factory = viewModelFactory)


    val jobOfferState by viewModel.jobOfferState.collectAsState()
    val companyState by viewModel.companyState.collectAsState()
    val pendingState by viewModel.pendingState.collectAsState()
    var user by remember { mutableStateOf<User?>(null) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(key1 = userId) {
        if (userId != null) {
            scope.launch {
                user = userRepository.getUser(userId)
                viewModel.fetchUserPendingState(user)
            }

        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Job Details") },
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
            when (jobOfferState) {
                is JobDetailsViewModel.JobOfferState.Loading -> {
                    CircularProgressIndicator()
                }
                is JobDetailsViewModel.JobOfferState.Success -> {
                    val jobOffer = (jobOfferState as JobDetailsViewModel.JobOfferState.Success).jobOffer

                    when (companyState) {
                        is JobDetailsViewModel.CompanyState.Loading -> {
                            CircularProgressIndicator()
                        }
                        is JobDetailsViewModel.CompanyState.Success -> {
                            val company = (companyState as JobDetailsViewModel.CompanyState.Success).company
                            JobOfferDetails(
                                jobOffer = jobOffer,
                                company = company,
                                user = user,
                                pendingState = pendingState,
                                onApplyJob = { user, jobOffer ->
                                    if (user != null) viewModel.applyJob(
                                        user, jobOffer
                                    ) else {
                                        Log.d("JobDetailsScreen", "user is null, please login")
                                    }
                                },
                                onRemoveJob = { user ->
                                    if(user != null) viewModel.removeJobApplication(user)
                                }
                            )
                        }
                        is JobDetailsViewModel.CompanyState.Error -> {
                            val errorMessage =
                                (companyState as JobDetailsViewModel.CompanyState.Error).message
                            Text("Error fetching company details: $errorMessage")
                        }
                        else -> {
                            Text("Waiting for company details...")
                        }
                    }

                }
                is JobDetailsViewModel.JobOfferState.Error -> {
                    val errorMessage =
                        (jobOfferState as JobDetailsViewModel.JobOfferState.Error).message
                    Text("Error fetching job offer details: $errorMessage")
                }
                else -> {
                    Text("Waiting for job offer details...")
                }
            }
        }
    }
}

@Composable
fun JobOfferDetails(
    jobOffer: JobOffer,
    company: Company,
    user: User?,
    pendingState: JobDetailsViewModel.PendingState,
    onApplyJob: (User?, JobOffer) -> Unit,
    onRemoveJob: (User?) -> Unit
) {
    var isCompanyDescriptionExpanded by remember { mutableStateOf(false) }
    var isFullDescriptionExpanded by remember { mutableStateOf(false) }
    var isRequirementsExpanded by remember { mutableStateOf(false) }
    var isBenefitsExpanded by remember { mutableStateOf(false) }

    val isPending = when (pendingState) {
        is JobDetailsViewModel.PendingState.Success -> pendingState.isPending
        else -> false
    }
    val isLoading = pendingState is JobDetailsViewModel.PendingState.Loading
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Job Offer Name
        Text(
            text = jobOffer.profession,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        // Company Info
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable {
                isCompanyDescriptionExpanded = !isCompanyDescriptionExpanded
            }
        ) {
            Text(
                text = company.name,
                style = MaterialTheme.typography.headlineSmall,
            )
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Expand Company Description",
                modifier = Modifier
                    .size(24.dp)
            )
        }


        AnimatedVisibility(
            visible = isCompanyDescriptionExpanded,
            enter = expandVertically(animationSpec = tween(300)),
            exit = shrinkVertically(animationSpec = tween(300))
        ) {
            Column {
                Text(
                    text = company.email,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 16.dp)
                )
                Text(
                    text = company.number,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 16.dp)
                )
                Text(
                    text = company.address,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 16.dp)
                )
                Text(
                    text = company.description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Job Full Description
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { isFullDescriptionExpanded = !isFullDescriptionExpanded }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Full Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Expand Full Description",
                        modifier = Modifier
                            .size(24.dp)
                    )
                }
                AnimatedVisibility(
                    visible = isFullDescriptionExpanded,
                    enter = expandVertically(animationSpec = tween(300)),
                    exit = shrinkVertically(animationSpec = tween(300))
                ) {
                    Text(text = jobOffer.full_description, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Job Requirements
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { isRequirementsExpanded = !isRequirementsExpanded }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Requirements", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Expand Requirements",
                        modifier = Modifier
                            .size(24.dp)
                    )
                }
                AnimatedVisibility(
                    visible = isRequirementsExpanded,
                    enter = expandVertically(animationSpec = tween(300)),
                    exit = shrinkVertically(animationSpec = tween(300))
                ) {
                    Text(text = jobOffer.requirement, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // Job Benefits
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { isBenefitsExpanded = !isBenefitsExpanded }
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Benefits", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Expand Benefits",
                        modifier = Modifier
                            .size(24.dp)
                    )
                }
                AnimatedVisibility(
                    visible = isBenefitsExpanded,
                    enter = expandVertically(animationSpec = tween(300)),
                    exit = shrinkVertically(animationSpec = tween(300))
                ) {
                    Text(text = jobOffer.benefits, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (isPending) {
                    onRemoveJob(user)
                } else {
                    onApplyJob(user, jobOffer)
                }
            }, enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text(if (isPending) "Batalkan Lamaran" else "Lamar sekarang!")
            }
        }
    }
}
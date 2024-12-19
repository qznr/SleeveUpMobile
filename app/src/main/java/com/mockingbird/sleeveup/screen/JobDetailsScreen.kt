package com.mockingbird.sleeveup.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mockingbird.sleeveup.R
import com.mockingbird.sleeveup.entity.Company
import com.mockingbird.sleeveup.entity.JobOffer
import com.mockingbird.sleeveup.entity.User
import com.mockingbird.sleeveup.factory.JobDetailsViewModelFactory
import com.mockingbird.sleeveup.model.JobDetailsViewModel
import com.mockingbird.sleeveup.repository.FirebaseUserRepository
import com.mockingbird.sleeveup.service.FirestoreService
import com.mockingbird.sleeveup.retrofit.ApiConfig
import com.mockingbird.sleeveup.ui.theme.AlmostBlack
import com.mockingbird.sleeveup.ui.theme.MajorelieBlue
import com.mockingbird.sleeveup.ui.theme.Moonstone
import com.mockingbird.sleeveup.ui.theme.White
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailsScreen(modifier: Modifier = Modifier, navController: NavController, jobId: String) {
    val apiService = ApiConfig.getApiService()
    val scope = rememberCoroutineScope()

    val firestore = FirebaseFirestore.getInstance()
    val firestoreService = FirestoreService(firestore)
    val userRepository = FirebaseUserRepository(firestoreService)

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
                    text = "Lowongan Kerja",
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
                when (jobOfferState) {
                    is JobDetailsViewModel.JobOfferState.Loading -> {
                        CircularProgressIndicator(color = White)
                    }

                    is JobDetailsViewModel.JobOfferState.Success -> {
                        val jobOffer =
                            (jobOfferState as JobDetailsViewModel.JobOfferState.Success).jobOffer

                        when (companyState) {
                            is JobDetailsViewModel.CompanyState.Loading -> {
                                CircularProgressIndicator(color = White)
                            }

                            is JobDetailsViewModel.CompanyState.Success -> {
                                val company =
                                    (companyState as JobDetailsViewModel.CompanyState.Success).company
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
                                        if (user != null) viewModel.removeJobApplication(user)
                                    }
                                )
                            }

                            is JobDetailsViewModel.CompanyState.Error -> {
                                val errorMessage =
                                    (companyState as JobDetailsViewModel.CompanyState.Error).message
                                Text("Error fetching company details: $errorMessage", color = White)
                            }

                            else -> {
                                Text("Waiting for company details...", color = White)
                            }
                        }

                    }

                    is JobDetailsViewModel.JobOfferState.Error -> {
                        val errorMessage =
                            (jobOfferState as JobDetailsViewModel.JobOfferState.Error).message
                        Text("Error fetching job offer details: $errorMessage", color = White)
                    }

                    else -> {
                        Text("Waiting for job offer details...", color = White)
                    }
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

    val isPending = when (pendingState) {
        is JobDetailsViewModel.PendingState.Success -> pendingState.isPending
        else -> false
    }
    val isLoading = pendingState is JobDetailsViewModel.PendingState.Loading


    var isCompanyDescriptionExpanded by remember { mutableStateOf(false) }
    var isRequirementsExpanded by remember { mutableStateOf(false) }
    var isBenefitsExpanded by remember { mutableStateOf(false) }

    val companyDescriptionLength = company.description.length
    val requirementLength = jobOffer.requirement.length
    val benefitsLength = jobOffer.benefits.length

    val isCompanyExpandable = companyDescriptionLength > 100
    val isRequirementExpandable = requirementLength > 100
    val isBenefitsExpandable = benefitsLength > 100
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Placeholder profile picture",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Gray),
                tint = White
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(horizontalAlignment = Alignment.Start) {
                // Job Offer Name
                Text(
                    text = jobOffer.profession,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MajorelieBlue
                )
                // Company Info
                Text(
                    text = company.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = White
                )
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painterResource(id = R.drawable.outline_email_24),
                        contentDescription = "Placeholder profile picture",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(horizontal = 4.dp),
                        tint = Moonstone
                    )
                    Text(
                        text = company.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painterResource(id = R.drawable.outline_call_24),
                        contentDescription = "Placeholder profile picture",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(horizontal = 4.dp),
                        tint = Moonstone
                    )
                    Text(
                        text = company.number,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painterResource(id = R.drawable.baseline_location_on_24),
                        contentDescription = "Placeholder profile picture",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(horizontal = 4.dp),
                        tint = Moonstone
                    )
                    Text(
                        text = company.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Company Info
        ExpandableCard(
            title = "Tentang Perusahaan",
            previewContent = {
                Text(
                    text = company.description.take(100),
                    style = MaterialTheme.typography.bodyMedium,
                    color = White
                )
            },
            fullContent = {
                Text(
                    text = company.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = White
                )
            },
            isExpanded = isCompanyDescriptionExpanded,
            onExpandChange = {isCompanyDescriptionExpanded = it},
            color = MajorelieBlue,
            showEditButton = false,
            isExpandable = isCompanyExpandable
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Job Requirements
        ExpandableCard(
            title = "Persyaratan",
            previewContent = {
                Text(
                    text = jobOffer.requirement.take(100),
                    style = MaterialTheme.typography.bodyMedium,
                    color = White
                )
            },
            fullContent = {
                Text(
                    text = jobOffer.requirement,
                    style = MaterialTheme.typography.bodyMedium,
                    color = White
                )
            },
            isExpanded = isRequirementsExpanded,
            onExpandChange = {isRequirementsExpanded = it},
            color = MajorelieBlue,
            showEditButton = false,
            isExpandable = isRequirementExpandable
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Job Benefits
        ExpandableCard(
            title = "Fasilitas Karyawan",
            previewContent = {
                Column{
                    Text(
                        text = jobOffer.benefits.take(100),
                        style = MaterialTheme.typography.bodyMedium,
                        color = White
                    )
                    if (jobOffer.salary.isNotBlank()) {
                        Text(
                            text = "Gaji: Rp ${NumberFormat.getNumberInstance(Locale("id", "ID")).format(jobOffer.salary.toDouble())} / Bulan",
                            style = MaterialTheme.typography.bodyMedium,
                            color = White
                        )
                    }
                }

            },
            fullContent = {
                Column{
                    Text(
                        text = jobOffer.benefits,
                        style = MaterialTheme.typography.bodyMedium,
                        color = White
                    )
                    if (jobOffer.salary.isNotBlank()) {
                        Text(
                            text = "Gaji: Rp ${NumberFormat.getNumberInstance(Locale("id", "ID")).format(jobOffer.salary.toDouble())} / Bulan",
                            style = MaterialTheme.typography.bodyMedium,
                            color = White
                        )
                    }
                }

            },
            isExpanded = isBenefitsExpanded,
            onExpandChange = {isBenefitsExpanded = it},
            color = MajorelieBlue,
            showEditButton = false,
            isExpandable = isBenefitsExpandable
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (isPending) {
                    onRemoveJob(user)
                } else {
                    onApplyJob(user, jobOffer)
                }
            },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MajorelieBlue)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = White)
            } else {
                Text(if (isPending) "Batalkan Lamaran" else "Lamar sekarang!", color = White)
            }
        }
    }
}
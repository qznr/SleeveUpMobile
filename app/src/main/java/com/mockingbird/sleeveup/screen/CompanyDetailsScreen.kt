package com.mockingbird.sleeveup.screen

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.mockingbird.sleeveup.R
import com.mockingbird.sleeveup.entity.Company
import com.mockingbird.sleeveup.entity.JobOffer
import com.mockingbird.sleeveup.factory.CompanyDetailsViewModelFactory
import com.mockingbird.sleeveup.model.CompanyDetailsViewModel
import com.mockingbird.sleeveup.navigation.Screen
import com.mockingbird.sleeveup.retrofit.ApiConfig
import com.mockingbird.sleeveup.ui.theme.AlmostBlack
import com.mockingbird.sleeveup.ui.theme.MajorelieBlue
import com.mockingbird.sleeveup.ui.theme.Moonstone
import com.mockingbird.sleeveup.ui.theme.TickleMePink
import com.mockingbird.sleeveup.ui.theme.White
import java.text.NumberFormat
import java.util.Locale


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
                title = { Text("Perusahaan", color = White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "backIcon", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AlmostBlack)

            )
        },
        containerColor = AlmostBlack
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
                    CircularProgressIndicator(color = White)
                }
                is CompanyDetailsViewModel.CompanyState.Success -> {
                    val company = (companyState as CompanyDetailsViewModel.CompanyState.Success).company
                    when (jobOffersState) {
                        is CompanyDetailsViewModel.JobOffersState.Loading -> {
                            CircularProgressIndicator(color = White)
                        }
                        is CompanyDetailsViewModel.JobOffersState.Success -> {
                            val jobOffers =
                                (jobOffersState as CompanyDetailsViewModel.JobOffersState.Success).jobOffers
                            CompanyDetailsContent(
                                company = company,
                                jobOffers = jobOffers,
                                navController = navController
                            )
                        }
                        is CompanyDetailsViewModel.JobOffersState.Error -> {
                            val errorMessage =
                                (jobOffersState as CompanyDetailsViewModel.JobOffersState.Error).message
                            Text("Error fetching job offers: $errorMessage", color = White)
                        }
                        else -> {
                            Text("Waiting for job offers...", color = White)
                        }
                    }
                }
                is CompanyDetailsViewModel.CompanyState.Error -> {
                    val errorMessage =
                        (companyState as CompanyDetailsViewModel.CompanyState.Error).message
                    Text("Error fetching company details: $errorMessage", color = White)
                }
                else -> {
                    Text("Waiting for company details...", color = White)
                }
            }
        }
    }
}

@Composable
fun CompanyDetailsContent(
    company: Company,
    jobOffers: Map<String, JobOffer>,
    navController: NavController
) {
    val companyDescriptionLength = company.description.length
    val isCompanyExpandable = companyDescriptionLength > 100
    var isCompanyDescriptionExpanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
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

            Column {
                Text(
                    text = company.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = White
                )
                Text(
                    text = company.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
                Text(
                    text = company.number,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
                Text(
                    text = company.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tentang Perusahaan",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MajorelieBlue,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = company.description,
            style = MaterialTheme.typography.bodyMedium,
            color = White
        )

        Spacer(modifier = Modifier.height(16.dp))


        ExpandableCard(
            title = "Posisi",
            previewContent = {
                Text(
                    text = if (jobOffers.isEmpty()) "Tidak ada posisi yang tersedia" else "Terdapat ${jobOffers.size} posisi",
                    style = MaterialTheme.typography.bodyMedium,
                    color = White
                )

            },
            fullContent = {
                if (jobOffers.isEmpty()) {
                    Text("Tidak ada posisi yang tersedia", color = White)
                } else {
                    Column{
                        jobOffers.forEach { (jobOfferId, jobOffer) ->
                            JobOfferCard(jobOfferId = jobOfferId, jobOffer = jobOffer, navController = navController)
                        }
                    }
                }
            },
            isExpanded = isCompanyDescriptionExpanded,
            onExpandChange = {isCompanyDescriptionExpanded = it},
            color = MajorelieBlue,
            showEditButton = false,
            isExpandable = true
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun JobOfferCard(
    jobOfferId: String,
    jobOffer: JobOffer,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = jobOffer.profession,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = White
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Column {
                    if (jobOffer.salary.isNotBlank()){
                        Text(
                            text = "Rp ${NumberFormat.getNumberInstance(Locale("id", "ID")).format(jobOffer.salary.toDouble())} / bulan",
                            style = MaterialTheme.typography.bodySmall,
                            color = White
                        )
                    }
                    if(jobOffer.education.isNotBlank()){
                        Text(
                            text = jobOffer.education,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (jobOffer.is_remote == "yes") Moonstone else TickleMePink,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = if (jobOffer.is_remote == "yes") "Remote" else "On-site",
                                style = MaterialTheme.typography.bodySmall,
                                color = AlmostBlack,
                                modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Moonstone,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = jobOffer.type ?: "No info",
                                style = MaterialTheme.typography.bodySmall,
                                color = AlmostBlack,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Button(
                    onClick = {
                        navController.navigate(Screen.JobDetails.createRoute(jobOfferId))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MajorelieBlue),
                ) {
                    Text("Detail", color = White)
                }
            }
        }

    }
}
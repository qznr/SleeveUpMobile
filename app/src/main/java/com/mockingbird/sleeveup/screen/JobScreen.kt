package com.mockingbird.sleeveup.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.mockingbird.sleeveup.R
import com.mockingbird.sleeveup.entity.JobOffer
import com.mockingbird.sleeveup.factory.JobViewModelFactory
import com.mockingbird.sleeveup.model.JobViewModel
import com.mockingbird.sleeveup.retrofit.ApiConfig
import com.mockingbird.sleeveup.navigation.Screen
import com.mockingbird.sleeveup.service.AuthService
import com.mockingbird.sleeveup.ui.theme.AlmostBlack
import com.mockingbird.sleeveup.ui.theme.MajorelieBlue
import com.mockingbird.sleeveup.ui.theme.Moonstone
import com.mockingbird.sleeveup.ui.theme.TickleMePink
import com.mockingbird.sleeveup.ui.theme.White
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobScreen(navController: NavController) {
    val context = LocalContext.current
    val authService = AuthService(FirebaseAuth.getInstance())
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id)).requestEmail().build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val apiService = ApiConfig.getApiService()
    val viewModelFactory = JobViewModelFactory(apiService)
    val viewModel: JobViewModel = viewModel(factory = viewModelFactory)

    val jobOffersState by viewModel.jobOffersState.collectAsState()
    var searchText by remember { mutableStateOf("") }

    Surface(
        color = AlmostBlack,
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lowongan Kerja",
                    style = MaterialTheme.typography.headlineSmall,
                    color = White
                )
                IconButton(
                    onClick = {
                        authService.signOut()
                        authService.signOutGoogle(googleSignInClient).addOnCompleteListener {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_logout_24),
                        contentDescription = stringResource(R.string.logout),
                        tint = White
                    )
                }
            }
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = { Text("Cari...", color = Color.LightGray) },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_search_24),
                                contentDescription = "Search Icon",
                                tint = MajorelieBlue
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp)),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = White,
                            unfocusedBorderColor = MajorelieBlue,
                            cursorColor = White,
                            containerColor = White,
                            unfocusedPlaceholderColor = White,
                            focusedTextColor = AlmostBlack
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                println("Searching for: $searchText")
                            }
                        ),
                        singleLine = true
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                when (jobOffersState) {
                    is JobViewModel.JobOffersState.Loading -> {
                        CircularProgressIndicator()
                    }

                    is JobViewModel.JobOffersState.Success -> {
                        val allJobOffers =
                            (jobOffersState as JobViewModel.JobOffersState.Success).jobOffers
                        val filteredJobOffers = if (searchText.isNotBlank()) {
                            allJobOffers.filter { (_, jobOffer) ->
                                jobOffer.profession.contains(searchText, ignoreCase = true) ||
                                        jobOffer.company_name.contains(searchText, ignoreCase = true)
                            }
                        } else {
                            allJobOffers
                        }
                        JobList(jobOffers = filteredJobOffers, navController = navController)
                    }

                    is JobViewModel.JobOffersState.Error -> {
                        val errorMessage =
                            (jobOffersState as JobViewModel.JobOffersState.Error).message
                        Text(text = "Error: $errorMessage")
                    }

                    else -> {
                        Text(text = "Waiting for initial State...")
                    }
                }
            }
        }
    }
}

@Composable
fun JobList(jobOffers: Map<String, JobOffer>, navController: NavController) {
    if (jobOffers.isEmpty()) {
        Text(
            text = "Tidak ada lowongan kerja ditemukan.",
            color = White,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
        )
    } else {
        LazyColumn(contentPadding = PaddingValues(all = 8.dp)) {
            items(jobOffers.toList()) { (jobOfferId, jobOffer) ->
                JobCard(jobOffer = jobOffer, jobOfferId = jobOfferId, navController = navController)
            }
        }
    }
}

@Composable
fun JobCard(jobOffer: JobOffer, jobOfferId: String, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate(Screen.JobDetails.createRoute(jobOfferId))
            },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Placeholder profile picture",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Gray),
                tint = White
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = jobOffer.profession,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MajorelieBlue
                )
                Text(
                    text = jobOffer.company_name,
                    style = MaterialTheme.typography.titleLarge,
                    color = White
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (jobOffer.salary.isNotBlank()) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_payments_24),
                            contentDescription = "Salary",
                            tint = Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Rp ${NumberFormat.getNumberInstance(Locale("id", "ID")).format(jobOffer.salary.toDouble())} / Bulan",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (jobOffer.education.isNotBlank()) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_school_24),
                            contentDescription = "Education",
                            tint = Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = jobOffer.education,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                                fontWeight = FontWeight.Bold,
                                color = AlmostBlack,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
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
                                fontWeight = FontWeight.Bold,
                                color = AlmostBlack,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 4.dp),
            thickness = 1.dp,
            color = White.copy(alpha = 0.5f)
        )
    }
}
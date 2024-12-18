package com.mockingbird.sleeveup.screen

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.mockingbird.sleeveup.R
import com.mockingbird.sleeveup.entity.JobOffer
import com.mockingbird.sleeveup.factory.JobViewModelFactory
import com.mockingbird.sleeveup.model.JobViewModel
import com.mockingbird.sleeveup.retrofit.ApiConfig
import com.mockingbird.sleeveup.navigation.Screen
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
    val apiService = ApiConfig.getApiService()
    val viewModelFactory = JobViewModelFactory(apiService)
    val viewModel: JobViewModel = viewModel(factory = viewModelFactory)

    val jobOffersState by viewModel.jobOffersState.collectAsState()
    var searchText by remember { mutableStateOf("") }

    Surface (
        color = AlmostBlack,
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically // Align items vertically
            ) {
                Text( // Title on the left
                    text = "Lowongan Kerja", // Assuming you have a string resource for "Profil"
                    style = MaterialTheme.typography.headlineMedium, // Or any appropriate style
                    color = White
                )
                IconButton(
                    onClick = {
                        /*authService.signOut()
                        authService.signOutGoogle(googleSignInClient).addOnCompleteListener {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }*/
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
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End, // Align filter to the end
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
                            .clip(RoundedCornerShape(16.dp)),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = White,
                            unfocusedBorderColor = MajorelieBlue,
                            cursorColor = White,
                            containerColor = White,
                            unfocusedPlaceholderColor = White,
                            focusedTextColor = AlmostBlack// Placeholder color
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), // Handle search action
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                println("Searching for: $searchText")
                            }
                        ),
                        singleLine = true // Make it a single line input
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Filter Button
                    IconButton(onClick = { /* TODO: Filter functionality */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.outline_filter_alt_24),
                            contentDescription = "Filter",
                            tint = White,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MajorelieBlue) // Light background
                                .size(32.dp) // increase Size
                        )
                    }
                }
            }

            Column(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp))
            {
                when (jobOffersState) {
                    is JobViewModel.JobOffersState.Loading -> {
                        CircularProgressIndicator()
                    }

                    is JobViewModel.JobOffersState.Success -> {
                        val jobOffers =
                            (jobOffersState as JobViewModel.JobOffersState.Success).jobOffers
                        JobList(jobOffers = jobOffers, navController = navController)
                    }

                    is JobViewModel.JobOffersState.Error -> {
                        val errorMessage = (jobOffersState as JobViewModel.JobOffersState.Error).message
                        Text(text = "Error: $errorMessage")
                    }

                    else -> {
                        // Initial State
                        Text(text = "Waiting for initial State...")
                    }
                }
            }
        }
    }
}

@Composable
fun JobList(jobOffers: Map<String, JobOffer>, navController: NavController) {
    LazyColumn(contentPadding = PaddingValues(all = 8.dp)) {
        items(jobOffers.toList()) { (jobOfferId, jobOffer) ->
            JobCard(jobOffer = jobOffer, jobOfferId = jobOfferId, navController = navController)
        }
    }
}


@Composable
fun JobCard(jobOffer: JobOffer, jobOfferId: String, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Company Logo (if available)
            if (jobOffer.company_img.isNotBlank()) { // Check if image URL exists
                AsyncImage(
                    model = jobOffer.company_img,
                    contentDescription = "Company Logo",
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(16.dp))
            } else { // Menampilkan placeholder jika imageBytes null
                Icon(
                    imageVector = Icons.Default.Person, // Atau ikon lain yang sesuai
                    contentDescription = "Placeholder profile picture",
                    modifier = Modifier
                        .size(128.dp)
                        .clip(RectangleShape)
                        .background(Color.Gray), // Warna latar belakang placeholder
                    tint = White // Warna ikon
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = jobOffer.profession,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MajorelieBlue
                )
                Text(
                    text = jobOffer.company_name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = White
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .width(8.dp),
                    horizontalArrangement = Arrangement.Start // Align items to the start
                ) {
                    if (jobOffer.salary.isNotBlank()) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_payments_24),
                            contentDescription = "Salary",
                            tint = White,
                            modifier = Modifier.size(16.dp)
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
                        .fillMaxWidth()
                        .width(8.dp),
                    horizontalArrangement = Arrangement.Start // Align items to the start
                ) {
                    if (jobOffer.education.isNotBlank()) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_school_24),
                            contentDescription = "Education",
                            tint = White,
                            modifier = Modifier.size(16.dp)
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
                        .fillMaxWidth()
                        .width(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Remote/Onsite status (if available)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (jobOffer.is_remote == "yes") Moonstone else TickleMePink, // Conditional color
                            modifier = Modifier.padding(vertical = 2.dp) // small vertical padding
                        ) {
                            Text(
                                text = if (jobOffer.is_remote == "yes") "Remote" else "On-site",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Moonstone, // Conditional color
                            modifier = Modifier.padding(vertical = 2.dp) // small vertical padding
                        ) {
                            Text(
                                text = jobOffer.type ?: "No info",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .width(8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            navController.navigate(Screen.JobDetails.createRoute(jobOfferId))
                        }
                    ) {
                        Text(text = "Detail", style = MaterialTheme.typography.labelLarge)
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
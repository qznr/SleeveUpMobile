package com.mockingbird.sleeveup.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.mockingbird.sleeveup.R
import com.mockingbird.sleeveup.entity.Company
import com.mockingbird.sleeveup.factory.CompanyViewModelFactory
import com.mockingbird.sleeveup.model.CompanyViewModel
import com.mockingbird.sleeveup.retrofit.ApiConfig
import com.mockingbird.sleeveup.navigation.Screen
import com.mockingbird.sleeveup.ui.theme.AlmostBlack
import com.mockingbird.sleeveup.ui.theme.MajorelieBlue
import com.mockingbird.sleeveup.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyScreen(navController: NavController) {
    val apiService = ApiConfig.getApiService()
    val viewModelFactory = CompanyViewModelFactory(apiService)
    val viewModel: CompanyViewModel = viewModel(factory = viewModelFactory)

    val companiesState by viewModel.companiesState.collectAsState()
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
                    text = "Perusahaan",
                    style = MaterialTheme.typography.headlineMedium,
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (companiesState) {
                    is CompanyViewModel.CompaniesState.Loading -> {
                        CircularProgressIndicator()
                    }

                    is CompanyViewModel.CompaniesState.Success -> {
                        val companies =
                            (companiesState as CompanyViewModel.CompaniesState.Success).companies
                        CompanyList(companies = companies, navController = navController)
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
    }
}

@Composable
fun CompanyList(companies: Map<String, Company>, navController: NavController) {
    LazyColumn(contentPadding = PaddingValues(all = 8.dp)) {
        items(companies.toList()) { (companyId, company) ->
            CompanyCard(company = company, companyId = companyId, navController = navController)
        }
    }
}

@Composable
fun CompanyCard(company: Company, companyId: String, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Company Logo (if available)
            if (company.img.isNotBlank()) { // Check if image URL exists
                AsyncImage(
                    model = company.img,
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

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = company.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MajorelieBlue
                )
                Text(
                    text = company.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = White,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .width(8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            navController.navigate(Screen.CompanyDetails.createRoute(companyId))
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
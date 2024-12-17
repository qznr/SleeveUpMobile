package com.mockingbird.sleeveup.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mockingbird.sleeveup.R
import com.mockingbird.sleeveup.entity.Certificate
import com.mockingbird.sleeveup.entity.Experience
import com.mockingbird.sleeveup.entity.JobOffer
import com.mockingbird.sleeveup.entity.Project
import com.mockingbird.sleeveup.entity.User
import com.mockingbird.sleeveup.factory.ProfileViewModelFactory
import com.mockingbird.sleeveup.model.ProfileViewModel
import com.mockingbird.sleeveup.navigation.Screen
import com.mockingbird.sleeveup.repository.FirebaseUserRepository
import com.mockingbird.sleeveup.service.AuthService
import com.mockingbird.sleeveup.service.FirestoreService
import com.mockingbird.sleeveup.service.StorageService
import com.mockingbird.sleeveup.ui.theme.AlmostBlack
import com.mockingbird.sleeveup.ui.theme.MajorelieBlue
import com.mockingbird.sleeveup.ui.theme.White

@Composable
fun ProfileScreen(modifier: Modifier = Modifier, navController: NavController, userId: String) {
    val context = LocalContext.current
    val authService = AuthService(FirebaseAuth.getInstance())
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id)).requestEmail().build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)
    val firestore = FirebaseFirestore.getInstance()
    val firestoreService = FirestoreService(firestore)
    val userRepository = FirebaseUserRepository(firestoreService)
    val storageService = StorageService()

    val viewModelFactory = remember(userRepository, storageService, userId) {
        ProfileViewModelFactory(userRepository, storageService, userId)
    }

    val viewModel: ProfileViewModel = viewModel(factory = viewModelFactory)

    val userState by viewModel.userState.collectAsState()
    val imageState by viewModel.imageState.collectAsState()

    Surface (
        color = AlmostBlack,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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

                IconButton(
                    onClick = {
                        userId.let {
                            navController.navigate(Screen.EditUserProfile.createRoute(it))
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_edit_24),
                        contentDescription = stringResource(R.string.logout),
                        tint = White
                    )
                }
            }

            when(userState){
                is ProfileViewModel.ProfileState.Loading -> {
                    Text(text = "Loading user data...")
                }
                is ProfileViewModel.ProfileState.Success -> {
                    val user = (userState as ProfileViewModel.ProfileState.Success).user
                    ProfileContent(user = user, imageState = imageState)
                }
                is ProfileViewModel.ProfileState.Error -> {
                    val errorMessage = (userState as ProfileViewModel.ProfileState.Error).message
                    Text(text = "Error: $errorMessage")
                }
                else -> {
                    Text(text = "Initial state...")
                }
            }

        }
    }
}

@Composable
fun ProfileContent(user: User, imageState: ProfileViewModel.ImageState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (imageState) {
            is ProfileViewModel.ImageState.Loading -> {
                Text(text = "Loading...")
            }
            is ProfileViewModel.ImageState.Success -> {
                val imageBytes = imageState.imageBytes
                if (imageBytes != null) {
                    AsyncImage(
                        model = imageBytes,
                        contentDescription = "User profile picture",
                        modifier = Modifier.size(128.dp)
                    )
                }
            }
            is ProfileViewModel.ImageState.Error -> {
                val errorMessage =
                    imageState.message
                Text("Error fetching profile image: $errorMessage")
            }
            else -> {}
        }

        Text(
            text = user.name ?: "No Name",  // Provide a default value
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = White
        )
        // Text(text = user.title ?: "No Title", style = MaterialTheme.typography.titleMedium, color = White)
        Text(text = user.gender ?: "No gender provided", style = MaterialTheme.typography.titleMedium, color = White)
        Text(text = user.status ?: "No status provided", style = MaterialTheme.typography.titleMedium, color = White)


        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painterResource(id = R.drawable.baseline_location_on_24),
                contentDescription = "Location",
                modifier = Modifier.size(16.dp),
                tint = White
            )
            Spacer(Modifier.width(4.dp))
            Text(text = user.lokasi ?: "No Location Provided", color = White)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painterResource(id = R.drawable.baseline_loker_24),
                contentDescription = "Education",
                modifier = Modifier.size(16.dp),
                tint = White
            )
            Spacer(Modifier.width(4.dp))
            Text(text = user.education ?: "No Education Provided", color = White)
        }

        Spacer(Modifier.height(16.dp))

        var isBioExpanded by remember { mutableStateOf(false) }
        ExpandableCard(
            title = "Tentang Saya",
            content = { Text(text = user.bio ?: "Isi dengan meng-edit profilmu!", color = White) }, // Provide default for Bio
            isExpanded = isBioExpanded,
            onExpandChange = { isBioExpanded = it },
            textColor = MajorelieBlue,
        )

        var isExperienceExpanded by remember { mutableStateOf(false) }
        ExpandableCard(
            title = "Pengalaman",
            content = { DisplayUserCredentials(items = user.experiences, textColor = White) },
            isExpanded = isExperienceExpanded,
            onExpandChange = { isExperienceExpanded = it },
            textColor = MajorelieBlue
        )
        var isProjectExpanded by remember { mutableStateOf(false) }
        ExpandableCard(
            title = "Proyek",
            content = { DisplayUserCredentials(items = user.projects, textColor = White) },
            isExpanded = isProjectExpanded,
            onExpandChange = { isProjectExpanded = it },
            textColor = MajorelieBlue
        )

        var isCertificationExpanded by remember { mutableStateOf(false) }
        ExpandableCard(
            title = "Sertifikasi",
            content = { DisplayUserCredentials(items = user.certifications, textColor = White) },
            isExpanded = isCertificationExpanded,
            onExpandChange = { isCertificationExpanded = it },
            textColor = MajorelieBlue
        )

        DisplayUserPendingApplications(
            items = user.pendingJobApplication, textColor = White
        )

    }
}

@Composable
fun ExpandableCard(
    title: String,
    content: @Composable () -> Unit,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    textColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onExpandChange(!isExpanded) }
            ) {
                Text(text = title, fontWeight = FontWeight.Bold, color = textColor, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                )
            }
            if (isExpanded) {
                content()
            }
        }
    }
}

@Composable
fun DisplayUserCredentials(items: Collection<*>?, textColor: Color) {
    if (items == null || items.isEmpty()) {
        Text(
            text = "Saat ini belum ada.",
            style = MaterialTheme.typography.bodyMedium,
            color = textColor
        )
    } else {
        Column {
            items.forEach { item ->
                when (item) {
                    is Project ->  UserCredentialItem(title = item.name ?: "No Name", description = item.description ?: "No Description", textColor = textColor)
                    is Certificate -> UserCredentialItem(title = item.name ?: "No Name", description = item.type ?: "No Type", textColor = textColor)
                    is Experience -> UserCredentialItem(title = item.name ?: "No Name", description = item.description ?: "No Description", textColor = textColor)
                }
            }
        }
    }
}

@Composable
fun UserCredentialItem(title: String, description: String, textColor: Color) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = title, fontWeight = FontWeight.Bold, color = textColor)
        Spacer(Modifier.width(8.dp))
        Text(text = description, color = textColor)
    }
}

@Composable
fun DisplayUserPendingApplications(items: Map<String, JobOffer>?, textColor: Color) {
    if (items.isNullOrEmpty()) {
        Text(
            text = "Saat ini belum ada.",
            style = MaterialTheme.typography.bodyMedium,
            color = textColor
        )
    } else {
        items.forEach { (jobOfferId, jobOffer) ->
            UserCredentialCard(title = jobOffer.profession ?: "No Profession", description = jobOffer.description ?: "No Description") //Handles null values
        }
    }
}

@Composable
fun UserCredentialCard(title: String, description: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description, style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
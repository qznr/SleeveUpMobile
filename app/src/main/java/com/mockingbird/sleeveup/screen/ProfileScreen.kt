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
import com.mockingbird.sleeveup.entity.JobOffer
import com.mockingbird.sleeveup.entity.User
import com.mockingbird.sleeveup.factory.ProfileViewModelFactory
import com.mockingbird.sleeveup.model.ProfileViewModel
import com.mockingbird.sleeveup.navigation.Screen
import com.mockingbird.sleeveup.repository.FirebaseUserRepository
import com.mockingbird.sleeveup.service.AuthService
import com.mockingbird.sleeveup.service.FirestoreService
import com.mockingbird.sleeveup.service.StorageService

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
        color = MaterialTheme.colorScheme.onSurface,
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
                        tint = MaterialTheme.colorScheme.background
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
                        tint = MaterialTheme.colorScheme.background
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
fun ProfileContent(user: User, imageState: ProfileViewModel.ImageState){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (imageState){
            is ProfileViewModel.ImageState.Loading -> {
                Text(text = "Loading...")
            }
            is ProfileViewModel.ImageState.Success -> {
                val imageBytes = (imageState as ProfileViewModel.ImageState.Success).imageBytes
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
                    (imageState as ProfileViewModel.ImageState.Error).message
                Text("Error fetching profile image: $errorMessage")
            }
            else -> {}
        }



        Text(
            text = user.name ?: "",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.background
        )
        Text(text = user.title ?: "", style = MaterialTheme.typography.titleMedium, color =  MaterialTheme.colorScheme.background)

        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painterResource(id = R.drawable.baseline_location_on_24),
                contentDescription = "Location",
                modifier = Modifier.size(16.dp),
                tint =  MaterialTheme.colorScheme.background
            )
            Spacer(Modifier.width(4.dp))
            user.lokasi?.let { Text(it, color =  MaterialTheme.colorScheme.background) }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painterResource(id = R.drawable.baseline_loker_24),
                contentDescription = "Education",
                modifier = Modifier.size(16.dp),
                tint =  MaterialTheme.colorScheme.background
            )
            Spacer(Modifier.width(4.dp))
            user.education?.let { Text(it, color =  MaterialTheme.colorScheme.background) }
        }

        Spacer(Modifier.height(16.dp))

        var isBioExpanded by remember { androidx.compose.runtime.mutableStateOf(false) }
        ExpandableCard(
            title = "Tentang Saya",
            content = { Text(user.bio ?: "Isi dengan meng-edit profilmu!", color = MaterialTheme.colorScheme.background) },
            isExpanded = isBioExpanded,
            onExpandChange = { isBioExpanded = it },
            textColor =  MaterialTheme.colorScheme.background
        )


        var isExperienceExpanded by remember { androidx.compose.runtime.mutableStateOf(false) }
        ExpandableCard(
            title = "Pengalaman",
            content = { DisplayUserCredentials(items = user.experiences, textColor = MaterialTheme.colorScheme.background) },
            isExpanded = isExperienceExpanded,
            onExpandChange = { isExperienceExpanded = it },
            textColor =  MaterialTheme.colorScheme.background
        )
        var isProjectExpanded by remember { androidx.compose.runtime.mutableStateOf(false) }
        ExpandableCard(
            title = "Proyek",
            content = { DisplayUserCredentials(items = user.projects, textColor = MaterialTheme.colorScheme.background) },
            isExpanded = isProjectExpanded,
            onExpandChange = { isProjectExpanded = it },
            textColor =  MaterialTheme.colorScheme.background
        )

        var isCertificationExpanded by remember { androidx.compose.runtime.mutableStateOf(false) }
        ExpandableCard(
            title = "Sertifikasi",
            content = { DisplayUserCredentials(items = user.certifications, textColor = MaterialTheme.colorScheme.background) },
            isExpanded = isCertificationExpanded,
            onExpandChange = { isCertificationExpanded = it },
            textColor =  MaterialTheme.colorScheme.background
        )

        DisplayUserPendingApplications(
            items = user.pendingJobApplication, textColor =  MaterialTheme.colorScheme.background
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
                Text(text = title, fontWeight = FontWeight.Bold, color = textColor)
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
fun DisplayUserCredentials(items: Map<String, String>?, textColor: Color) {
    if (items == null || items.isEmpty()) {
        Text(
            text = "Saat ini belum ada.",
            style = MaterialTheme.typography.bodyMedium,
            color = textColor
        )
    } else {
        Column {
            items.forEach { (title, description) ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(text = title, fontWeight = FontWeight.Bold, color = textColor)
                    Spacer(Modifier.width(8.dp))
                    Text(text = description, color = textColor)
                }
            }
        }
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
            UserCredentialCard(title = jobOffer.profession, description = jobOffer.description)
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
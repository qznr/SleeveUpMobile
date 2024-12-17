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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mockingbird.sleeveup.R
import com.mockingbird.sleeveup.entity.JobOffer
import com.mockingbird.sleeveup.entity.User
import com.mockingbird.sleeveup.navigation.Screen
import com.mockingbird.sleeveup.repository.FirebaseUserRepository
import com.mockingbird.sleeveup.service.AuthService
import com.mockingbird.sleeveup.service.FirestoreService
import com.mockingbird.sleeveup.service.StorageService
import kotlinx.coroutines.launch

// Show and edit profile picture, full name, title, bio, projects, certificates, experiences
// After submitting profile or applying for job offer, it goes to the profile with Pending response status
// DO NOT REMOVE ABOVE COMMENT -zen

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

    var user by remember { mutableStateOf<User?>(null) }
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var loadingImage by remember{mutableStateOf<ByteArray?>(null)}
    val textColor = MaterialTheme.colorScheme.background
    val iconColor = MaterialTheme.colorScheme.background
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = userId) {
        scope.launch {
            try {
                // Loading image so that it doesn't have to load a loading animation -zen
                // Case: if the user image is deleted (by an admin or other means), instead of the
                // app showing "Loading..." text, just show the placeholder image in the meantime -zen
                loadingImage = storageService.fetchImage("placeholder.png")
                user = userRepository.getUser(userId)
                //var bytes: ByteArray? = null

                if (!user?.photoUrl.isNullOrBlank()) {
                    imageBytes = storageService.fetchImage(user!!.photoUrl!!)
                } else {
                    imageBytes = loadingImage
                }
            } catch (e: Exception) {
                // Handle error, user not found, etc.
                println("Error fetching user: $e")
                user = null
            }
        }
    }

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
                        tint = iconColor
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
                        tint = iconColor
                    )
                }

                /*Button(
                    onClick = { navController.popBackStack(Screen.Profile.route, inclusive = false) }, modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Back")
                }*/
            }

            if (user != null) {
                // Profile Information Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (user != null) {
                        // Profile Picture
                        if (imageBytes != null) {
                            AsyncImage(
                                model = imageBytes,
                                contentDescription = "User profile picture",
                                modifier = Modifier.size(128.dp)
                            )
                        }
                        Text(
                            text = user?.name ?: "",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(text = user?.title ?: "", style = MaterialTheme.typography.titleMedium, color = textColor)

                        Spacer(Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painterResource(id = R.drawable.baseline_location_on_24),
                                contentDescription = "Location",
                                modifier = Modifier.size(16.dp),
                                tint = iconColor
                            )
                            Spacer(Modifier.width(4.dp))
                            user?.lokasi?.let { Text(it, color = textColor) }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painterResource(id = R.drawable.baseline_loker_24),
                                contentDescription = "Education",
                                modifier = Modifier.size(16.dp),
                                tint = iconColor
                            )
                            Spacer(Modifier.width(4.dp))
                            user?.education?.let { Text(it, color = textColor) }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Bio, Experience, projects, certifications, and pending job applications in separate cards
                        var isBioExpanded by remember { mutableStateOf(false) }
                        ExpandableCard(
                            title = "Tentang Saya",
                            content = { Text(user?.bio ?: "Isi dengan meng-edit profilmu!", color = textColor) },
                            isExpanded = isBioExpanded,
                            onExpandChange = { isBioExpanded = it },
                            textColor = textColor
                        )


                        var isExperienceExpanded by remember { mutableStateOf(false) }
                        ExpandableCard(
                            title = "Pengalaman",
                            content = { DisplayUserCredentials(items = user?.experiences, textColor = textColor) },
                            isExpanded = isExperienceExpanded,
                            onExpandChange = { isExperienceExpanded = it },
                            textColor = textColor
                        )
                        // Project (Expandable)
                        var isProjectExpanded by remember { mutableStateOf(false) }
                        ExpandableCard(
                            title = "Proyek",
                            content = { DisplayUserCredentials(items = user?.projects, textColor = textColor) },
                            isExpanded = isProjectExpanded,
                            onExpandChange = { isProjectExpanded = it },
                            textColor = textColor
                        )

                        // Certifications (Expandable)
                        var isCertificationExpanded by remember { mutableStateOf(false) }
                        ExpandableCard(
                            title = "Sertifikasi",
                            content = { DisplayUserCredentials(items = user?.certifications, textColor = textColor) },
                            isExpanded = isCertificationExpanded,
                            onExpandChange = { isCertificationExpanded = it },
                            textColor = textColor
                        )

                        // Assuming DisplayUserPendingApplications handles the display
                        DisplayUserPendingApplications(
                            items = user?.pendingJobApplication, textColor = textColor
                        )

                    } else {
                        Text(text = "Loading user data...")
                    }
                }
            }
        }
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

// Here, "credentials" mean a user's competence i.e. what is their past projects, experiences, and certifications
// DO NOT REMOVE ABOVE COMMENT -zen

@Composable
fun DisplayUserCredentials(items: Map<String, String>?, textColor: Color) {
    if (items == null || items.isEmpty()) {
        Text(
            text = "Saat ini belum ada.",
            style = MaterialTheme.typography.bodyMedium,
            color = textColor
        )
    } else {
        Column {  // Tambahkan Column untuk menyusun item secara vertikal
            items.forEach { (title, description) ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) { // Tambahkan padding vertikal di sini
                    Text(text = title, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Text(text = description)
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

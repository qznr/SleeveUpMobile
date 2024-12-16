package com.mockingbird.sleeveup.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = userId) {
        scope.launch {
            try {
                // Loading image so that it doesn't have to load a loading animation -zen
                // Case: if the user image is deleted (by an admin or other means), instead of the
                // app showing "Loading..." text, just show the placeholder image in the meantime -zen
                loadingImage = storageService.fetchImage("placeholder.png")

                val fetchedUser = userRepository.getUser(userId)
                user = fetchedUser
                var bytes: ByteArray? = null

                if (!user?.photoUrl.isNullOrBlank()) {
                    bytes = storageService.fetchImage(user!!.photoUrl!!)
                    imageBytes = bytes
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

    Column {
        Row {
            Button(
                onClick = {
                    authService.signOut()
                    authService.signOutGoogle(googleSignInClient).addOnCompleteListener {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }, modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                Text(stringResource(R.string.logout))
            }

            Button(
                onClick = {
                    userId.let {
                        navController.navigate(Screen.EditUserProfile.createRoute(it))
                    }
                }, modifier = Modifier.fillMaxWidth(0.5f)
            ) { Text(stringResource(R.string.edit_profile)) }

            Button(
                onClick = { navController.popBackStack(Screen.Profile.route, inclusive = false) }, modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Back")
            }
        }

        if (user != null) {
            Text(text = "Name: ${user?.displayName ?: user?.name}")
            if (imageBytes != null) {
                AsyncImage(
                    model = imageBytes,
                    contentDescription = "User profile picture",
                    modifier = Modifier.size(128.dp)
                )
            } else {
                // If the actual image has not been loaded, load a loading or temporary image first
                // instead of showing "Loading..." text or an animation
                // But if you want to change this to an animation then sure -zen
                AsyncImage(
                    model = loadingImage,
                    contentDescription = "User profile picture",
                    modifier = Modifier.size(128.dp)
                )
            }
            Text(text = "Title: ${user?.title ?: "Isi dengan meng-edit profilmu!"}")
            Text(text = "Bio: ${user?.bio ?: "Isi dengan meng-edit profilmu!"}")

            Spacer(Modifier.height(16.dp))

            Text("Proyek", fontWeight = FontWeight.Bold)
            DisplayUserCredentials(items = user?.projects)

            Spacer(Modifier.height(16.dp))

            Text("Sertifikasi", fontWeight = FontWeight.Bold)
            DisplayUserCredentials(items = user?.certifications)

            Spacer(Modifier.height(16.dp))

            Text("Pengalaman", fontWeight = FontWeight.Bold)
            DisplayUserCredentials(items = user?.experiences)

            Spacer(Modifier.height(16.dp))

            Text("Sedang Dilamar", fontWeight = FontWeight.Bold)
            DisplayUserPendingApplications(items = user?.pendingJobApplication)
        } else {
            Text(text = "Loading user data...")
        }
    }
}

// Here, "credentials" mean a user's competence i.e. what is their past projects, experiences, and certifications
// DO NOT REMOVE ABOVE COMMENT -zen

@Composable
fun DisplayUserCredentials(items: Map<String, String>?) {
    if (items.isNullOrEmpty()) {
        Text(
            text = "Saat ini belum ada.",
            style = MaterialTheme.typography.bodyMedium
        )
    } else {
        items.forEach { (title, description) ->
            UserCredentialCard(title = title, description = description)
        }
    }
}

@Composable
fun DisplayUserPendingApplications(items: Map<String, JobOffer>?) {
    if (items.isNullOrEmpty()) {
        Text(
            text = "Saat ini belum ada.",
            style = MaterialTheme.typography.bodyMedium
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
            .padding(8.dp)
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

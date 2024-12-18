package com.mockingbird.sleeveup.screen

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.layout.ContentScale
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
import com.mockingbird.sleeveup.ui.theme.Moonstone
import com.mockingbird.sleeveup.ui.theme.TickleMePink
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically // Align items vertically
            ) {
                Text( // Title on the left
                    text = "Profil", // Assuming you have a string resource for "Profil"
                    style = MaterialTheme.typography.headlineMedium, // Or any appropriate style
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

            when (userState) {
                is ProfileViewModel.ProfileState.Loading -> {
                    Text(text = "Loading user data...")
                }
                is ProfileViewModel.ProfileState.Success -> {
                    val user = (userState as ProfileViewModel.ProfileState.Success).user
                    ProfileContent(user = user, imageState = imageState, navController = navController)
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
fun ProfileContent(user: User, imageState: ProfileViewModel.ImageState, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                when (imageState) {
                    is ProfileViewModel.ImageState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(128.dp),
                            color = White // Atau warna lain yang sesuai
                        )
                    }
                    is ProfileViewModel.ImageState.Success -> {
                        val imageBytes = imageState.imageBytes
                        if (imageBytes != null) {
                            AsyncImage(
                                model = imageBytes,
                                contentDescription = "User profile picture",
                                modifier = Modifier
                                    .size(128.dp)
                                    .clip(CircleShape), // Menambahkan clip untuk bentuk lingkaran
                                contentScale = ContentScale.Crop // Memastikan gambar dipotong agar sesuai dengan lingkaran
                            )
                        } else { // Menampilkan placeholder jika imageBytes null
                            Icon(
                                imageVector = Icons.Default.Person, // Atau ikon lain yang sesuai
                                contentDescription = "Placeholder profile picture",
                                modifier = Modifier
                                    .size(128.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray), // Warna latar belakang placeholder
                                tint = White // Warna ikon
                            )

                        }
                    }
                    is ProfileViewModel.ImageState.Error -> {
                        // ... (kode error handling)
                        Icon(
                            imageVector = Icons.Default.Person, // Atau ikon lain yang sesuai
                            contentDescription = "Placeholder profile picture",
                            modifier = Modifier
                                .size(128.dp)
                                .clip(CircleShape)
                                .background(Color.Gray), // Warna latar belakang placeholder
                            tint = White // Warna ikon
                        )
                    }
                    else -> {}
                }
            }
            IconButton(
                onClick = {
                    if (user.id != null) {
                        navController.navigate(Screen.EditUserProfile.createRoute(user.id))
                    } else {
                        // Tangani kasus di mana user.id null, misalnya:
                        //  - Tampilkan pesan error
                        //  - Navigasi ke layar lain
                        Log.e("ProfileScreen", "User ID is null. Cannot navigate to edit profile.")
                    }
                }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_edit_24),
                    contentDescription = "edit",
                    tint = White
                )
            }
        }

        Text(
            text = user.name ?: "No Name",  // Provide a default value
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = White
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 4.dp) // Optional padding
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (user.gender == "Male") MajorelieBlue else TickleMePink, // Conditional color
                modifier = Modifier.padding(vertical = 2.dp) // small vertical padding
            ) {
                Text(
                    text = user.gender ?: "No gender provided",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Moonstone, // Use a theme color or define your own
                modifier = Modifier.padding(vertical = 2.dp) // small vertical padding

            ) {
                Text(
                    text = user.status ?: "No status provided",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

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
            previewContent = { Text(text = (user.bio ?: "Isi dengan meng-edit profilmu!").take(50), color = White) }, // Preview 50 karakter pertama
            fullContent = { Text(text = user.bio ?: "Isi dengan meng-edit profilmu!", color = White) },
            isExpanded = isBioExpanded,
            onExpandChange = { isBioExpanded = it },
            color = MajorelieBlue,
            showEditButton = true,  // Pastikan ini true jika ingin menampilkan tombol edit
            onEditClick = {
                // Aksi yang akan dilakukan saat tombol edit diklik, misalnya:
                // Navigasi ke layar edit profil
                // Menampilkan dialog edit
                // dll.
                //Contoh: navController.navigate(Screen.EditUserProfile.createRoute(user.id!!))
            }
        )

        var isExperienceExpanded by remember { mutableStateOf(false) }
        ExpandableCard(
            title = "Pengalaman",
            previewContent = {
                val experiencesToShow = user.experiences?.take(2) ?: emptyList() // Menampilkan maksimal 2 pengalaman
                DisplayUserCredentials(items = experiencesToShow, textColor = White)
            },
            fullContent = { DisplayUserCredentials(items = user.experiences, textColor = White) },
            isExpanded = isExperienceExpanded,
            onExpandChange = { isExperienceExpanded = it },
            color = MajorelieBlue,
            showEditButton = true,  // Pastikan ini true jika ingin menampilkan tombol edit
            onEditClick = {
                // Aksi yang akan dilakukan saat tombol edit diklik, misalnya:
                // Navigasi ke layar edit profil
                // Menampilkan dialog edit
                // dll.
                //Contoh: navController.navigate(Screen.EditUserProfile.createRoute(user.id!!))
            }
        )
        var isProjectExpanded by remember { mutableStateOf(false) }
        ExpandableCard(
            title = "Proyek",
            previewContent = {
                val projectToShow = user.projects?.take(2) ?: emptyList() // Menampilkan maksimal 2 pengalaman
                DisplayUserCredentials(items = projectToShow, textColor = White)
            },
            fullContent = { DisplayUserCredentials(items = user.projects, textColor = White) },
            isExpanded = isProjectExpanded,
            onExpandChange = { isProjectExpanded = it },
            color = MajorelieBlue,
            showEditButton = true,  // Pastikan ini true jika ingin menampilkan tombol edit
            onEditClick = {
                // Aksi yang akan dilakukan saat tombol edit diklik, misalnya:
                // Navigasi ke layar edit profil
                // Menampilkan dialog edit
                // dll.
                //Contoh: navController.navigate(Screen.EditUserProfile.createRoute(user.id!!))
            }
        )

        var isCertificationExpanded by remember { mutableStateOf(false) }
        ExpandableCard(
            title = "Sertifikasi",
            previewContent = {
                val certificateToShow = user.experiences?.take(2) ?: emptyList() // Menampilkan maksimal 2 pengalaman
                DisplayUserCredentials(items = certificateToShow, textColor = White)
            },
            fullContent = { DisplayUserCredentials(items = user.certifications, textColor = White) },
            isExpanded = isCertificationExpanded,
            onExpandChange = { isCertificationExpanded = it },
            color = MajorelieBlue,
            showEditButton = true,  // Pastikan ini true jika ingin menampilkan tombol edit
            onEditClick = {
                // Aksi yang akan dilakukan saat tombol edit diklik, misalnya:
                // Navigasi ke layar edit profil
                // Menampilkan dialog edit
                // dll.
                //Contoh: navController.navigate(Screen.EditUserProfile.createRoute(user.id!!))
            }
        )

        DisplayUserPendingApplications(
            items = user.pendingJobApplication, textColor = White
        )

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableCard(
    title: String,
    previewContent: @Composable () -> Unit,
    fullContent: @Composable () -> Unit,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    showEditButton: Boolean = true,
    onEditClick: (() -> Unit)? = null,
    color: Color
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
                horizontalArrangement = Arrangement.SpaceBetween, // Mengatur space di antara elemen
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = MajorelieBlue,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f) // Memberi bobot pada judul agar mengisi sisa ruang
                )

                if (showEditButton && onEditClick != null) {
                    IconButton(onClick = { onEditClick() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_edit_24),
                            contentDescription = "Edit",
                            tint = White
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.Start) { // Tambahkan Column untuk membungkus konten
                if (!isExpanded) {
                    previewContent()
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 4.dp),
                        thickness = 1.dp,
                        color = White.copy(alpha = 0.5f)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically, // Vertikal alignment
                        horizontalArrangement = Arrangement.Start // Horizontal alignment
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Lihat Selengkapnya",
                            tint = White,
                            modifier = Modifier.size(24.dp) // Ubah ukuran icon jika perlu
                        )
                        Spacer(Modifier.width(4.dp))
                        TextButton(onClick = { onExpandChange(true) }) {
                            Text(
                                "Lihat Selengkapnya",
                                style = MaterialTheme.typography.labelSmall,
                                color = White
                            )
                        }
                    }
                } else {
                    fullContent()
                }
            }

            // Tombol panah ditampilkan di bawah full content saat expanded
            if (isExpanded) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = { onExpandChange(false) }) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowUp,
                            contentDescription = "Collapse",
                            tint = White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DisplayUserCredentials(items: Collection<*>?, textColor: Color) {
    val safeItems = items ?: emptyList<Any>() // Menangani kemungkinan null

    if (safeItems.isEmpty()) {
        Text("Saat ini belum ada.", style = MaterialTheme.typography.bodyMedium, color = textColor)
    } else {
        Column {
            safeItems.forEach { item ->
                when (item) {
                    is Project -> UserCredentialItem(title = item.name ?: "No Name", description = item.description ?: "No Description", textColor = textColor)
                    is Certificate -> UserCredentialItem(title = item.name ?: "No Name", description = item.type ?: "No Type", textColor = textColor)
                    is Experience -> UserCredentialItem(title = item.name ?: "No Name", description = item.description ?: "No Description", textColor = textColor)
                    // Tambahkan penanganan tipe data lain jika ada
                    else -> {}
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
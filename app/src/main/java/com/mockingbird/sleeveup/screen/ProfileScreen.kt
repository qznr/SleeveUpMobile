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
import androidx.compose.ui.text.style.TextOverflow
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
                    style = MaterialTheme.typography.headlineSmall, // Or any appropriate style
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
                            color = White
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
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Placeholder profile picture",
                                modifier = Modifier
                                    .size(128.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray),
                                tint = White
                            )
                        }
                    }
                    is ProfileViewModel.ImageState.Error -> {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Placeholder profile picture",
                            modifier = Modifier
                                .size(128.dp)
                                .clip(CircleShape)
                                .background(Color.Gray),
                            tint = White
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
            modifier = Modifier.padding(top = 4.dp), // Optional padding
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

        Column(horizontalAlignment = Alignment.Start) {
            Row( verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(id = R.drawable.baseline_location_on_24),
                    contentDescription = "Location",
                    modifier = Modifier.size(24.dp),
                    tint = White
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = user.lokasi ?: "No Location Provided",
                    color = White,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(id = R.drawable.baseline_loker_24),
                    contentDescription = "Education",
                    modifier = Modifier.size(24.dp),
                    tint = White
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = user.education ?: "No Education Provided",
                    color = White,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        var isBioExpanded by remember { mutableStateOf(false) }
        val isBioExpandable = (user.bio?.length ?: 0) > 50
        ExpandableCard(
            title = "Tentang Saya",
            previewContent = {
                Text(text = (user.bio ?: "Isi dengan meng-edit profilmu!").take(50), color = White, style = MaterialTheme.typography.bodyMedium, overflow = TextOverflow.Ellipsis) },
            fullContent = { Text(text = user.bio ?: "Isi dengan meng-edit profilmu!", color = White, style = MaterialTheme.typography.bodyMedium) },
            isExpanded = isBioExpanded,
            onExpandChange = { isBioExpanded = it },
            color = MajorelieBlue,
            showEditButton = false,  // Pastikan ini true jika ingin menampilkan tombol edit
            isExpandable = isBioExpandable
        )

        var isExperienceExpanded by remember { mutableStateOf(false) }
        val isExperienceExpandable = (user.experiences?.size ?: 0) > 2
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
            showEditButton = false,
            isExpandable = isExperienceExpandable
        )
        var isProjectExpanded by remember { mutableStateOf(false) }
        val isProjectExpandable = (user.projects?.size ?: 0) > 2
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
            showEditButton = false,
            isExpandable = isProjectExpandable
        )

        var isCertificationExpanded by remember { mutableStateOf(false) }
        val isCertificationExpandable = (user.certifications?.size ?: 0) > 2
        ExpandableCard(
            title = "Sertifikasi",
            previewContent = {
                val certificateToShow = user.certifications?.take(2) ?: emptyList() // Menampilkan maksimal 2 pengalaman
                DisplayUserCredentials(items = certificateToShow, textColor = White)
            },
            fullContent = { DisplayUserCredentials(items = user.certifications, textColor = White) },
            isExpanded = isCertificationExpanded,
            onExpandChange = { isCertificationExpanded = it },
            color = MajorelieBlue,
            showEditButton = false,
            isExpandable = isCertificationExpandable
        )

        var isApplicationExpanded by remember { mutableStateOf(false) }
        val isApplicationExpandable = (user.pendingJobApplication?.size ?: 0) > 0
        ExpandableCard(
            title = "Lamaran Kerja",
            previewContent = {
                Text(text = if (user.pendingJobApplication.isNullOrEmpty()) "Tidak ada lamaran yang tertunda" else "Ada ${user.pendingJobApplication?.size} lamaran yang tertunda", color = White, style = MaterialTheme.typography.bodyMedium)
            },
            fullContent = {
                DisplayUserPendingApplications(items = user.pendingJobApplication, textColor = White)
            },
            isExpanded = isApplicationExpanded,
            onExpandChange = { isApplicationExpanded = it },
            color = MajorelieBlue,
            isExpandable = isApplicationExpandable
        )

    }
}

@Composable
fun ExpandableCard(
    title: String,
    previewContent: @Composable () -> Unit,
    fullContent: @Composable () -> Unit,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    showEditButton: Boolean = true,
    onEditClick: (() -> Unit)? = null,
    color: Color,
    isExpandable: Boolean = true // New parameter
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color.Gray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = MajorelieBlue,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
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

            Column(horizontalAlignment = Alignment.Start) {
                if (!isExpanded) {
                    previewContent()
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 4.dp),
                        thickness = 1.dp,
                        color = White.copy(alpha = 0.5f)
                    )
                    if(isExpandable){
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
                                    style = MaterialTheme.typography.labelLarge,
                                    color = White
                                )
                            }
                        }
                    }
                } else {
                    fullContent()
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 4.dp),
                        thickness = 1.dp,
                        color = White.copy(alpha = 0.5f)
                    )
                }
            }

            // Tombol panah ditampilkan di bawah full content saat expanded
            if (isExpanded && isExpandable) {
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
                UserCredentialItem(item = item, textColor = textColor)
            }
        }
    }
}

@Composable
fun UserCredentialItem(item: Any?, textColor: Color) {
    when (item) {
        is Project -> ProjectCredentialItem(project = item, textColor = textColor)
        is Certificate -> CertificateCredentialItem(certificate = item, textColor = textColor)
        is Experience -> ExperienceCredentialItem(experience = item, textColor = textColor)
        else -> {
            Text("Invalid Data", color = textColor)
        }
    }
}

@Composable
fun ProjectCredentialItem(project: Project, textColor: Color) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = project.name ?: "No Name", fontWeight = FontWeight.Bold, color = textColor, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.width(4.dp))
        project.description?.let {
            Text(text = it, color = textColor, style = MaterialTheme.typography.bodyMedium)
        }
        project.type?.let {
            Text(text = "Type: $it", color = textColor, style = MaterialTheme.typography.bodyMedium)
        }
        project.startDate?.let {
            Text(text = "Start Date: $it", color = textColor, style = MaterialTheme.typography.bodyMedium)
        }
        project.endDate?.let {
            Text(text = "End Date: $it", color = textColor, style = MaterialTheme.typography.bodyMedium)
        }
        project.link?.let {
            Text(text = "Link: $it", color = textColor, style = MaterialTheme.typography.bodyMedium)
        }

    }
}


@Composable
fun CertificateCredentialItem(certificate: Certificate, textColor: Color) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = certificate.name ?: "No Name", fontWeight = FontWeight.Bold, color = textColor, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.width(4.dp))
        certificate.type?.let {
            Text(text = "Type: $it", color = textColor, style = MaterialTheme.typography.bodyMedium)
        }
        certificate.startDate?.let {
            Text(text = "Start Date: $it", color = textColor, style = MaterialTheme.typography.bodyMedium)
        }
        certificate.link?.let {
            Text(text = "Link: $it", color = textColor, style = MaterialTheme.typography.bodyMedium)
        }
        certificate.skills?.let { skills ->
            if (skills.isNotEmpty()) {
                Text(text ="Skills: ${skills.joinToString(", ")}", color = textColor, style = MaterialTheme.typography.bodyMedium)
            }
        }
        certificate.tools?.let { tools ->
            if (tools.isNotEmpty()) {
                Text(text = "Tools: ${tools.joinToString(", ")}", color = textColor, style = MaterialTheme.typography.bodyMedium)
            }
        }

    }
}

@Composable
fun ExperienceCredentialItem(experience: Experience, textColor: Color) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = experience.name ?: "No Name", fontWeight = FontWeight.Bold, color = textColor, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.width(4.dp))
        experience.description?.let {
            Text(text = it, color = textColor, style = MaterialTheme.typography.bodyMedium)
        }
        experience.role?.let {
            Text(text = "Role: $it", color = textColor, style = MaterialTheme.typography.bodyMedium)
        }
        experience.location?.let {
            Text(text = "Location: $it", color = textColor, style = MaterialTheme.typography.bodyMedium)
        }
        experience.startDate?.let {
            Text(text = "Start Date: $it", color = textColor, style = MaterialTheme.typography.bodyMedium)
        }
        experience.endDate?.let {
            Text(text = "End Date: $it", color = textColor, style = MaterialTheme.typography.bodyMedium)
        }
        experience.skills?.let { skills ->
            if (skills.isNotEmpty()) {
                Text(text = "Skills: ${skills.joinToString(", ")}", color = textColor, style = MaterialTheme.typography.bodyMedium)
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
        Column {
            items.forEach { (_, jobOffer) ->
                UserCredentialCard(
                    title = jobOffer.profession ?: "No Profession",
                    description = jobOffer.description ?: "No Description"
                )
            }
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
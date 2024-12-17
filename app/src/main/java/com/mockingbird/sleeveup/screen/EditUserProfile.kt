package com.mockingbird.sleeveup.screen

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mockingbird.sleeveup.R
import com.mockingbird.sleeveup.entity.Certificate
import com.mockingbird.sleeveup.entity.Experience
import com.mockingbird.sleeveup.entity.JobOffer
import com.mockingbird.sleeveup.entity.Project
import com.mockingbird.sleeveup.entity.User
import com.mockingbird.sleeveup.factory.EditProfileViewModelFactory
import com.mockingbird.sleeveup.model.EditProfileViewModel
import com.mockingbird.sleeveup.navigation.Screen
import com.mockingbird.sleeveup.repository.FirebaseUserRepository
import com.mockingbird.sleeveup.service.FirestoreService
import com.mockingbird.sleeveup.service.StorageService
import java.util.UUID

@Composable
fun EditUserProfileScreen(
    modifier: Modifier = Modifier, navController: NavController, userId: String
) {
    val firestore = FirebaseFirestore.getInstance()
    val firestoreService = FirestoreService(firestore)
    val userRepository = FirebaseUserRepository(firestoreService)
    val storageService = StorageService()
    val authService = FirebaseAuth.getInstance()
    val viewModelFactory =
        EditProfileViewModelFactory(authService, userRepository, storageService, navController)
    val viewModel: EditProfileViewModel = viewModel(factory = viewModelFactory)

    val userState by viewModel.userState.collectAsState()

    var name by remember { mutableStateOf("") }
    // Removed title
    var bio by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf<String?>(null) }
    var imageDestinationPath: String? by remember { mutableStateOf(null) }
    var education by remember { mutableStateOf("") }
    var lokasi by remember { mutableStateOf("") }
    val projects = remember { mutableStateListOf<Project>() }
    val certifications = remember { mutableStateListOf<Certificate>() }
    val experiences = remember { mutableStateListOf<Experience>() }
    val pendingJobApplications = remember { mutableStateListOf<Pair<String, JobOffer>>() }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var uploadState by remember { mutableStateOf<UploadState>(UploadState.Idle) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    LaunchedEffect(key1 = userId) {
        viewModel.fetchUser(userId)
    }

    when (userState) {
        is EditProfileViewModel.EditProfileState.Loading -> {
            Text(text = "Loading...")
        }

        is EditProfileViewModel.EditProfileState.Success -> {
            val user = (userState as EditProfileViewModel.EditProfileState.Success).user
            // Initialize state with current user data
            LaunchedEffect(user) {
                name = user.displayName ?: user.name ?: ""
                //  title = user.title ?: ""
                bio = user.bio ?: ""
                gender = user.gender
                status = user.status
                education = user.education ?: ""
                lokasi = user.lokasi ?: ""
                imageDestinationPath = null

                projects.clear()
                projects.addAll(user.projects ?: emptyList())

                certifications.clear()
                certifications.addAll(user.certifications ?: emptyList())

                experiences.clear()
                experiences.addAll(user.experiences ?: emptyList())

                pendingJobApplications.clear()
                pendingJobApplications.addAll(user.pendingJobApplication?.toList() ?: emptyList())
            }

            EditUserProfileContent(name = name,
                onNameChange = { name = it },
                //title = title, //remove title
                // onTitleChange = { title = it }, // remove title
                bio = bio,
                onBioChange = { bio = it },
                gender = gender,
                onGenderChange = {gender = it},
                status = status,
                onStatusChange = { status = it },
                education = education,
                onEducationChange = { education = it },
                lokasi = lokasi,
                onLokasiChange = { lokasi = it },
                projects = projects,
                onProjectsChange = { list ->
                    projects.clear()
                    projects.addAll(list)
                },
                certifications = certifications,
                onCertificationsChange = { list ->
                    certifications.clear()
                    certifications.addAll(list)
                },
                experiences = experiences,
                onExperiencesChange = { list ->
                    experiences.clear()
                    experiences.addAll(list)
                },
                pendingJobApplications = pendingJobApplications,
                onRemoveApplication = { jobOfferId ->
                    user.let { viewModel.removeJobApplication(it, jobOfferId) }
                },
                imageDestinationPath = imageDestinationPath,
                selectedImageUri = selectedImageUri,
                onImageUploadClick = {
                    uploadState = UploadState.PendingUpload
                    imagePickerLauncher.launch("image/*")
                },
                onSaveClick = {
                    val updatedUser = User(
                        id = user.id,
                        name = name,
                        displayName = name,
                        // title = title, //remove title
                        bio = bio,
                        gender = gender,
                        status = status,
                        education = education,
                        lokasi = lokasi,
                        photoUrl = if (imageDestinationPath == null) user.photoUrl else imageDestinationPath.toString(),
                        projects = projects.filterNot { it.name.isBlank() && it.description.isBlank() },
                        certifications = certifications.filterNot { it.name.isBlank() },
                        experiences = experiences.filterNot { it.name.isBlank() && it.description.isBlank() },
                        pendingJobApplication = pendingJobApplications.filterNot { it.first.isBlank() && it.second.description.isBlank() }
                            .associate { it.first to it.second }
                    )
                    viewModel.updateUser(updatedUser)
                },
                navController = navController,
                uploadState = uploadState,
                onUpload = { uri ->
                    uploadState = UploadState.Loading
                    if (uri != null) {
                        val uniqueImageName = UUID.randomUUID().toString()
                        val destinationPath = "user/$uniqueImageName"
                        viewModel.uploadImage(uri, destinationPath, user) { success, _ ->
                            if (success) {
                                uploadState = UploadState.Success
                                imageDestinationPath = destinationPath
                            } else {
                                uploadState = UploadState.Error("Image Upload failed")
                            }
                        }
                    } else {
                        uploadState =
                            UploadState.Error("No image selected or user data not available.")
                    }
                })
        }

        is EditProfileViewModel.EditProfileState.Error -> {
            Text(text = "Error loading user data: ${(userState as EditProfileViewModel.EditProfileState.Error).message}")
        }

        else -> {}
    }

    when (viewModel.updateState.collectAsState().value) {
        is EditProfileViewModel.UpdateState.Success -> {
            LaunchedEffect(Unit) {
                navController.popBackStack()
                navController.navigate(Screen.UserProfile.createRoute(userId))
            }
        }

        is EditProfileViewModel.UpdateState.Error -> {
            Text(text = "Error updating profile: ${(viewModel.updateState.collectAsState().value as EditProfileViewModel.UpdateState.Error).message}")
        }

        else -> {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserProfileContent(
    name: String,
    onNameChange: (String) -> Unit,
    //  title: String, //remove title
    //  onTitleChange: (String) -> Unit, //remove title
    bio: String,
    onBioChange: (String) -> Unit,
    gender: String?,
    onGenderChange: (String?) -> Unit,
    status: String?,
    onStatusChange: (String?) -> Unit,
    education: String,
    onEducationChange: (String) -> Unit,
    lokasi: String,
    onLokasiChange: (String) -> Unit,
    projects: SnapshotStateList<Project>,
    onProjectsChange: (SnapshotStateList<Project>) -> Unit,
    certifications: SnapshotStateList<Certificate>,
    onCertificationsChange: (SnapshotStateList<Certificate>) -> Unit,
    experiences: SnapshotStateList<Experience>,
    onExperiencesChange: (SnapshotStateList<Experience>) -> Unit,
    pendingJobApplications: SnapshotStateList<Pair<String, JobOffer>>,
    onRemoveApplication: (String) -> Unit,
    imageDestinationPath: String?,
    selectedImageUri: Uri?,
    onImageUploadClick: () -> Unit,
    onSaveClick: () -> Unit,
    navController: NavController,
    uploadState: UploadState,
    onUpload: (Uri?) -> Unit
) {
    val genderOptions = listOf("Male", "Female", "Other", "Prefer not to say")
    var genderExpanded by remember { mutableStateOf(false) }
    val statusOptions = listOf("Available", "Not Available")
    var statusExpanded by remember { mutableStateOf(false) }


    var profileImageIsAlreadyUploadedSuccessfully by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.name_hint)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = onImageUploadClick, content = { Text("Upload profile image baru!") })
            Spacer(modifier = Modifier.height(8.dp))

            // Upload button only shows if the image uri is not null
            if (selectedImageUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(selectedImageUri)
                        .build(), contentDescription = "Your new profile image"
                )
                Button(
                    onClick = { onUpload(selectedImageUri) },
                    enabled = (uploadState != UploadState.Loading) && !profileImageIsAlreadyUploadedSuccessfully
                ) {
                    Text("Upload sekarang!")
                }
                when (uploadState) {
                    // The Idle state is logically not needed but Kotlin will explode without this idle condition
                    is UploadState.Idle -> Log.d("EditUserProfile", "Menunggu input dari user...")
                    // The PendingUpload state is to control the "Save changes" button; if the image has not been uploaded
                    // yet, then the user MUST upload it first before saving changes
                    is UploadState.PendingUpload -> Log.d(
                        "EditUserProfile",
                        "Image harus di-upload terlebih dahulu sebelum user dapat save changes..."
                    )

                    is UploadState.Loading -> CircularProgressIndicator()
                    is UploadState.Success -> {
                        Text("Image berhasil di-upload!")
                        // If image is successfully uploaded, forcefully set this to true because you don't need to
                        // upload it again
                        profileImageIsAlreadyUploadedSuccessfully = true
                    }

                    is UploadState.Error -> Text("Image gagal di-upload: ${uploadState.message}")
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            ExposedDropdownMenuBox(
                expanded = genderExpanded,
                onExpandedChange = { genderExpanded = !genderExpanded }
            ) {
                OutlinedTextField(
                    value = gender ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Gender") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { genderExpanded = false }
                ) {
                    genderOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(text = option) },
                            onClick = {
                                onGenderChange(option)
                                genderExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))


            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = !statusExpanded }
            ) {
                OutlinedTextField(
                    value = status ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false }
                ) {
                    statusOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(text = option) },
                            onClick = {
                                onStatusChange(option)
                                statusExpanded = false
                            }
                        )
                    }
                }
            }


            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = education,
                onValueChange = onEducationChange,
                label = { Text(text = "Education") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = lokasi,
                onValueChange = onLokasiChange,
                label = { Text(text = "Lokasi") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = bio,
                onValueChange = onBioChange,
                label = { Text(text = "Bio") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Projects", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = {
                    projects.add(Project(name = "", description = ""))
                }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Filled.Add,
                        contentDescription = "Add project"
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        itemsIndexed(projects) { index, project ->
            ProjectItem(project = project, onProjectChange = { changedProject ->
                projects[index] = changedProject
            }, onRemoveProject = {
                if (projects.size > 0) {
                    projects.removeAt(index)
                }
            })
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Certifications", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = {
                    certifications.add(Certificate(name = ""))
                }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Filled.Add,
                        contentDescription = "Add certification"
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        itemsIndexed(certifications) { index, certification ->
            CertificationItem(certification = certification,
                onCertificationChange = { changedCertification ->
                    certifications[index] = changedCertification
                },
                onRemoveCertification = {
                    if (certifications.size > 0) {
                        certifications.removeAt(index)
                    }
                })
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Experiences", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = {
                    experiences.add(Experience(name = "", description = "", role = "", startDate = ""))
                }) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Filled.Add,
                        contentDescription = "Add experience"
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        itemsIndexed(experiences) { index, experience ->
            ExperienceItem(experience = experience, onExperienceChange = { changedExperience ->
                experiences[index] = changedExperience
            }, onRemoveExperience = {
                if (experiences.size > 0) {
                    experiences.removeAt(index)
                }
            })
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sedang Dilamar", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        itemsIndexed(pendingJobApplications) { index, application ->
            PendingApplicationItem(application = application, onRemoveApplication = {
                onRemoveApplication(application.first)
            })
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            // If the user uploads a new profile image and it has not been uploaded yet (by tapping
            // the upload button), then the user MUST upload it first before saving changes
            Button(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !(uploadState == UploadState.PendingUpload || uploadState == UploadState.Loading)
            ) {
                Text(text = "Save Changes")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Cancel")
            }
        }
    }
}

@Composable
fun ProjectItem(
    project: Project,
    onProjectChange: (Project) -> Unit,
    onRemoveProject: () -> Unit
) {
    Column {
        OutlinedTextField(value = project.name,
            onValueChange = { onProjectChange(project.copy(name = it)) },
            label = { Text("Project Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(value = project.description,
            onValueChange = { onProjectChange(project.copy(description = it)) },
            label = { Text("Project Description") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = project.type ?: "",
            onValueChange = { onProjectChange(project.copy(type = it)) },
            label = { Text("Project Type") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = project.startDate ?: "",
            onValueChange = { onProjectChange(project.copy(startDate = it)) },
            label = { Text("Project Start Date") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = project.endDate ?: "",
            onValueChange = { onProjectChange(project.copy(endDate = it)) },
            label = { Text("Project End Date") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = project.link ?: "",
            onValueChange = { onProjectChange(project.copy(link = it)) },
            label = { Text("Project Link") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onRemoveProject) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Filled.Delete,
                    contentDescription = "Remove project"
                )
            }
        }
    }
}

@Composable
fun CertificationItem(
    certification: Certificate,
    onCertificationChange: (Certificate) -> Unit,
    onRemoveCertification: () -> Unit
) {
    Column {
        OutlinedTextField(
            value = certification.name,
            onValueChange = { onCertificationChange(certification.copy(name = it)) },
            label = { Text("Certification Title") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = certification.type ?: "",
            onValueChange = { onCertificationChange(certification.copy(type = it)) },
            label = { Text("Certification Type") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = certification.startDate ?: "",
            onValueChange = { onCertificationChange(certification.copy(startDate = it)) },
            label = { Text("Certification Start Date") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = certification.link ?: "",
            onValueChange = { onCertificationChange(certification.copy(link = it)) },
            label = { Text("Certification Link") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = certification.skills?.joinToString(", ") ?: "",
            onValueChange = {
                onCertificationChange(certification.copy(skills = it.split(",").map { it.trim() }))
            },
            label = { Text("Certification Skills (comma-separated)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = certification.tools?.joinToString(", ") ?: "",
            onValueChange = {
                onCertificationChange(certification.copy(tools = it.split(",").map { it.trim() }))
            },
            label = { Text("Certification Tools (comma-separated)") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onRemoveCertification) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Filled.Delete,
                    contentDescription = "Remove certification"
                )
            }
        }
    }
}

@Composable
fun ExperienceItem(
    experience: Experience,
    onExperienceChange: (Experience) -> Unit,
    onRemoveExperience: () -> Unit
) {
    Column {
        OutlinedTextField(
            value = experience.name,
            onValueChange = { onExperienceChange(experience.copy(name = it)) },
            label = { Text("Experience Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = experience.description,
            onValueChange = { onExperienceChange(experience.copy(description = it)) },
            label = { Text("Experience Description") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = experience.role,
            onValueChange = { onExperienceChange(experience.copy(role = it)) },
            label = { Text("Experience Role") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = experience.startDate,
            onValueChange = { onExperienceChange(experience.copy(startDate = it)) },
            label = { Text("Experience Start Date") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = experience.endDate ?: "",
            onValueChange = { onExperienceChange(experience.copy(endDate = it)) },
            label = { Text("Experience End Date") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = experience.location ?: "",
            onValueChange = { onExperienceChange(experience.copy(location = it)) },
            label = { Text("Experience Location") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = experience.skills?.joinToString(", ") ?: "",
            onValueChange = {
                onExperienceChange(experience.copy(skills = it.split(",").map { it.trim() }))
            },
            label = { Text("Experience Skills (comma-separated)") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onRemoveExperience) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Filled.Delete,
                    contentDescription = "Remove experience"
                )
            }
        }
    }
}

@Composable
fun PendingApplicationItem(
    application: Pair<String, JobOffer>, onRemoveApplication: () -> Unit
) {
    Column {
        Text(
            text = application.second.profession,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = application.second.description,
            style = MaterialTheme.typography.bodyMedium,
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onRemoveApplication) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Filled.Delete,
                    contentDescription = "Remove Application"
                )
            }
        }
    }
}

sealed class UploadState {
    object Idle : UploadState()
    object PendingUpload : UploadState()
    object Loading : UploadState()
    object Success : UploadState()
    data class Error(val message: String) : UploadState()
}
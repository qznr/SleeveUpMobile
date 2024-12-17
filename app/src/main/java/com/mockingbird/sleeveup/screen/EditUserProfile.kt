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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mockingbird.sleeveup.R
import com.mockingbird.sleeveup.entity.JobOffer
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

    var name by remember { mutableStateOf(TextFieldValue("")) }
    var title by remember { mutableStateOf(TextFieldValue("")) }
    var bio by remember { mutableStateOf(TextFieldValue("")) }
    var imageDestinationPath: String? by remember { mutableStateOf(null) }
    val projects = remember { mutableStateListOf<Pair<TextFieldValue, TextFieldValue>>() }
    val certifications = remember { mutableStateListOf<Pair<TextFieldValue, TextFieldValue>>() }
    val experiences = remember { mutableStateListOf<Pair<TextFieldValue, TextFieldValue>>() }
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
                name = TextFieldValue(user.displayName ?: user.name ?: "")
                title = TextFieldValue(user.title ?: "")
                bio = TextFieldValue(user.bio ?: "")
                imageDestinationPath = null

                projects.clear()
                projects.addAll(user.projects?.map { (k, v) ->
                    Pair(TextFieldValue(k), TextFieldValue(v))
                } ?: emptyList())

                certifications.clear()
                certifications.addAll(user.certifications?.map { (k, v) ->
                    Pair(TextFieldValue(k), TextFieldValue(v))
                } ?: emptyList())

                experiences.clear()
                experiences.addAll(user.experiences?.map { (k, v) ->
                    Pair(TextFieldValue(k), TextFieldValue(v))
                } ?: emptyList())

                pendingJobApplications.clear()
                pendingJobApplications.addAll(user.pendingJobApplication?.map { (k, v) ->
                    Pair(k, v)
                } ?: emptyList())
            }

            EditUserProfileContent(name = name,
                onNameChange = { name = it },
                title = title,
                onTitleChange = { title = it },
                bio = bio,
                onBioChange = { bio = it },
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
                imageDestinationPath = imageDestinationPath,
                selectedImageUri = selectedImageUri,
                onImageUploadClick = {
                    uploadState = UploadState.PendingUpload
                    imagePickerLauncher.launch("image/*")
                },
                onSaveClick = {
                    val updatedUser = User(id = user.id,
                        name = name.text,
                        displayName = name.text,
                        title = title.text,
                        bio = bio.text,
                        photoUrl = if (imageDestinationPath == null) user.photoUrl else imageDestinationPath.toString(),
                        projects = projects.filterNot { it.first.text.isBlank() && it.second.text.isBlank() }
                            .associate { it.first.text to it.second.text },
                        certifications = certifications.filterNot { it.first.text.isBlank() && it.second.text.isBlank() }
                            .associate { it.first.text to it.second.text },
                        experiences = experiences.filterNot { it.first.text.isBlank() && it.second.text.isBlank() }
                            .associate { it.first.text to it.second.text },
                        pendingJobApplication = pendingJobApplications.filterNot { it.first.isBlank() && it.second.description.isBlank() }
                            .associate { it.first to it.second })
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

@Composable
fun EditUserProfileContent(
    name: TextFieldValue,
    onNameChange: (TextFieldValue) -> Unit,
    title: TextFieldValue,
    onTitleChange: (TextFieldValue) -> Unit,
    bio: TextFieldValue,
    onBioChange: (TextFieldValue) -> Unit,
    projects: SnapshotStateList<Pair<TextFieldValue, TextFieldValue>>,
    onProjectsChange: (SnapshotStateList<Pair<TextFieldValue, TextFieldValue>>) -> Unit,
    certifications: SnapshotStateList<Pair<TextFieldValue, TextFieldValue>>,
    onCertificationsChange: (SnapshotStateList<Pair<TextFieldValue, TextFieldValue>>) -> Unit,
    experiences: SnapshotStateList<Pair<TextFieldValue, TextFieldValue>>,
    onExperiencesChange: (SnapshotStateList<Pair<TextFieldValue, TextFieldValue>>) -> Unit,
    pendingJobApplications: SnapshotStateList<Pair<String, JobOffer>>,
    imageDestinationPath: String?,
    selectedImageUri: Uri?,
    onImageUploadClick: () -> Unit,
    onSaveClick: () -> Unit,
    navController: NavController,
    uploadState: UploadState,
    onUpload: (Uri?) -> Unit
) {
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

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text(text = "Title (e.g., Undergraduate in CS at X University)") },
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
                    projects.add(Pair(TextFieldValue(""), TextFieldValue("")))
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
                    certifications.add(Pair(TextFieldValue(""), TextFieldValue("")))
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
                    experiences.add(Pair(TextFieldValue(""), TextFieldValue("")))
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
                if (pendingJobApplications.size > 0) {
                    pendingJobApplications.removeAt(index)
                }
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
    project: Pair<TextFieldValue, TextFieldValue>,
    onProjectChange: (Pair<TextFieldValue, TextFieldValue>) -> Unit,
    onRemoveProject: () -> Unit
) {
    Column {
        OutlinedTextField(value = project.first,
            onValueChange = { onProjectChange(project.copy(first = it)) },
            label = { Text("Project Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(value = project.second,
            onValueChange = { onProjectChange(project.copy(second = it)) },
            label = { Text("Project Description") },
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
    certification: Pair<TextFieldValue, TextFieldValue>,
    onCertificationChange: (Pair<TextFieldValue, TextFieldValue>) -> Unit,
    onRemoveCertification: () -> Unit
) {
    Column {
        OutlinedTextField(
            value = certification.first,
            onValueChange = { onCertificationChange(certification.copy(first = it)) },
            label = { Text("Certification Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = certification.second,
            onValueChange = { onCertificationChange(certification.copy(second = it)) },
            label = { Text("Certification Description") },
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
    experience: Pair<TextFieldValue, TextFieldValue>,
    onExperienceChange: (Pair<TextFieldValue, TextFieldValue>) -> Unit,
    onRemoveExperience: () -> Unit
) {
    Column {
        OutlinedTextField(
            value = experience.first,
            onValueChange = { onExperienceChange(experience.copy(first = it)) },
            label = { Text("Experience Title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = experience.second,
            onValueChange = { onExperienceChange(experience.copy(second = it)) },
            label = { Text("Experience Description") },
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
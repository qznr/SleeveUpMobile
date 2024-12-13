package com.mockingbird.sleeveup.screen

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mockingbird.sleeveup.R
import com.mockingbird.sleeveup.factory.RegisterViewModelFactory
import com.mockingbird.sleeveup.navigation.Screen
import com.mockingbird.sleeveup.ui.theme.*
import com.mockingbird.sleeveup.model.RegisterViewModel
import com.mockingbird.sleeveup.repository.FirebaseUserRepository
import com.mockingbird.sleeveup.service.AuthService
import com.mockingbird.sleeveup.service.FirestoreService
import kotlinx.coroutines.launch

private const val TAG = "RegisterScreen"

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun RegisterScreen(navController: NavController) {
    val authService = AuthService(FirebaseAuth.getInstance())
    val firestore = FirebaseFirestore.getInstance()
    val userRepository = FirebaseUserRepository(FirestoreService(firestore))
    val viewModelFactory = RegisterViewModelFactory(authService, userRepository, navController)
    val viewModel: RegisterViewModel = viewModel(factory = viewModelFactory)

    Log.d(TAG, "RegisterScreen composition started")

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isAgreed by remember { mutableStateOf(false) }


    // Observe the registration state from the ViewModel
    val registrationState by viewModel.registrationState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val greyColor = Color(0xFFBDBDBD)


    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleSignInResult(result) { firebaseUser ->
            if (firebaseUser != null) {
                navController.navigate(Screen.Profile.createRoute(firebaseUser.email ?: ""))
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar("Google Sign In Failed")
                }
            }
        }
    }

//    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//        .requestIdToken(context.getString(R.string.default_web_client_id)).requestEmail().build()
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id)).requestEmail().build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }, containerColor = AlmostBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.register_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MajorelieBlue
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.register_subtitle),
                style = MaterialTheme.typography.titleLarge,
                color = White,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = {
                    Text(
                        stringResource(id = R.string.name_hint),
                        color = greyColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    focusedBorderColor = MajorelieBlue,
                    unfocusedBorderColor = greyColor,
                    cursorColor = White,
                    focusedLabelColor = MajorelieBlue,
                    unfocusedLabelColor = greyColor
                ),
                textStyle = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = {
                    Text(
                        stringResource(id = R.string.email_hint),
                        color = greyColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    focusedBorderColor = MajorelieBlue,
                    unfocusedBorderColor = greyColor,
                    cursorColor = White,
                    focusedLabelColor = MajorelieBlue,
                    unfocusedLabelColor = greyColor
                ),
                textStyle = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = {
                    Text(
                        stringResource(id = R.string.password_hint),
                        color = greyColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    focusedBorderColor = MajorelieBlue,
                    unfocusedBorderColor = greyColor,
                    cursorColor = White,
                    focusedLabelColor = MajorelieBlue,
                    unfocusedLabelColor = greyColor
                ),
                textStyle = MaterialTheme.typography.bodyMedium,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isAgreed,
                    onCheckedChange = { isAgreed = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MajorelieBlue, uncheckedColor = White
                    )
                )
                Text(
                    text = stringResource(id = R.string.terms_and_conditions),
                    color = White,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && isAgreed) {
                        viewModel.register(name, email, password)
                    } else {
                        // Show input error Snackbar (using the same snackbarHostState)
                        scope.launch { // Launch in the correct scope
                            snackbarHostState.showSnackbar(
                                message = "Please fill in all fields and agree to the terms.",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MajorelieBlue)
            ) {
                Text(
                    stringResource(id = R.string.register),
                    color = White,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = {
                    val signInIntent = googleSignInClient.signInIntent
                    launcher.launch(signInIntent)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, MajorelieBlue),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = White,
                    containerColor = Color.Transparent,
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google Icon",
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(id = R.string.register_with_google),
                        color = White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.already_have_account),
                    color = White,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = { navController.navigate(Screen.Login.route) }) {
                    Text(
                        stringResource(R.string.login),
                        color = MajorelieBlue,
                        style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.Underline)
                    )
                }
            }

            when (registrationState) {
                is RegisterViewModel.RegistrationState.Success -> {
                }

                is RegisterViewModel.RegistrationState.Error -> {
                    val errorMessage =
                        (registrationState as RegisterViewModel.RegistrationState.Error).message
                    scope.launch {
                        snackbarHostState.showSnackbar(errorMessage)
                    }
                }

                else -> Unit
            }
        }
    }
}
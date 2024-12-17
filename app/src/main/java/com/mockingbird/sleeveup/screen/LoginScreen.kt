package com.mockingbird.sleeveup.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.mockingbird.sleeveup.R
import com.mockingbird.sleeveup.factory.LoginViewModelFactory
import com.mockingbird.sleeveup.navigation.Screen
import com.mockingbird.sleeveup.ui.theme.*
import com.mockingbird.sleeveup.model.LoginViewModel
import com.mockingbird.sleeveup.service.AuthService
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(navController: NavController) {
    val authService = AuthService(FirebaseAuth.getInstance())
    val viewModelFactory =
        LoginViewModelFactory(authService, navController) // Create ViewModel Factory instance IMPORTANT
    val viewModel: LoginViewModel = viewModel(factory = viewModelFactory)

    // **NEW: Check for existing user**
    val firebaseAuth = FirebaseAuth.getInstance()
    val currentUser = firebaseAuth.currentUser
    if (currentUser != null) {
        // User is already logged in. Navigate to Profile
        LaunchedEffect(Unit) {
            navController.navigate(Screen.Profile.createRoute(currentUser.email ?: "")) {
                popUpTo(Screen.Login.route) {
                    inclusive = true // Remove Login from back stack
                }
            }
        }
        return // Exit the composable to prevent rendering login screen
    }


    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    var showError by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf("") }
    val greyColor = Color(0xFFBDBDBD)
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()


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

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id)).requestEmail().build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    Scaffold(
        containerColor = AlmostBlack,
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                text = stringResource(id = R.string.welcome_back),
                style = MaterialTheme.typography.titleLarge,
                color = White,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(id = R.string.email_hint), color = greyColor, style = MaterialTheme.typography.bodyMedium) },
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
                label = { Text(stringResource(id = R.string.password_hint), color = greyColor, style = MaterialTheme.typography.bodyMedium) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible)
                        R.drawable.visibility
                    else R.drawable.visibility_off
                    val description = if (passwordVisible) "Hide password" else "Show password"

                    IconButton(onClick = {passwordVisible = !passwordVisible}){
                        Icon(painter = painterResource(id = image), contentDescription = description)
                    }
                },
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

            TextButton(
                onClick = { /*TODO*/ },
                modifier = Modifier.align(Alignment.Start)
            ) {
                Text(
                    text = stringResource(R.string.forgot_password),
                    color = MajorelieBlue,
                    style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.Underline)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar(message = "Email dan Password tidak boleh kosong", duration = SnackbarDuration.Short)
                        }
                    }
                    else {
                        viewModel.login(email, password)
                        showError = false
                        errorMessage = ""
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(containerColor = MajorelieBlue)
            ) {
                Text(stringResource(id = R.string.login), color = White, style = MaterialTheme.typography.labelLarge)
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
                        modifier = Modifier.size(24.dp), // Set the size here
                        tint = Color.Unspecified // Use Color.Unspecified to use the original icon color
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(id = R.string.login_with_google), color = White, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.dont_have_account), color = White, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
                    Text(
                        text = stringResource(R.string.register),
                        color = MajorelieBlue,
                        style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.Underline)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
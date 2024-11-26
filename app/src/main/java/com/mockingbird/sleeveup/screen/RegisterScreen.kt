package com.mockingbird.sleeveup.screen

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
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

@Composable
fun RegisterScreen(navController: NavController) {

    val authService = AuthService(FirebaseAuth.getInstance())
    val firestore = FirebaseFirestore.getInstance()
    val userRepository = FirebaseUserRepository(FirestoreService(firestore))
    val viewModelFactory = RegisterViewModelFactory(authService, userRepository)
    val viewModel: RegisterViewModel = viewModel(factory = viewModelFactory)

    Log.d(TAG, "RegisterScreen composition started")

    var name by remember {
        Log.d(TAG, "Initializing name state")
        mutableStateOf("")
    }
    var email by remember {
        Log.d(TAG, "Initializing email state")
        mutableStateOf("")
    }
    var password by remember {
        Log.d(TAG, "Initializing password state")
        mutableStateOf("")
    }
    var isAgreed by remember {
        Log.d(TAG, "Initializing agreement state")
        mutableStateOf(false)
    }

    // State to handle successful registration and navigation
    var registrationSuccessful by remember { mutableStateOf(false) }

    // Observe the registration state from the ViewModel
    val registrationState = viewModel.registrationState.collectAsState()

    Log.d(TAG, "Current registration state: ${registrationState.value}")

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }, containerColor = DarkPurple
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
                text = stringResource(id = R.string.register_title), // SleeveUp!
                style = MaterialTheme.typography.displaySmall, color = MajorelieBlue
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.register_subtitle), //Subtitle text
                style = MaterialTheme.typography.bodyMedium, color = White
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(id = R.string.name_hint), color = White) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    focusedBorderColor = MajorelieBlue,
                    unfocusedBorderColor = White,
                    cursorColor = White,
                    focusedLabelColor = White,
                    unfocusedLabelColor = White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(id = R.string.email_hint), color = White) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    focusedBorderColor = MajorelieBlue,
                    unfocusedBorderColor = White,
                    cursorColor = White,
                    focusedLabelColor = White,
                    unfocusedLabelColor = White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(id = R.string.password_hint), color = White) },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = White,
                    unfocusedTextColor = White,
                    focusedBorderColor = MajorelieBlue,
                    unfocusedBorderColor = White,
                    cursorColor = White,
                    focusedLabelColor = White,
                    unfocusedLabelColor = White
                )
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
                    text = stringResource(id = R.string.terms_and_conditions), color = White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && isAgreed) {
                        viewModel.register(name, email, password)
                        registrationSuccessful = true // Set the flag after successful registration
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
                Text(stringResource(id = R.string.register), color = White)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { /* TODO: Google Sign-In */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = White)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Icon(
//                        imageVector = TODO("Add Google Icon"), // Replace with actual Google Icon
//                        contentDescription = null,
//                        tint = AlmostBlack
//                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(id = R.string.register_with_google), color = AlmostBlack)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Text(stringResource(R.string.already_have_account), color = White)
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = { navController.navigate(Screen.Login.route) }) {
                    Text(stringResource(R.string.login), color = MajorelieBlue)
                }
            }
        }
    }

    if (registrationSuccessful) {
        LaunchedEffect(Unit) { // Use LaunchedEffect to perform side effects
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Register.route) { inclusive = true }
            }
        }
    }

}
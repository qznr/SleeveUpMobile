package com.mockingbird.sleeveup.screen

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.mockingbird.sleeveup.R
import com.mockingbird.sleeveup.factory.LoginViewModelFactory
import com.mockingbird.sleeveup.navigation.Screen
import com.mockingbird.sleeveup.ui.theme.*
import com.mockingbird.sleeveup.model.LoginViewModel
import com.mockingbird.sleeveup.service.AuthService


// Ini adalah helper function, tidak perlu disentuh kecuali kalau mau dipindah ke viewModel
private fun handleSignInResult(
    result: androidx.activity.result.ActivityResult, onComplete: (FirebaseUser?) -> Unit
) {
    if (result.resultCode == Activity.RESULT_OK) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            Log.d("LoginScreen", "firebaseAuthWithGoogle:" + account.id)
            firebaseAuthWithGoogle(account.idToken!!, onComplete)
        } catch (e: ApiException) {
            Log.w("LoginScreen", "Google sign in failed", e)
            onComplete(null) // Important: Call onComplete with null on failure
        }
    } else {
        onComplete(null) // Also handle cases where the result is not OK
    }
}

// Ini adalah helper function, tidak perlu disentuh kecuali kalau mau dipindah ke viewModel
private fun firebaseAuthWithGoogle(idToken: String, onComplete: (FirebaseUser?) -> Unit) {
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    Firebase.auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("LoginScreen", "signInWithCredential:success")
                onComplete(Firebase.auth.currentUser)
            } else {
                Log.w("LoginScreen", "signInWithCredential:failure", task.exception)
                onComplete(null)
            }
        }
}

@Composable
fun LoginScreen(navController: NavController) {
    val authService = AuthService(FirebaseAuth.getInstance()) // Create AuthService instance
    val viewModelFactory =
        LoginViewModelFactory(authService, navController) // Create ViewModel Factory instance
    val viewModel: LoginViewModel = viewModel(factory = viewModelFactory)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current
    var showError by rememberSaveable { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        handleSignInResult(result) { firebaseUser ->
            if (firebaseUser != null) {
                // Navigate to the profile screen or perform other actions
                navController.navigate(Screen.Profile.createRoute(firebaseUser.email ?: ""))
            } else {
                showError = true
            }
        }
    }

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id)).requestEmail().build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    Scaffold(
        containerColor = DarkPurple
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
                text = stringResource(id = R.string.welcome_back),
                style = MaterialTheme.typography.displaySmall,
                color = MajorelieBlue
            )
//            Spacer(modifier = Modifier.height(8.dp))
//            Text(
//                text = stringResource(id = R.string.login_subtitle),
//                style = MaterialTheme.typography.bodyMedium,
//                color = White
//            )
//            Spacer(modifier = Modifier.height(16.dp))

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

            Button(
                onClick = { viewModel.login(email, password) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MajorelieBlue)
            ) {
                Text(stringResource(id = R.string.login), color = White)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val signInIntent = googleSignInClient.signInIntent
                    launcher.launch(signInIntent)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = White)

            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Icon(
//                        // Replace with actual Google Icon
//                        imageVector = Icons.Default.Email,
//                        contentDescription = null,
//                        tint = AlmostBlack
//                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(id = R.string.login_with_google), color = AlmostBlack)
                }
            }

            if (showError) {
                Text("Login failed", color = Color.Red)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                Text(stringResource(R.string.dont_have_account), color = White)
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
                    Text(stringResource(R.string.register), color = MajorelieBlue)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { /*TODO*/ }) {
                Text(stringResource(R.string.forgot_password), color = White)
            }
        }
    }
}
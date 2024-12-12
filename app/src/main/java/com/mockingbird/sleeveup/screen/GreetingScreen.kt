package com.mockingbird.sleeveup.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.mockingbird.sleeveup.navigation.Screen
import com.mockingbird.sleeveup.R
import com.mockingbird.sleeveup.service.AuthService


@Composable
fun ProfileScreen(navController: NavController, email: String) {
    val authService = AuthService(FirebaseAuth.getInstance())
    val context = LocalContext.current

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id)).requestEmail().build()

    val googleSignInClient = GoogleSignIn.getClient(context, gso)


    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Hello, $email")

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                authService.signOut()
                authService.signOutGoogle(googleSignInClient).addOnCompleteListener{
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }


            },
            modifier = Modifier.fillMaxWidth(0.5f) // Make the button smaller
        ) {
            Text(stringResource(R.string.logout))
        }
    }
}

@Composable
fun GreetingScreen() {
    // The UI of the greeting screen
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Welcome to the App!")
        Spacer(modifier = Modifier.height(16.dp))
    }
}
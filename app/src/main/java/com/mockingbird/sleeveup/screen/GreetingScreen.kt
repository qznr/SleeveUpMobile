package com.mockingbird.sleeveup.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.mockingbird.sleeveup.navigation.Screen
import com.mockingbird.sleeveup.R

@Composable
fun ProfileScreen(navController: NavController, email: String) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Hello, $email")

        Spacer(modifier = Modifier.height(16.dp))

        Button (
            onClick = {
                auth.signOut()
                navController.navigate(Screen.Login.route) {
                    // Pop everything up to and including the Login screen
                    // to prevent the user from going back to the Profile screen
                    // after logging out.
                    popUpTo(Screen.Login.route) { inclusive = true }
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

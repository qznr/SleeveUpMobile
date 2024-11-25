package com.mockingbird.sleeveup.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

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

//@Preview(showBackground = true)
//@Composable
//fun GreetingScreenPreview() {
//    // Remember NavController in the preview, it won't perform any navigation, just for previewing UI.
//    val navController = rememberNavController()
//    GreetingScreen(navController = navController)
//}

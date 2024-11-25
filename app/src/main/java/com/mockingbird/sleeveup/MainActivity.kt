package com.mockingbird.sleeveup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mockingbird.sleeveup.navigation.Screen
import com.mockingbird.sleeveup.screen.LoginScreen
import com.mockingbird.sleeveup.screen.RegisterScreen // Import RegisterScreen
import com.mockingbird.sleeveup.ui.theme.SleeveUpTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SleeveUpTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        // Add more composable routes as needed for other screens
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SleeveUpTheme {
        AppNavigation()
    }
}
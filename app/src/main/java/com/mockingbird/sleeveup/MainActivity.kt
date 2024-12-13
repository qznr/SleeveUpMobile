package com.mockingbird.sleeveup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mockingbird.sleeveup.navigation.Screen
import com.mockingbird.sleeveup.screen.EditUserProfileScreen
import com.mockingbird.sleeveup.screen.LoginScreen
import com.mockingbird.sleeveup.screen.LandingScreen
import com.mockingbird.sleeveup.screen.ProfileScreen
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
        composable(
            route = Screen.Profile.route,
            arguments = listOf(navArgument("email"){type = NavType.StringType})
        ) { navBackStackEntry ->
            val email = navBackStackEntry.arguments?.getString("email") ?: "Guest"
            LandingScreen(navController, email)
        }
        composable(
            route = Screen.UserProfile.route,
            arguments = listOf(navArgument("userId"){type = NavType.StringType})
        ) { navBackStackEntry ->
                val userId = navBackStackEntry.arguments?.getString("userId") ?: "Guest"
            ProfileScreen(navController = navController, userId = userId)
        }
        composable(
            route = Screen.EditUserProfile.route,
            arguments = listOf(navArgument("userId"){type = NavType.StringType})
        ) { navBackStackEntry ->
            val userId = navBackStackEntry.arguments?.getString("userId") ?: "Guest"
            EditUserProfileScreen(navController = navController, userId = userId)
        }
    }
}
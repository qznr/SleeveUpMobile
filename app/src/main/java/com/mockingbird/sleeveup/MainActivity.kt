package com.mockingbird.sleeveup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mockingbird.sleeveup.navigation.NavigationItem
import com.mockingbird.sleeveup.navigation.Screen
import com.mockingbird.sleeveup.screen.CompanyScreen
import com.mockingbird.sleeveup.screen.JobDetailsScreen
import com.mockingbird.sleeveup.screen.EditUserProfileScreen
import com.mockingbird.sleeveup.screen.JobScreen
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
                MainScreenView()
            }
        }
    }
}

@Composable
fun MainScreenView() {
    val navController = rememberNavController()
    val bottomBarState = remember { mutableStateOf(false) } // Initially hidden

    Scaffold(
        bottomBar = {
            if (bottomBarState.value) { // Conditionally show BottomBar
                BottomAppBar(navController = navController)
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            AppNavigation(navController = navController, bottomBarState = bottomBarState)
        }
    }
}

@Composable
fun BottomAppBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.onBackground, // Or your desired color
    ) {
        val navigationItems = listOf(
            NavigationItem(
                title = "Loker",
                icon = painterResource(id = R.drawable.baseline_loker_24),
                screen = Screen.Jobs // Assuming this is your home screen
            ),
            NavigationItem(
                title = "Perusahaan",
                icon = painterResource(id = R.drawable.baseline_perusahaan_24),
                screen = Screen.Companies
            ),
            NavigationItem(
                title = "Event",
                icon = painterResource(id = R.drawable.baseline_event_24),
                screen = Screen.Profile
            ),
            NavigationItem(
                title = "Profil",
                icon = painterResource(id = R.drawable.baseline_profil_24),
                screen = Screen.UserProfile
            ),
            // Add more navigation items as needed
        )

        navigationItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = item.icon,
                        contentDescription = item.title,
                        tint = if (currentRoute == item.screen.route) {
                            MaterialTheme.colorScheme.primary // Selected color
                        } else {
                            MaterialTheme.colorScheme.onPrimary // Unselected color
                        }
                    )
                },
                label = {
                    Text(
                        item.title,
                        color = if (currentRoute == item.screen.route) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onPrimary
                        }
                    )
                },
                selected = currentRoute == item.screen.route,
                onClick = {
                    navController.navigate(item.screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController, bottomBarState: MutableState<Boolean>) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            bottomBarState.value = false  // Hide BottomBar on LoginScreen
            LoginScreen(navController = navController)
        }
        composable(Screen.Register.route) {
            bottomBarState.value = false
            RegisterScreen(navController = navController)
        }
        composable(
            route = Screen.Profile.route,
            arguments = listOf(navArgument("email"){type = NavType.StringType})
        ) { navBackStackEntry ->
            bottomBarState.value = true  // Show BottomBar
            val email = navBackStackEntry.arguments?.getString("email") ?: "Guest"
            LandingScreen(navController, email)
        }
        composable(
            route = Screen.UserProfile.route,
            arguments = listOf(navArgument("userId"){type = NavType.StringType})
        ) { navBackStackEntry ->
            bottomBarState.value = true  // Show BottomBar
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
        composable(
            route = Screen.Jobs.route,
        ) {
            bottomBarState.value = true
            JobScreen(navController = navController)
        }
        composable(
            route = Screen.Companies.route,
        ) {
            bottomBarState.value = true
            CompanyScreen(navController = navController)
        }
        composable(
            route = Screen.JobDetails.route,
            arguments = listOf(navArgument("jobId"){type = NavType.StringType})
        ) { navBackStackEntry ->
            bottomBarState.value = true  // Show BottomBar
            val jobId = navBackStackEntry.arguments?.getString("jobId") ?: "Guest"
            JobDetailsScreen(navController = navController, jobId = jobId)
        }

    }
}
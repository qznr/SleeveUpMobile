package com.mockingbird.sleeveup.navigation

sealed class Screen(val route: String) {
    object Greeting : Screen("greeting")
    object Login : Screen("login")
    object Register : Screen("register")
    object UserProfile : Screen("user_profile/{userId}") {
        fun createRoute(userId: String) = "user_profile/$userId"
    }
}
package com.mockingbird.sleeveup.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Profile : Screen("profile/{email}") {
        fun createRoute(email: String) = "profile/$email"
    }
    object UserProfile : Screen("user_profile/{userId}") {
        fun createRoute(userId: String) = "user_profile/$userId"
    }
}
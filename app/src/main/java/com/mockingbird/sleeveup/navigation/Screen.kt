package com.mockingbird.sleeveup.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Jobs : Screen("jobs")
    object Companies : Screen("companies")
    object Profile : Screen("profile/{email}") {
        fun createRoute(email: String) = "profile/$email"
    }
    object UserProfile : Screen("user_profile/{userId}") {
        fun createRoute(userId: String) = "user_profile/$userId"
    }
    object EditUserProfile : Screen("user_profile/{userId}/edit") {
        fun createRoute(userId: String) = "user_profile/$userId/edit"
    }
    object JobDetails : Screen("job_details/{jobId}"){
        fun createRoute(jobId: String) = "job_details/$jobId"
    }
    object CompanyDetails: Screen("company_details/{companyId}"){
        fun createRoute(companyId: String) = "company_details/$companyId"
    }
}
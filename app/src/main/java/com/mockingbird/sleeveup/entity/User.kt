package com.mockingbird.sleeveup.entity

data class User(
    val id: String,
    val name: String,
    val email: String,
    val password: String? = null,
    val photoUrl: String? = null,
    val providerId: String? = null, // "password" for email/password, "google.com" for Google
    val isEmailVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSignInAt: Long = System.currentTimeMillis()
)
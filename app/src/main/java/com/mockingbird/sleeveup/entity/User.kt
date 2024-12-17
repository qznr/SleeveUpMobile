package com.mockingbird.sleeveup.entity

import com.google.rpc.Status

data class User(
    val id: String? = "Guest",
    val name: String? = "Guest", // defaults as full name
    val email: String? = "Guest",
    val password: String? = null,
    val photoUrl: String? = null, // URL to the image in Firebase Storage. Check if null, then output the placeholder image
    val providerId: String? = null, // "password" for email/password, "google.com" for Google
    val isEmailVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSignInAt: Long = System.currentTimeMillis(),
    // For user profile sakes: Profile picture, full name, title, bio, projects, certificates, experiences
    val gender: String? = null,
    val status: String? = "Mencari Pekerjaan",
    val displayName: String? = null,
    val title: String? = null,
    val bio: String? = "Haloo saya menggunakan SleeveUp!",
    val lokasi: String? = null,
    val education: String? = null,
    val projects: List<Project>? = null,
    val certifications: List<Certificate>? = null,
    val experiences: List<Experience>? = null,
    val pendingJobApplication: Map<String, JobOffer>? = null
)
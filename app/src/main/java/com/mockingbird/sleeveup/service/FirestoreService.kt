package com.mockingbird.sleeveup.service

import com.google.firebase.firestore.FirebaseFirestore
import com.mockingbird.sleeveup.entity.User
import kotlinx.coroutines.tasks.await

class FirestoreService (private val firestore: FirebaseFirestore) {
    private val usersCollection = firestore.collection("users")

    suspend fun saveUser(user: User) {
        user.id?.let { usersCollection.document(it).set(user).await() }
    }

    suspend fun getUser(userId: String): User? {
        return usersCollection.document(userId).get().await().toObject(User::class.java)
    }

    suspend fun updateUser(user: User) {
        user.id?.let {
            usersCollection.document(it).update(
                mapOf(
                    "name" to user.name,
                    "displayName" to user.displayName,
                    "bio" to user.bio,
                    "gender" to user.gender,
                    "status" to user.status,
                    "education" to user.education,
                    "lokasi" to user.lokasi,
                    "projects" to user.projects,
                    "certifications" to user.certifications,
                    "experiences" to user.experiences,
                    "pendingJobApplication" to user.pendingJobApplication,
                    "photoUrl" to user.photoUrl
                )
            ).await()
        }
    }
}
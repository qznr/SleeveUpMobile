package com.mockingbird.sleeveup.service

import com.google.firebase.firestore.FirebaseFirestore
import com.mockingbird.sleeveup.entity.User
import kotlinx.coroutines.tasks.await

class FirestoreService (private val firestore: FirebaseFirestore) {
    private val usersCollection = firestore.collection("users")

    suspend fun saveUser(user: User) {
        usersCollection.document(user.id).set(user).await()
    }

    suspend fun getUser(userId: String): User? {
        return usersCollection.document(userId).get().await().toObject(User::class.java)
    }
}
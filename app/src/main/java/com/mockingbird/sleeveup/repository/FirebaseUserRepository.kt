package com.mockingbird.sleeveup.repository

import com.mockingbird.sleeveup.entity.User
import com.mockingbird.sleeveup.service.FirestoreService

class FirebaseUserRepository(private val firebaseService: FirestoreService) : UserRepository {
    override suspend fun getUser(userId: String): User {
        return firebaseService.getUser(userId)
            ?: throw NoSuchElementException("User not found with id: $userId")
    }

    override suspend fun saveUser(user: User) {
        firebaseService.saveUser(user)
    }
}
package com.mockingbird.sleeveup.repository
import com.mockingbird.sleeveup.entity.User

interface UserRepository {
    suspend fun getUser(userId: String): User
    suspend fun saveUser(user: User)
}
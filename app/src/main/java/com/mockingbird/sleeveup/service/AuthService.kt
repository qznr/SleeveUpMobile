package com.mockingbird.sleeveup.service

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mockingbird.sleeveup.entity.User
import kotlinx.coroutines.tasks.await


class AuthService(private val firebaseAuth: FirebaseAuth) {

    suspend fun register(user: User): FirebaseUser? {
        val authResult = firebaseAuth.createUserWithEmailAndPassword(
            user.email,
            user.password ?: throw IllegalArgumentException("Password cannot be null for registration")
        ).await()

        return authResult.user
    }

    fun login(email: String, password: String): Task<AuthResult> {
        return firebaseAuth.signInWithEmailAndPassword(email, password)
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}
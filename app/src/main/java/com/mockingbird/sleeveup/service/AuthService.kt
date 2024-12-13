package com.mockingbird.sleeveup.service

import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.mockingbird.sleeveup.entity.User
import kotlinx.coroutines.tasks.await
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class AuthService(private val firebaseAuth: FirebaseAuth) {

    suspend fun register(user: User): FirebaseUser? {
        val authResult = user.email?.let {
            firebaseAuth.createUserWithEmailAndPassword(
                it,
                user.password
                    ?: throw IllegalArgumentException("Password cannot be null for registration")
            ).await()
        }
        if (authResult != null) {
            return authResult.user
        }
        return null
    }

    fun login(email: String, password: String): Task<AuthResult> {
        return firebaseAuth.signInWithEmailAndPassword(email, password)
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    fun firebaseAuthWithGoogle(idToken: String): Task<AuthResult> {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return Firebase.auth.signInWithCredential(credential)
    }

    fun signOutGoogle(googleSignInClient: GoogleSignInClient): Task<Void> {
        return googleSignInClient.signOut()
    }
}
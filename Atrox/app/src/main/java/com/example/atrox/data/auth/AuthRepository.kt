package com.example.atrox.data.auth

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

//Login using Google via Firebase OAuth and email-password
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    suspend fun signInWithGoogleCredential(idToken: String): Result<AuthResult> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            Result.success(authResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<AuthResult> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(authResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createUserWithEmailAndPassword(email: String, password: String): Result<AuthResult> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Result.success(authResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

package com.example.atrox.data.auth

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// Create a new account using email - username - password and save it to firestore
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun saveUserToDatabase(uid: String, email: String, username: String): Result<Unit> {
        return try {
            val userMap = hashMapOf(
                "uid" to uid,
                "email" to email,
                "username" to username,
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("users").document(uid).set(userMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
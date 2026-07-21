package com.example.atrox.domain.repository

import com.google.firebase.auth.AuthResult

interface IAuthRepository {
    suspend fun signInWithGoogleCredential(idToken: String): Result<AuthResult>
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<AuthResult>
    suspend fun createUserWithEmailAndPassword(email: String, password: String): Result<AuthResult>
}

package com.example.atrox.domain.repository

interface IUserRepository {
    suspend fun saveUserToDatabase(uid: String, email: String, username: String): Result<Unit>
}

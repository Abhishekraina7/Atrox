package com.example.atrox.domain.repository

import kotlinx.coroutines.flow.Flow

interface IRegulatorRepository {
    val guardianPhone: Flow<String?>
    val guardianName: Flow<String?>
    val guardianConnectedSince: Flow<Long?>
    suspend fun saveGuardianPhone(phone: String)
    suspend fun saveGuardianName(name: String)
    suspend fun saveGuardianConnectedSince(timestamp: Long)
    suspend fun clearGuardian()
}

package com.studyhub.domain.repository

import com.studyhub.domain.model.User

interface AuthRepository {
    suspend fun login(email: String, password: String): User
    suspend fun register(email: String, password: String, name: String): User
    suspend fun logout()
    suspend fun getCurrentUser(): User?
}

package com.studyhub.domain.repository

import com.studyhub.domain.model.User

class FakeAuthRepository : AuthRepository {
    override suspend fun login(email: String, password: String): User {
        return User("1", email, "Test User")
    }

    override suspend fun register(email: String, password: String, name: String): User {
        return User("1", email, name)
    }

    override suspend fun logout() {}

    override suspend fun getCurrentUser(): User? = null
}

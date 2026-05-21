package com.studyhub.domain.repository

import com.studyhub.domain.model.User

class FakeAuthRepository : AuthRepository {
    
    var shouldReturnError = false
    var errorMessage = "Simulated error"
    
    private var currentUser: User? = null

    override suspend fun login(email: String, password: String): User {
        if (shouldReturnError) throw Exception(errorMessage)
        val user = User("1", email, "Test User")
        currentUser = user
        return user
    }

    override suspend fun register(email: String, password: String, name: String): User {
        if (shouldReturnError) throw Exception(errorMessage)
        val user = User("1", email, name)
        currentUser = user
        return user
    }

    override suspend fun logout() {
        currentUser = null
    }

    override suspend fun getCurrentUser(): User? = currentUser
}

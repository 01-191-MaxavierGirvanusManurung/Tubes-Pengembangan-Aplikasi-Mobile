package com.studyhub.data.repository

import com.studyhub.data.remote.FirebaseAuthSource
import com.studyhub.domain.model.User
import com.studyhub.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val firebaseAuthSource: FirebaseAuthSource
) : AuthRepository {
    override suspend fun login(email: String, password: String) =
        firebaseAuthSource.login(email, password)
    override suspend fun register(email: String, password: String, name: String) =
        firebaseAuthSource.register(email, password, name)
    override suspend fun logout() = firebaseAuthSource.logout()
    override suspend fun getCurrentUser() = firebaseAuthSource.getCurrentUser()
}

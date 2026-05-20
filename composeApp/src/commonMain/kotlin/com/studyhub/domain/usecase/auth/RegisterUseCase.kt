package com.studyhub.domain.usecase.auth

import com.studyhub.domain.model.User
import com.studyhub.domain.repository.AuthRepository

class RegisterUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String, name: String): User {
        require(name.isNotBlank()) { "Nama tidak boleh kosong" }
        require(email.isNotBlank()) { "Email tidak boleh kosong" }
        require(email.contains("@")) { "Format email tidak valid" }
        require(password.length >= 6) { "Password minimal 6 karakter" }
        return authRepository.register(email, password, name)
    }
}

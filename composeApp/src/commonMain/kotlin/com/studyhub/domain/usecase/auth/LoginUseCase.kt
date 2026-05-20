package com.studyhub.domain.usecase.auth

import com.studyhub.domain.model.User
import com.studyhub.domain.repository.AuthRepository

class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, password: String): User {
        require(email.isNotBlank()) { "Email tidak boleh kosong" }
        require(password.isNotBlank()) { "Password tidak boleh kosong" }
        require(email.contains("@")) { "Format email tidak valid" }
        require(password.length >= 6) { "Password minimal 6 karakter" }
        return authRepository.login(email, password)
    }
}

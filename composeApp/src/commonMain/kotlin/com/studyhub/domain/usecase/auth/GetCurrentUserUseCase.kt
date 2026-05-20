package com.studyhub.domain.usecase.auth

import com.studyhub.domain.model.User
import com.studyhub.domain.repository.AuthRepository

class GetCurrentUserUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): User? = authRepository.getCurrentUser()
}

package com.studyhub.domain.usecase.auth

import com.studyhub.domain.repository.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LoginUseCaseTest {

    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var loginUseCase: LoginUseCase

    @BeforeTest
    fun setup() {
        fakeAuthRepository = FakeAuthRepository()
        loginUseCase = LoginUseCase(fakeAuthRepository)
    }

    @Test
    fun `given valid credentials when login then returns user`() = runTest {
        val email = "test@example.com"
        val password = "password123"
        
        val result = loginUseCase(email, password)
        
        assertEquals(email, result.email)
    }

    @Test
    fun `given empty email when login then throws exception`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            loginUseCase("", "password123")
        }
    }

    @Test
    fun `given invalid email format when login then throws exception`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            loginUseCase("invalid-email", "password123")
        }
    }

    @Test
    fun `given short password when login then throws exception`() = runTest {
        assertFailsWith<IllegalArgumentException> {
            loginUseCase("test@example.com", "123")
        }
    }
}

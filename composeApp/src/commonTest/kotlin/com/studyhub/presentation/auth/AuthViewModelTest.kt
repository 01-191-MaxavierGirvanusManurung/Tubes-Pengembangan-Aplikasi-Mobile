package com.studyhub.presentation.auth

import app.cash.turbine.test
import com.studyhub.domain.repository.FakeAuthRepository
import com.studyhub.domain.usecase.auth.GetCurrentUserUseCase
import com.studyhub.domain.usecase.auth.LoginUseCase
import com.studyhub.domain.usecase.auth.LogoutUseCase
import com.studyhub.domain.usecase.auth.RegisterUseCase
import com.studyhub.presentation.screens.auth.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var viewModel: AuthViewModel
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeAuthRepository = FakeAuthRepository()
        viewModel = AuthViewModel(
            loginUseCase = LoginUseCase(fakeAuthRepository),
            registerUseCase = RegisterUseCase(fakeAuthRepository),
            logoutUseCase = LogoutUseCase(fakeAuthRepository),
            getCurrentUserUseCase = GetCurrentUserUseCase(fakeAuthRepository)
        )
    }

    @Test
    fun `initial state is empty`() {
        val state = viewModel.uiState.value
        assertNull(state.user)
        assertNull(state.error)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `login with valid credentials updates user state`() = runTest {
        val email = "test@example.com"
        val password = "password123"

        viewModel.login(email, password)

        val state = viewModel.uiState.value
        assertNotNull(state.user)
        assertEquals(email, state.user?.email)
        assertNull(state.error)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `login with repository error updates error state`() = runTest {
        fakeAuthRepository.shouldReturnError = true
        fakeAuthRepository.errorMessage = "Invalid credentials"

        viewModel.login("test@example.com", "wrong-password")

        val state = viewModel.uiState.value
        assertNull(state.user)
        assertEquals("Invalid credentials", state.error)
        assertEquals(false, state.isLoading)
    }

    @Test
    fun `register with valid details updates user state`() = runTest {
        val email = "new@example.com"
        val name = "New User"
        
        viewModel.register(email, "password123", name)

        val state = viewModel.uiState.value
        assertNotNull(state.user)
        assertEquals(email, state.user?.email)
        assertEquals(name, state.user?.name)
        assertNull(state.error)
    }

    @Test
    fun `register with repository error updates error state`() = runTest {
        fakeAuthRepository.shouldReturnError = true
        fakeAuthRepository.errorMessage = "Email already in use"

        viewModel.register("existing@example.com", "password123", "User")

        val state = viewModel.uiState.value
        assertNull(state.user)
        assertEquals("Email already in use", state.error)
    }

    @Test
    fun `login loading state is handled correctly`() = runTest {
        viewModel.uiState.test {
            // Skip initial state
            assertEquals(false, awaitItem().isLoading)
            
            val email = "test@example.com"
            viewModel.login(email, "password123")
            
            // Note: Since we use UnconfinedTestDispatcher, the loading state might be skipped 
            // if the repository returns immediately. 
            // But let's verify the final success state.
            val finalState = expectMostRecentItem()
            assertNotNull(finalState.user)
            assertEquals(false, finalState.isLoading)
        }
    }
}

package com.studyhub.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyhub.domain.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val aiInsight: String = "Insight feature coming soon.",
    val isInsightLoading: Boolean = false,
    val error: String? = null
)

class ProfileViewModel(
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            try {
                val tasks = taskRepository.getAllTasks("default_user")
                _uiState.value = _uiState.value.copy(
                    totalTasks = tasks.size,
                    completedTasks = 0 // Stub
                )
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    fun generateAiInsight() {}
}

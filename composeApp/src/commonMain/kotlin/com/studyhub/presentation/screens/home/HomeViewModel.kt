package com.studyhub.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyhub.domain.model.Task
import com.studyhub.domain.model.TaskStatus
import com.studyhub.domain.usecase.auth.GetCurrentUserUseCase
import com.studyhub.domain.usecase.task.GetActiveTasksUseCase
import com.studyhub.domain.usecase.task.GetTasksByDateUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val userName: String = "",
    val todayTasks: List<Task> = emptyList(),
    val upcomingTasks: List<Task> = emptyList(),
    val completedThisWeek: Int = 0,
    val overdueCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class HomeViewModel(
    private val getActiveTasksUseCase: GetActiveTasksUseCase,
    private val getTasksByDateUseCase: GetTasksByDateUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val user = getCurrentUserUseCase()
                val userId = user?.id ?: return@launch
                val today = currentMillis()
                val startOfDay = today - (today % 86_400_000L)
                val todayTasks = getTasksByDateUseCase(userId, startOfDay)
                val allActive = getActiveTasksUseCase(userId)
                val upcoming = allActive.filter { it.dueDate > startOfDay + 86_400_000L }
                    .take(5)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        userName = user.name,
                        todayTasks = todayTasks,
                        upcomingTasks = upcoming,
                        overdueCount = allActive.count {
                            t -> t.dueDate < today && t.status != TaskStatus.DONE
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun currentMillis(): Long {
        return kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
    }
}

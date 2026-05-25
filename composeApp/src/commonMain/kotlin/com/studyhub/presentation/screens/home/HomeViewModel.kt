package com.studyhub.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyhub.core.util.atEndOfDayMillis
import com.studyhub.domain.model.Task
import com.studyhub.domain.model.TaskStatus
import com.studyhub.domain.model.UserPreferences
import com.studyhub.domain.usecase.preferences.GetUserPreferencesUseCase
import com.studyhub.domain.usecase.task.DeleteTaskUseCase
import com.studyhub.domain.usecase.task.GetActiveTasksUseCase
import com.studyhub.domain.usecase.task.GetAllTasksUseCase
import com.studyhub.domain.usecase.task.GetTasksByDateUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class HomeUiState(
    val userName: String = "Pelajar",
    val todayTasksCount: Int = 0,
    val totalTasks: Int = 0,
    val doneTasks: Int = 0,
    val activeTasks: Int = 0,
    val dueTodayTasks: Int = 0,
    val completionPercentage: Int = 0,
    val upcomingTasks: List<Task> = emptyList(),
    val pomodoroWorkDuration: Int = 25,
    val isLoading: Boolean = false,
    val error: String? = null
)

class HomeViewModel(
    private val getActiveTasksUseCase: GetActiveTasksUseCase,
    private val getAllTasksUseCase: GetAllTasksUseCase,
    private val getTasksByDateUseCase: GetTasksByDateUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
        observePreferences()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            getUserPreferencesUseCase().collect { prefs ->
                _uiState.update { it.copy(
                    userName = prefs.userName,
                    pomodoroWorkDuration = prefs.pomodoroFocusDuration
                ) }
            }
        }
    }

    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val now = Clock.System.now()
                val localNow = now.toLocalDateTime(TimeZone.currentSystemDefault())
                val today = localNow.date
                
                val allTasks = getAllTasksUseCase()
                val allActive = getActiveTasksUseCase()
                val todayTasks = getTasksByDateUseCase(today)
                
                val startOfTomorrow = today.atEndOfDayMillis() + 1
                
                val doneCount = allTasks.count { it.status == TaskStatus.DONE && !it.isDeleted }
                val totalCount = allTasks.count { !it.isDeleted }
                val activeCount = allActive.size
                val dueTodayCount = todayTasks.count { it.status != TaskStatus.DONE }
                
                val completionPct = if (totalCount > 0) (doneCount * 100) / totalCount else 0
                
                val upcoming = allActive
                    .filter { it.dueDate >= startOfTomorrow }
                    .sortedBy { it.dueDate }
                    .take(4)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        todayTasksCount = dueTodayCount,
                        totalTasks = totalCount,
                        doneTasks = doneCount,
                        activeTasks = activeCount,
                        dueTodayTasks = dueTodayCount,
                        completionPercentage = completionPct,
                        upcomingTasks = upcoming
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteTaskUseCase(taskId)
            loadData()
        }
    }
}

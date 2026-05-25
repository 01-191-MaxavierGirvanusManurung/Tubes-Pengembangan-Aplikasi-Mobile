package com.studyhub.presentation.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyhub.core.util.toLocalDate
import com.studyhub.domain.model.Task
import com.studyhub.domain.model.TaskStatus
import com.studyhub.domain.usecase.task.DeleteTaskUseCase
import com.studyhub.domain.usecase.task.GetAllTasksUseCase
import com.studyhub.domain.usecase.task.GetTasksByDateUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class CalendarUiState(
    val selectedDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val tasksOnSelectedDate: List<Task> = emptyList(),
    val taskDates: Set<LocalDate> = emptySet(),
    val upcomingMonthTasks: List<Task> = emptyList(),
    val upcomingDeadlinesCount: Int = 0,
    val allTasks: List<Task> = emptyList(),
    val isLoading: Boolean = false
)

class CalendarViewModel(
    private val getTasksByDateUseCase: GetTasksByDateUseCase,
    private val getAllTasksUseCase: GetAllTasksUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val tasks = getAllTasksUseCase()
                val dates = tasks.map { it.dueDate.toLocalDate() }.toSet()
                
                // Upcoming deadlines: not DONE and dueDate >= today
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val upcomingCount = tasks.count { 
                    it.status != TaskStatus.DONE && it.dueDate.toLocalDate() >= today 
                }

                _uiState.update { 
                    it.copy(
                        allTasks = tasks,
                        taskDates = dates,
                        upcomingDeadlinesCount = upcomingCount,
                        isLoading = false
                    )
                }
                updateMonthOverview(today) // Initial month overview
                selectDate(_uiState.value.selectedDate)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateMonthOverview(currentMonth: LocalDate) {
        val tasks = _uiState.value.allTasks
        val monthTasks = tasks.filter {
            val date = it.dueDate.toLocalDate()
            date.month == currentMonth.month && 
            date.year == currentMonth.year && 
            it.status != TaskStatus.DONE
        }.sortedBy { it.dueDate }
        
        _uiState.update { it.copy(upcomingMonthTasks = monthTasks) }
    }

    fun selectDate(date: LocalDate) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(selectedDate = date) }
            try {
                val tasks = getTasksByDateUseCase(date)
                _uiState.update { it.copy(tasksOnSelectedDate = tasks) }
            } catch (e: Exception) {
                // Handle error
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

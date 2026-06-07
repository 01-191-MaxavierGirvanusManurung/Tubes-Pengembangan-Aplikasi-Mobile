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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

sealed interface CalendarUiState {
    object Loading : CalendarUiState
    data class Success(
        val selectedDate: LocalDate,
        val tasksOnSelectedDate: List<Task>,
        val taskDates: Set<LocalDate>,
        val upcomingMonthTasks: List<Task>,
        val upcomingDeadlinesCount: Int
    ) : CalendarUiState
    data class Error(val message: String) : CalendarUiState
}

class CalendarViewModel(
    private val getTasksByDateUseCase: GetTasksByDateUseCase,
    private val getAllTasksUseCase: GetAllTasksUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
    private val _currentMonth = MutableStateFlow(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CalendarUiState> = combine(
        getAllTasksUseCase(),
        _selectedDate.flatMapLatest { getTasksByDateUseCase(it) },
        _selectedDate,
        _currentMonth
    ) { allTasks, tasksOnDate, selectedDate, currentMonth ->
        try {
            val datesWithTasks = allTasks.map { it.dueDate.toLocalDate() }.toSet()
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

            val upcomingCount = allTasks.count {
                it.status != TaskStatus.DONE && it.dueDate.toLocalDate() >= today
            }

            val monthTasks = allTasks.filter {
                val date = it.dueDate.toLocalDate()
                date.month == currentMonth.month &&
                date.year == currentMonth.year &&
                it.status != TaskStatus.DONE
            }.sortedBy { it.dueDate }

            CalendarUiState.Success(
                selectedDate = selectedDate,
                tasksOnSelectedDate = tasksOnDate,
                taskDates = datesWithTasks,
                upcomingMonthTasks = monthTasks,
                upcomingDeadlinesCount = upcomingCount
            )
        } catch (e: Exception) {
            CalendarUiState.Error(e.message ?: "Terjadi kesalahan saat memuat kalender")
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalendarUiState.Loading
    )

    fun updateMonthOverview(month: LocalDate) {
        _currentMonth.value = month
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                deleteTaskUseCase(taskId)
            } catch (e: Exception) {
                // Silently fail or log for UI feedback if needed
            }
        }
    }
}

package com.studyhub.presentation.screens.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyhub.domain.model.TaskStatus
import com.studyhub.domain.repository.PomodoroRepository
import com.studyhub.domain.repository.TaskRepository
import com.studyhub.core.util.atStartOfDayMillis
import kotlinx.coroutines.flow.*
import kotlinx.datetime.*
import kotlinx.coroutines.launch

sealed interface ReportUiState {
    object Loading : ReportUiState
    data class Success(
        val totalTasksMonthly: Int,
        val completionRateMonthly: Float,
        val totalFocusMinutesMonthly: Int,
        val dailyUsage: List<Pair<String, Int>>,
        val focusTrend: List<Int>,
        val taskStatus: Triple<Int, Int, Int>, // On-time, Late, Unfinished
        val monthlyActivity: List<Triple<String, Int, Int>>, // Week, Added, Completed
        val monthlyOverdue: List<Int>
    ) : ReportUiState
    data class Error(val message: String) : ReportUiState
}

class ReportViewModel(
    private val taskRepository: TaskRepository,
    private val pomodoroRepository: PomodoroRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReportUiState>(ReportUiState.Loading)
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        loadReportData()
    }

    private fun loadReportData() {
        viewModelScope.launch {
            try {
                val now = Clock.System.now()
                val tz = TimeZone.currentSystemDefault()
                val today = now.toLocalDateTime(tz).date
                
                // Last 30 days
                val thirtyDaysAgo = now.toEpochMilliseconds() - (30L * 86_400_000L)
                val allTasks = taskRepository.getAllTasks().first()
                val monthlyTasks = allTasks.filter { it.createdAt >= thirtyDaysAgo || it.dueDate >= thirtyDaysAgo }
                
                // Last 7 days for usage & focus trend
                val sevenDaysAgo = today.minus(6, DateTimeUnit.DAY).atStartOfDayMillis()
                val recentSessions = pomodoroRepository.getSessionsInRange(sevenDaysAgo, now.toEpochMilliseconds())
                
                // 1. Daily Usage (App usage proxy = Focus sessions)
                val days = (0..6).map { today.minus(6 - it, DateTimeUnit.DAY) }
                val dailyUsage = days.map { date ->
                    val start = date.atStartOfDayMillis()
                    val end = start + 86_400_000L
                    val mins = recentSessions.filter { it.completedAt in start until end }.sumOf { it.durationMinutes }
                    val dayName = date.dayOfWeek.name.take(3)
                    dayName to mins
                }

                // 2. Focus Trend
                val focusTrend = days.map { date ->
                    val start = date.atStartOfDayMillis()
                    val end = start + 86_400_000L
                    recentSessions.filter { it.completedAt in start until end && it.phase.name == "FOCUS" }.sumOf { it.durationMinutes }
                }

                // 3. Task Status (Monthly)
                var onTime = 0
                var late = 0
                var unfinished = 0
                monthlyTasks.forEach { task ->
                    if (task.status == TaskStatus.DONE) {
                        // Perbaikan: Gunakan completedAt, jika null gunakan updatedAt sebagai fallback.
                        // Bandingkan dengan dueDate.
                        val completionTime = task.completedAt ?: task.updatedAt
                        if (completionTime <= task.dueDate) {
                            onTime++
                        } else {
                            late++
                        }
                    } else {
                        unfinished++
                    }
                }

                // 4. Monthly Activity (Added vs Completed vs Overdue per week)
                val weeks = (0..3).map { "W${it + 1}" }
                val monthlyActivity = (0..3).map { weekIndex ->
                    val start = thirtyDaysAgo + (weekIndex * 7L * 86_400_000L)
                    val end = start + (7L * 86_400_000L)
                    val added = monthlyTasks.count { it.createdAt in start until end }
                    val completed = monthlyTasks.count { it.completedAt != null && it.completedAt in start until end }
                    Triple(weeks[weekIndex], added, completed)
                }
                
                val monthlyOverdue = (0..3).map { weekIndex ->
                    val start = thirtyDaysAgo + (weekIndex * 7L * 86_400_000L)
                    val end = start + (7L * 86_400_000L)
                    monthlyTasks.count { it.status != TaskStatus.DONE && it.dueDate in start until end && it.dueDate < now.toEpochMilliseconds() }
                }

                val totalFocusMonthly = pomodoroRepository.getFocusMinutesInRange(thirtyDaysAgo, now.toEpochMilliseconds())
                val completionRate = if (monthlyTasks.isNotEmpty()) 
                    (monthlyTasks.count { it.status == TaskStatus.DONE }.toFloat() / monthlyTasks.size) * 100 
                    else 0f

                _uiState.value = ReportUiState.Success(
                    totalTasksMonthly = monthlyTasks.size,
                    completionRateMonthly = completionRate,
                    totalFocusMinutesMonthly = totalFocusMonthly,
                    dailyUsage = dailyUsage,
                    focusTrend = focusTrend,
                    taskStatus = Triple(onTime, late, unfinished),
                    monthlyActivity = monthlyActivity,
                    monthlyOverdue = monthlyOverdue
                )
            } catch (e: Exception) {
                _uiState.value = ReportUiState.Error(e.message ?: "Gagal memuat laporan")
            }
        }
    }
}

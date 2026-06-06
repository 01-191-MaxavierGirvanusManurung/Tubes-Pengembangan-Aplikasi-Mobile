package com.studyhub.domain.model

enum class PomodoroPhase(val displayName: String) {
    FOCUS("Fokus"),
    SHORT_BREAK("Istirahat Pendek"),
    LONG_BREAK("Istirahat Panjang")
}

data class PomodoroState(
    val phase: PomodoroPhase = PomodoroPhase.FOCUS,
    val timeRemainingSeconds: Int = 25 * 60,
    val totalSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val currentSession: Int = 1,
    val totalSessions: Int = 4,
    val linkedTaskId: String? = null,
    val linkedTaskTitle: String? = null,
    val completedSessionsToday: Int = 0
)

data class PomodoroSettings(
    val focusDurationMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val sessionsBeforeLongBreak: Int = 4,
    val autoStartBreak: Boolean = true,
    val autoStartFocus: Boolean = true,
    val notificationEnabled: Boolean = true
)

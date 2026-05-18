package com.studyhub.domain.model

data class UserPreferences(
    val userName: String = "Pelajar",
    val userAvatar: String = "default",
    val isDarkMode: Boolean = false,
    val isFocusModeEnabled: Boolean = false,
    val isAiReminderEnabled: Boolean = true,
    val pomodoroFocusDuration: Int = 25,
    val pomodoroShortBreak: Int = 5,
    val pomodoroLongBreak: Int = 15,
    val pomodoroSessionsBeforeLong: Int = 4,
    val notificationEnabled: Boolean = true,
    val defaultView: String = "list",
    val defaultSortBy: String = "dueDate"
)

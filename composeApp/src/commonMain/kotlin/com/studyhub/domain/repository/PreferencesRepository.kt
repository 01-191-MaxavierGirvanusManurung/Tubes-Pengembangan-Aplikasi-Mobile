package com.studyhub.domain.repository

import com.studyhub.domain.model.UserPreferences

interface PreferencesRepository {
    suspend fun getPreferences(): UserPreferences
    suspend fun updateUserName(name: String)
    suspend fun updateDarkMode(isDark: Boolean)
    suspend fun updateFocusMode(enabled: Boolean)
    suspend fun updateAiReminder(enabled: Boolean)
    suspend fun updatePomodoroSettings(
        focus: Int,
        shortBreak: Int,
        longBreak: Int,
        sessionsBeforeLong: Int
    )
    suspend fun updateDefaultView(view: String)
    suspend fun updateNotification(enabled: Boolean)
}

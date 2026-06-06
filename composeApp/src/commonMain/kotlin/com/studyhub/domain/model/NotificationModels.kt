package com.studyhub.domain.model

data class ReminderInfo(
    val taskId: String,
    val scheduledAt: Long,
    val aiReason: String,
    val isActive: Boolean,
    val createdAt: Long
)

data class NotifHistoryItem(
    val id: String,
    val taskId: String,
    val taskTitle: String,
    val taskSubject: String,
    val aiReason: String,
    val sentAt: Long,
    val isRead: Boolean
)

data class NotificationSettings(
    val isEnabled: Boolean = true,
    val isAiReminderEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true
)

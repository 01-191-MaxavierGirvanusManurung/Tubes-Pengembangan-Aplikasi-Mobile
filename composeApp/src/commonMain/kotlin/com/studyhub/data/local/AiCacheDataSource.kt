package com.studyhub.data.local

import com.studyhub.database.StudyHubDatabase
import com.studyhub.domain.model.AiUsageStats
import com.studyhub.database.AiCacheEntity
import kotlinx.datetime.*
import com.studyhub.core.util.currentTimeMillis

object AiCacheTTL {
    const val PRIORITY_HOURS = 6L
    const val REMINDER_HOURS = 24L

    fun priorityExpiry() =
        currentTimeMillis() + PRIORITY_HOURS * 3_600_000
    fun reminderExpiry() =
        currentTimeMillis() + REMINDER_HOURS * 3_600_000
}

object AiUsageLimit {
    const val MAX_PRIORITY_PER_DAY = 10
    const val MAX_REMINDER_PER_DAY = 20
    const val MIN_TASKS_FOR_AI = 2
}

class AiCacheDataSource(private val database: StudyHubDatabase) {

    fun getValidCache(cacheKey: String): AiCacheEntity? =
        database.aiCacheEntityQueries
            .selectValidCache(cacheKey, currentTimeMillis())
            .executeAsOneOrNull()

    fun insertCache(
        cacheKey: String,
        result: String,
        promptType: String,
        expiresAt: Long
    ) {
        database.aiCacheEntityQueries.insertCache(
            cacheKey = cacheKey,
            result = result,
            promptType = promptType,
            createdAt = currentTimeMillis(),
            expiresAt = expiresAt
        )
    }

    fun deleteExpiredCache() {
        database.aiCacheEntityQueries
            .deleteExpiredCache(currentTimeMillis())
    }

    fun getUsageStats(): AiUsageStats {
        val today = getTodayDateString()
        val usage = database.aiUsageEntityQueries
            .getUsage()
            .executeAsOneOrNull()

        return if (usage == null || usage.lastResetDate != today) {
            // Reset daily counter
            database.aiUsageEntityQueries.upsertUsage(
                priorityCallsToday = 0,
                reminderCallsToday = 0,
                lastResetDate = today
            )
            AiUsageStats(0, 0, today)
        } else {
            AiUsageStats(
                priorityCallsToday = usage.priorityCallsToday.toInt(),
                reminderCallsToday = usage.reminderCallsToday.toInt(),
                lastResetDate = usage.lastResetDate
            )
        }
    }

    fun incrementPriorityUsage() {
        ensureTodayUsage()
        database.aiUsageEntityQueries.incrementPriority()
    }

    fun incrementReminderUsage() {
        ensureTodayUsage()
        database.aiUsageEntityQueries.incrementReminder()
    }

    fun canCallPriority(): Boolean {
        val stats = getUsageStats()
        return stats.priorityCallsToday < AiUsageLimit.MAX_PRIORITY_PER_DAY
    }

    fun canCallReminder(): Boolean {
        val stats = getUsageStats()
        return stats.reminderCallsToday < AiUsageLimit.MAX_REMINDER_PER_DAY
    }

    private fun ensureTodayUsage() {
        val today = getTodayDateString()
        val usage = database.aiUsageEntityQueries
            .getUsage().executeAsOneOrNull()
        if (usage == null || usage.lastResetDate != today) {
            database.aiUsageEntityQueries.upsertUsage(0, 0, today)
        }
    }

    private fun getTodayDateString(): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return "${now.year}-${now.monthNumber.toString().padStart(2, '0')}-${now.dayOfMonth.toString().padStart(2, '0')}"
    }
}

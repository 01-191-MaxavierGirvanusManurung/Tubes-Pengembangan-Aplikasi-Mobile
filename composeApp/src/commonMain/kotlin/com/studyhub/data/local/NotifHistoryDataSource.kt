package com.studyhub.data.local

import com.studyhub.database.StudyHubDatabase
import com.studyhub.domain.model.NotifHistoryItem
import com.studyhub.core.util.currentTimeMillis
import com.studyhub.core.util.uuid

class NotifHistoryDataSource(
    private val database: StudyHubDatabase
) {
    fun getAll(): List<NotifHistoryItem> = try {
        database.notifHistoryEntityQueries.selectAll()
            .executeAsList()
            .map {
                NotifHistoryItem(
                    it.id, it.taskId, it.taskTitle,
                    it.taskSubject, it.aiReason,
                    it.sentAt, it.isRead == 1L
                )
            }
    } catch (e: Exception) { emptyList() }

    fun getUnreadCount(): Int = try {
        database.notifHistoryEntityQueries.selectUnreadCount()
            .executeAsOne().toInt()
    } catch (e: Exception) { 0 }

    fun insert(
        taskId: String, taskTitle: String,
        taskSubject: String, aiReason: String
    ) = try {
        database.notifHistoryEntityQueries.insert(
            uuid(),
            taskId, taskTitle, taskSubject,
            aiReason, currentTimeMillis()
        )
    } catch (e: Exception) { }

    fun markAllRead() = try {
        database.notifHistoryEntityQueries.markAllRead()
    } catch (e: Exception) { }

    fun markRead(id: String) = try {
        database.notifHistoryEntityQueries.markRead(id)
    } catch (e: Exception) { }

    fun deleteById(id: String) = try {
        database.notifHistoryEntityQueries.deleteById(id)
    } catch (e: Exception) { }

    fun deleteAll() = try {
        database.notifHistoryEntityQueries.deleteAll()
    } catch (e: Exception) { }
}

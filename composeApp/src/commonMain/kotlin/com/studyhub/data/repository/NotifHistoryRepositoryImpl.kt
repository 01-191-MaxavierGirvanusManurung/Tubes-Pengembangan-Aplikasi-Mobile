package com.studyhub.data.repository

import com.studyhub.data.local.NotifHistoryDataSource
import com.studyhub.domain.repository.NotifHistoryRepository

class NotifHistoryRepositoryImpl(
    private val dataSource: NotifHistoryDataSource
) : NotifHistoryRepository {
    override suspend fun getHistory() = dataSource.getAll()
    override suspend fun getUnreadCount() = dataSource.getUnreadCount()
    override suspend fun addToHistory(
        taskId: String, taskTitle: String,
        taskSubject: String, aiReason: String
    ) = dataSource.insert(taskId, taskTitle, taskSubject, aiReason)
    override suspend fun markAllRead() = dataSource.markAllRead()
    override suspend fun markRead(id: String) = dataSource.markRead(id)
    override suspend fun deleteItem(id: String) = dataSource.deleteById(id)
    override suspend fun clearAll() = dataSource.deleteAll()
}

package com.studyhub.data.repository

import com.studyhub.data.local.LocalTaskDataSource
import com.studyhub.domain.model.Task
import com.studyhub.domain.model.TaskStatus
import com.studyhub.domain.repository.TaskRepository

class TaskRepositoryImpl(
    private val localDataSource: LocalTaskDataSource
) : TaskRepository {
    override suspend fun addTask(task: Task) = localDataSource.insertTask(task)
    override suspend fun getTaskById(taskId: String) = localDataSource.selectById(taskId)
    override suspend fun getAllTasks(userId: String) = localDataSource.selectAllByUserId(userId)
    override suspend fun getActiveTasks(userId: String) = localDataSource.selectActiveByUserId(userId)
    override suspend fun getCompletedTasks(userId: String) = localDataSource.selectCompletedByUserId(userId)
    override suspend fun getTasksByDate(userId: String, date: Long) =
        localDataSource.selectByDate(userId, date, date + 86_400_000L)
    override suspend fun getTasksBySubject(userId: String, subject: String) =
        localDataSource.selectBySubject(userId, subject)
    override suspend fun getOverdueCount(userId: String, now: Long) =
        localDataSource.selectOverdueCount(userId, now).toInt()
    override suspend fun getCompletedCountInRange(userId: String, start: Long, end: Long) =
        localDataSource.selectCompletedCountInRange(userId, start, end).toInt()
    override suspend fun updateTask(task: Task) = localDataSource.updateTask(task)
    override suspend fun updateTaskStatus(taskId: String, status: TaskStatus, now: Long) =
        localDataSource.updateStatus(
            taskId, status.value, now,
            if (status == TaskStatus.DONE) now else null
        )
    override suspend fun softDeleteTask(taskId: String, now: Long) =
        localDataSource.softDelete(taskId, now)
    override suspend fun markAsCompleted(taskId: String, now: Long) =
        localDataSource.updateStatus(taskId, TaskStatus.DONE.value, now, now)
}

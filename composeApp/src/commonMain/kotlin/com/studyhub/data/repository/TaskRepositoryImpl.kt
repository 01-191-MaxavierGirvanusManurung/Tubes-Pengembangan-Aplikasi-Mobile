package com.studyhub.data.repository

import com.studyhub.data.local.StudyHubDatabase
import com.studyhub.domain.model.Task
import com.studyhub.domain.model.TaskStatus
import com.studyhub.domain.repository.TaskRepository

class TaskRepositoryImpl(
    private val db: StudyHubDatabase
) : TaskRepository {

    override suspend fun addTask(task: Task) {}

    override suspend fun getTaskById(taskId: String): Task? = null

    override suspend fun getAllTasks(userId: String): List<Task> = emptyList()

    override suspend fun getActiveTasks(userId: String): List<Task> = emptyList()

    override suspend fun getCompletedTasks(userId: String): List<Task> = emptyList()

    override suspend fun getTasksByDate(userId: String, date: Long): List<Task> = emptyList()

    override suspend fun getTasksBySubject(userId: String, subject: String): List<Task> = emptyList()

    override suspend fun getOverdueCount(userId: String, now: Long): Int = 0

    override suspend fun getCompletedCountInRange(userId: String, start: Long, end: Long): Int = 0

    override suspend fun updateTask(task: Task) {}

    override suspend fun updateTaskStatus(taskId: String, status: TaskStatus, now: Long) {}

    override suspend fun softDeleteTask(taskId: String, now: Long) {}

    override suspend fun markAsCompleted(taskId: String, now: Long) {}
}

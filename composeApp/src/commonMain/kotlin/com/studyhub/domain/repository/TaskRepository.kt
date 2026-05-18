package com.studyhub.domain.repository

import com.studyhub.domain.model.Task
import com.studyhub.domain.model.TaskStatus

interface TaskRepository {
    suspend fun addTask(task: Task)
    suspend fun getTaskById(taskId: String): Task?
    suspend fun getAllTasks(userId: String): List<Task>
    suspend fun getActiveTasks(userId: String): List<Task>
    suspend fun getCompletedTasks(userId: String): List<Task>
    suspend fun getTasksByDate(userId: String, date: Long): List<Task>
    suspend fun getTasksBySubject(userId: String, subject: String): List<Task>
    suspend fun getOverdueCount(userId: String, now: Long): Int
    suspend fun getCompletedCountInRange(userId: String, start: Long, end: Long): Int
    suspend fun updateTask(task: Task)
    suspend fun updateTaskStatus(taskId: String, status: TaskStatus, now: Long)
    suspend fun softDeleteTask(taskId: String, now: Long)
    suspend fun markAsCompleted(taskId: String, now: Long)
}

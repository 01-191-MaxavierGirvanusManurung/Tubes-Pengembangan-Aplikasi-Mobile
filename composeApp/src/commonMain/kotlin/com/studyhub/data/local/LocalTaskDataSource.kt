package com.studyhub.data.local

import com.studyhub.database.StudyHubDatabase
import com.studyhub.database.TaskEntity
import com.studyhub.domain.model.Task
import com.studyhub.domain.model.Priority
import com.studyhub.domain.model.TaskStatus

class LocalTaskDataSource(private val database: StudyHubDatabase) {

    fun insertTask(task: Task) {
        database.taskEntityQueries.insertTask(
            id = task.id,
            userId = task.userId,
            title = task.title,
            description = task.description,
            subject = task.subject,
            priority = task.priority.name.lowercase(),
            status = task.status.value,
            dueDate = task.dueDate,
            dueTime = task.dueTime,
            tags = task.tags.joinToString(",", "[", "]") { "\"$it\"" },
            estimatedMinutes = task.estimatedMinutes.toLong(),
            isDeleted = if (task.isDeleted) 1L else 0L,
            completedAt = task.completedAt,
            createdAt = task.createdAt,
            updatedAt = task.updatedAt
        )
    }

    fun selectAllByUserId(userId: String): List<Task> =
        database.taskEntityQueries.selectAllByUserId(userId)
            .executeAsList().map { it.toTask() }

    fun selectActiveByUserId(userId: String): List<Task> =
        database.taskEntityQueries.selectActiveByUserId(userId)
            .executeAsList().map { it.toTask() }

    fun selectCompletedByUserId(userId: String): List<Task> =
        database.taskEntityQueries.selectCompletedByUserId(userId)
            .executeAsList().map { it.toTask() }

    fun selectById(taskId: String): Task? =
        database.taskEntityQueries.selectById(taskId)
            .executeAsOneOrNull()?.toTask()

    fun selectByDate(userId: String, start: Long, end: Long): List<Task> =
        database.taskEntityQueries.selectByDate(userId, start, end)
            .executeAsList().map { it.toTask() }

    fun selectBySubject(userId: String, subject: String): List<Task> =
        database.taskEntityQueries.selectBySubject(userId, subject)
            .executeAsList().map { it.toTask() }

    fun selectOverdueCount(userId: String, now: Long): Long =
        database.taskEntityQueries.selectOverdueCount(userId, now)
            .executeAsOne()

    fun selectCompletedCountInRange(userId: String, start: Long, end: Long): Long =
        database.taskEntityQueries.selectCompletedCountInRange(userId, start, end)
            .executeAsOne()

    fun updateTask(task: Task) {
        database.taskEntityQueries.updateTask(
            title = task.title,
            description = task.description,
            subject = task.subject,
            priority = task.priority.name.lowercase(),
            status = task.status.value,
            dueDate = task.dueDate,
            dueTime = task.dueTime,
            tags = task.tags.joinToString(",", "[", "]") { "\"$it\"" },
            estimatedMinutes = task.estimatedMinutes.toLong(),
            updatedAt = task.updatedAt,
            id = task.id
        )
    }

    fun updateStatus(taskId: String, status: String, now: Long, completedAt: Long?) {
        database.taskEntityQueries.updateStatus(status, now, completedAt, taskId)
    }

    fun softDelete(taskId: String, now: Long) {
        database.taskEntityQueries.softDelete(now, taskId)
    }

    private fun TaskEntity.toTask(): Task = Task(
        id = id,
        userId = userId,
        title = title,
        description = description,
        subject = subject,
        priority = Priority.valueOf(priority.uppercase()),
        status = TaskStatus.fromString(status),
        dueDate = dueDate,
        dueTime = dueTime,
        tags = tags.removeSurrounding("[", "]")
            .split(",")
            .map { it.trim().removeSurrounding("\"") }
            .filter { it.isNotBlank() },
        estimatedMinutes = estimatedMinutes.toInt(),
        isDeleted = isDeleted == 1L,
        completedAt = completedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

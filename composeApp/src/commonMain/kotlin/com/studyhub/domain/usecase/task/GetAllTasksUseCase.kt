package com.studyhub.domain.usecase.task

import com.studyhub.domain.model.Task
import com.studyhub.domain.repository.TaskRepository

class GetAllTasksUseCase(private val taskRepository: TaskRepository) {
    suspend operator fun invoke(userId: String): List<Task> {
        require(userId.isNotBlank()) { "UserId tidak boleh kosong" }
        return taskRepository.getAllTasks(userId)
    }
}

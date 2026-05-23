package com.studyhub.domain.usecase.task

import com.studyhub.domain.model.Task
import com.studyhub.domain.repository.TaskRepository

class GetTasksByDateUseCase(private val taskRepository: TaskRepository) {
    suspend operator fun invoke(date: Long): List<Task> {
        return taskRepository.getTasksByDate(date)
    }
}

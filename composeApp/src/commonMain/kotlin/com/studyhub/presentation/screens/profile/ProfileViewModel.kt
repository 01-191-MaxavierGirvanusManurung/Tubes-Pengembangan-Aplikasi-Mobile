package com.studyhub.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyhub.domain.model.TaskStatus
import com.studyhub.domain.repository.SubjectRepository
import com.studyhub.domain.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

data class ProfileUiState(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val activeTasks: Int = 0,
    val overdueTasks: Int = 0,
    val totalSubjects: Int = 0,
    val userName: String = "Pengguna StudyHub",
    val isLoading: Boolean = false,
    val error: String? = null
)

class ProfileViewModel(
    private val taskRepository: TaskRepository,
    private val subjectRepository: SubjectRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val tasks = taskRepository.getAllTasks()
                val subjects = subjectRepository.getAllSubjects()
                val now = Clock.System.now().toEpochMilliseconds()
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        totalTasks = tasks.size,
                        completedTasks = tasks.count { t -> t.status == TaskStatus.DONE },
                        activeTasks = tasks.count { t -> t.status != TaskStatus.DONE },
                        overdueTasks = tasks.count { t -> t.dueDate < now && t.status != TaskStatus.DONE && !t.isDeleted },
                        totalSubjects = subjects.size
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    
    fun updateUserName(name: String) {
        _uiState.update { it.copy(userName = name) }
    }
}

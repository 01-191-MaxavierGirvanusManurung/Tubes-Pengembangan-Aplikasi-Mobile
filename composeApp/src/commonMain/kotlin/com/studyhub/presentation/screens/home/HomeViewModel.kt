package com.studyhub.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyhub.domain.model.Task
import com.studyhub.domain.repository.TaskRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val repository: TaskRepository
) : ViewModel() {
    
    val uiState: StateFlow<HomeUiState> = MutableStateFlow<List<Task>>(emptyList())
        .map { tasks ->
            if (tasks.isEmpty()) {
                HomeUiState.Empty
            } else {
                HomeUiState.Success(tasks)
            }
        }.catch { e ->
            emit(HomeUiState.Error(e.message ?: "Terjadi kesalahan"))
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState.Loading
        )
}

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val tasks: List<Task>) : HomeUiState
    data object Empty : HomeUiState
    data class Error(val message: String) : HomeUiState
}

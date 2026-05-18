package com.studyhub.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.studyhub.domain.model.Note
import com.studyhub.domain.model.NoteCategory
import com.studyhub.domain.repository.NoteRepository
import com.studyhub.domain.usecase.DeleteNoteUseCase
import com.studyhub.domain.usecase.GetAllNotesUseCase
import com.studyhub.domain.usecase.NoteSortBy
import com.studyhub.domain.usecase.SearchNotesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val getAllNotesUseCase: GetAllNotesUseCase,
    private val searchNotesUseCase: SearchNotesUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val repository: NoteRepository
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow<NoteCategory?>(null)
    private val _sortBy = MutableStateFlow(NoteSortBy.UPDATED_DESC)
    
    private val debouncedSearchQuery = _searchQuery.debounce(300)
    
    val sortBy: StateFlow<NoteSortBy> = _sortBy
    
    val uiState: StateFlow<HomeUiState> = combine(
        debouncedSearchQuery,
        _selectedCategory,
        _sortBy
    ) { query, category, sortBy ->
        Triple(query, category, sortBy)
    }.flatMapLatest { (query, category, sortBy) ->
        if (query.isBlank() && category == null) {
            getAllNotesUseCase(sortBy)
        } else {
            searchNotesUseCase(query, category)
        }
    }.map { notes ->
        if (notes.isEmpty()) {
            HomeUiState.Empty(_searchQuery.value, _selectedCategory.value)
        } else {
            HomeUiState.Success(notes, _searchQuery.value, _selectedCategory.value, _sortBy.value)
        }
    }.catch { e ->
        emit(HomeUiState.Error(e.message ?: "Terjadi kesalahan"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState.Loading
    )
    
    fun onSearchQueryChange(query: String) { _searchQuery.value = query }
    fun clearSearch() { _searchQuery.value = "" }
    fun onCategorySelected(category: NoteCategory?) { _selectedCategory.value = category }
    fun onSortByChanged(sortBy: NoteSortBy) { _sortBy.value = sortBy }
    fun togglePin(noteId: Long) { viewModelScope.launch { repository.togglePinNote(noteId) } }
    fun deleteNote(noteId: Long) { viewModelScope.launch { deleteNoteUseCase(noteId) } }
}

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val notes: List<Note>, val query: String = "", val category: NoteCategory? = null, val sortBy: NoteSortBy = NoteSortBy.UPDATED_DESC) : HomeUiState
    data class Empty(val query: String = "", val category: NoteCategory? = null) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

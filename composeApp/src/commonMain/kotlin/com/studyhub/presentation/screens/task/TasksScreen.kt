package com.studyhub.presentation.screens.task

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.studyhub.domain.model.TaskStatus
import com.studyhub.presentation.components.EmptyStateView
import com.studyhub.presentation.components.TaskCard
import com.studyhub.presentation.navigation.Screen
import com.studyhub.presentation.theme.Spacing
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(navController: NavController) {
    val viewModel: TasksViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchActive by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadTasks() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddTask.createRoute()) },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, "Tambah tugas")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::setSearchQuery,
                onSearch = {},
                active = searchActive,
                onActiveChange = { searchActive = it },
                placeholder = { Text("Cari tugas...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton({ viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, null)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.normal)
            ) {}

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                contentPadding = PaddingValues(horizontal = Spacing.normal),
                modifier = Modifier.padding(vertical = Spacing.small)
            ) {
                item {
                    FilterChip(
                        selected = uiState.filterStatus == null &&
                            uiState.filterPriority == null,
                        onClick = { viewModel.clearFilters() },
                        label = { Text("Semua") }
                    )
                }
                items(TaskStatus.entries) { status ->
                    FilterChip(
                        selected = uiState.filterStatus == status,
                        onClick = {
                            viewModel.setFilter(
                                if (uiState.filterStatus == status) null else status,
                                uiState.filterPriority, uiState.filterSubject
                            )
                        },
                        label = { Text(status.value.replace("_", " ").replaceFirstChar { it.uppercase() }) },
                        leadingIcon = {
                            if (uiState.filterStatus == status)
                                Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                        }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredTasks.isEmpty()) {
                EmptyStateView(
                    message = "Belum ada tugas",
                    actionLabel = "Tambah Tugas",
                    onAction = { navController.navigate(Screen.AddTask.createRoute()) },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(Spacing.normal),
                    verticalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    items(uiState.filteredTasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onStatusChange = { viewModel.updateStatus(task.id, it) },
                            onEdit = {
                                navController.navigate(Screen.EditTask.createRoute(task.id))
                            },
                            onDelete = { viewModel.showDeleteConfirm(task.id) }
                        )
                    }
                }
            }
        }
    }

    if (uiState.deleteConfirmTaskId != null) {
        AlertDialog(
            onDismissRequest = { viewModel.showDeleteConfirm(null) },
            title = { Text("Hapus Tugas") },
            text = { Text("Yakin ingin menghapus tugas ini?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTask(uiState.deleteConfirmTaskId!!)
                    viewModel.showDeleteConfirm(null)
                }) { Text("Hapus", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showDeleteConfirm(null) }) {
                    Text("Batal")
                }
            }
        )
    }
}

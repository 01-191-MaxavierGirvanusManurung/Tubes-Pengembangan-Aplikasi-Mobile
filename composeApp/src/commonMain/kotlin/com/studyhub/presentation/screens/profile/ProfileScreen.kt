package com.studyhub.presentation.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.studyhub.presentation.components.QuickStatCard
import com.studyhub.presentation.theme.Spacing
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val viewModel: ProfileViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showEditNameDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(uiState.userName) }

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Profil Saya") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.normal),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.large)
        ) {
            // Profile Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    Text(
                        text = uiState.userName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { 
                        newName = uiState.userName
                        showEditNameDialog = true 
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Nama", modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Stats Row
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.normal)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.normal)
                ) {
                    QuickStatCard(
                        title = "Selesai",
                        value = uiState.completedTasks.toString(),
                        subtitle = "tugas",
                        icon = Icons.Default.CheckCircle,
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                    QuickStatCard(
                        title = "Terlambat",
                        value = uiState.overdueTasks.toString(),
                        subtitle = "tugas",
                        icon = Icons.Default.Warning,
                        modifier = Modifier.weight(1f),
                        containerColor = if (uiState.overdueTasks > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.normal)
                ) {
                    QuickStatCard(
                        title = "Aktif",
                        value = uiState.activeTasks.toString(),
                        subtitle = "tugas",
                        icon = Icons.Default.Assignment,
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                    QuickStatCard(
                        title = "Mata Kuliah",
                        value = uiState.totalSubjects.toString(),
                        subtitle = "subjek",
                        icon = Icons.Default.Book,
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                }
            }

            // Settings Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Mode Gelap") },
                        supportingContent = { Text("Gunakan tema gelap") },
                        leadingContent = { Icon(Icons.Default.DarkMode, null) },
                        trailingContent = { Switch(checked = false, onCheckedChange = {}) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = Spacing.normal))
                    ListItem(
                        headlineContent = { Text("Pengingat AI") },
                        supportingContent = { Text("Notifikasi pintar dari AI") },
                        leadingContent = { Icon(Icons.Default.AutoAwesome, null) },
                        trailingContent = { Switch(checked = true, onCheckedChange = {}) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = Spacing.normal))
                    ListItem(
                        headlineContent = { Text("Notifikasi") },
                        supportingContent = { Text("Aktifkan semua notifikasi") },
                        leadingContent = { Icon(Icons.Default.Notifications, null) },
                        trailingContent = { Switch(checked = true, onCheckedChange = {}) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                }
            }

            Spacer(Modifier.height(Spacing.normal))

            // Logout Button
            Button(
                onClick = {
                    // Auth was removed, so this is a placeholder behavior
                    // In a real auth app: authViewModel.logout() then navigate to Login
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(Spacing.small)
            ) {
                Icon(Icons.Default.Logout, null)
                Spacer(Modifier.width(Spacing.small))
                Text("Keluar")
            }
        }
    }

    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Edit Nama") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Nama Lengkap") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateUserName(newName)
                    showEditNameDialog = false
                }) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) { Text("Batal") }
            }
        )
    }
}

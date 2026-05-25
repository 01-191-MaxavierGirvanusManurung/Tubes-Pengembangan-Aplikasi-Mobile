package com.studyhub.presentation.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.studyhub.core.util.SystemAppearance
import com.studyhub.presentation.components.TaskCard
import com.studyhub.presentation.navigation.Screen
import com.studyhub.presentation.screens.home.components.FocusTimerBottomSheet
import com.studyhub.presentation.screens.task.AddEditTaskBottomSheet
import com.studyhub.presentation.theme.Spacing
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val viewModel: HomeViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showFocusTimer by remember { mutableStateOf(false) }
    var showAddBottomSheet by remember { mutableStateOf(false) }
    var editingTaskId by remember { mutableStateOf<String?>(null) }

    // Force light icons (white) because header is dark
    SystemAppearance(isDarkMode = true)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background, // Consistent with other screens
    ) { _ ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = Spacing.normal + 80.dp), // Account for bottom bar
            verticalArrangement = Arrangement.spacedBy(Spacing.normal)
        ) {
            // Header Section (Dark themed to match Tasks/Calendar)
            item {
                HomeHeader(
                    userName = uiState.userName,
                    taskCount = uiState.todayTasksCount,
                    onAddTaskClick = {
                        editingTaskId = null
                        showAddBottomSheet = true
                    }
                )
            }

            item {
                Column(
                    modifier = Modifier.padding(horizontal = Spacing.normal),
                    verticalArrangement = Arrangement.spacedBy(Spacing.normal)
                ) {
                    // Overall Progress Card
                    ProgressCard(
                        percentage = uiState.completionPercentage,
                        done = uiState.doneTasks,
                        total = uiState.totalTasks
                    )

                    // Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        StatBox(Modifier.weight(1f), "Total", uiState.totalTasks.toString(), Icons.Default.Book, Color(0xFF7C5C29))
                        StatBox(Modifier.weight(1f), "Done", uiState.doneTasks.toString(), Icons.Default.CheckCircle, Color(0xFF10B981))
                        StatBox(Modifier.weight(1f), "Active", uiState.activeTasks.toString(), Icons.Default.Schedule, Color(0xFF3B82F6))
                        StatBox(Modifier.weight(1f), "Due Today", uiState.dueTodayTasks.toString(), Icons.Default.Bolt, Color(0xFFF59E0B))
                    }

                    // Focus Session Button
                    FocusSessionButton(
                        durationMins = uiState.pomodoroWorkDuration,
                        onClick = { showFocusTimer = true }
                    )

                    // Upcoming Tasks Header
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = Spacing.medium),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Upcoming Tasks",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { navController.navigate(Screen.Tasks.route) }) {
                            Text("See all", color = Color(0xFF9C7C50))
                        }
                    }
                }
            }

            // Task List
            if (uiState.upcomingTasks.isEmpty()) {
                item {
                    Text(
                        "No upcoming tasks today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.normal, vertical = 16.dp)
                    )
                }
            } else {
                items(uiState.upcomingTasks) { task ->
                    Box(modifier = Modifier.padding(horizontal = Spacing.normal)) {
                        TaskCard(
                            task = task,
                            onEdit = { 
                                editingTaskId = task.id
                                showAddBottomSheet = true 
                            },
                            onDelete = { viewModel.deleteTask(task.id) }
                        )
                    }
                }
            }
        }
    }

    if (showFocusTimer) {
        FocusTimerBottomSheet(
            initialWorkMinutes = uiState.pomodoroWorkDuration,
            onDismiss = { 
                showFocusTimer = false 
            }
        )
    }

    // Bottom Sheet for Add/Edit (Same as TasksScreen)
    if (showAddBottomSheet) {
        AddEditTaskBottomSheet(
            taskId = editingTaskId,
            initialDate = null,
            onDismiss = { showAddBottomSheet = false },
            onSuccess = {
                showAddBottomSheet = false
                viewModel.loadData()
            }
        )
    }
}

@Composable
fun HomeHeader(userName: String, taskCount: Int, onAddTaskClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                )
            )
            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .statusBarsPadding()
            .padding(top = 28.dp, start = 20.dp, end = 20.dp, bottom = 28.dp) // Adjusted padding
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Good morning,\n$userName! 👋",
                        style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 32.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "You have $taskCount tasks today",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledIconButton(
                        onClick = { /* Notifications */ },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Outlined.Notifications, "Notifications", modifier = Modifier.size(22.dp))
                    }
                    
                    FilledIconButton(
                        onClick = { 
                            onAddTaskClick()
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.Default.Add, "Add", modifier = Modifier.size(22.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressCard(percentage: Int, done: Int, total: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { percentage / 100f },
                    modifier = Modifier.size(64.dp),
                    color = Color(0xFFF59E0B),
                    trackColor = Color(0xFFFDE68A),
                    strokeWidth = 6.dp,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Text("$percentage%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Overall Progress", fontWeight = FontWeight.Bold)
                LinearProgressIndicator(
                    progress = { percentage / 100f },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                    color = Color(0xFFF59E0B),
                    trackColor = Color(0xFFFDE68A)
                )
                Text("$done of $total tasks completed", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }
}

@Composable
fun StatBox(modifier: Modifier, label: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(10.dp),
                color = color.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
fun FocusSessionButton(durationMins: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFF8570), Color(0xFFFFB070))
                )
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp).fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Timer, null, tint = Color.White)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Start Focus Session", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Pomodoro • $durationMins min work", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                }
                Icon(Icons.Default.ChevronRight, null, tint = Color.White)
            }
        }
    }
}

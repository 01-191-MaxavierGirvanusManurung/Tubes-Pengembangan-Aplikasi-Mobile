package com.studyhub.presentation.screens.calendar

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.studyhub.core.util.SystemAppearance
import com.studyhub.core.util.atStartOfDayMillis
import com.studyhub.core.util.capitalizeFirst
import com.studyhub.core.util.toLocalDate
import com.studyhub.domain.model.Priority
import com.studyhub.domain.model.Task
import com.studyhub.domain.model.TaskStatus
import com.studyhub.presentation.components.CalendarDayCell
import com.studyhub.presentation.components.EmptyStateView
import com.studyhub.presentation.navigation.Screen
import com.studyhub.presentation.screens.task.AddEditTaskBottomSheet
import com.studyhub.presentation.theme.*
import kotlinx.datetime.*
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(navController: NavController) {
    val viewModel: CalendarViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    var showAddBottomSheet by remember { mutableStateOf(false) }
    var editingTaskId by remember { mutableStateOf<String?>(null) }
    var deleteTaskConfirmId by remember { mutableStateOf<String?>(null) }

    var currentMonth by remember {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        mutableStateOf(LocalDate(today.year, today.month, 1))
    }

    // Force light icons (white) because header is dark
    SystemAppearance(isDarkMode = true)

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    LaunchedEffect(currentMonth) {
        viewModel.updateMonthOverview(currentMonth)
    }

    val daysInMonth = remember(currentMonth) {
        val firstDayOfNextMonth = if (currentMonth.monthNumber == 12)
            LocalDate(currentMonth.year + 1, 1, 1)
        else
            LocalDate(currentMonth.year, currentMonth.monthNumber + 1, 1)
        val lastDayOfMonth = firstDayOfNextMonth.minus(1, DateTimeUnit.DAY)
        lastDayOfMonth.dayOfMonth
    }

    val firstDayOfWeek = remember(currentMonth) {
        currentMonth.dayOfWeek.isoDayNumber % 7 // 0 for Sunday
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    editingTaskId = null
                    showAddBottomSheet = true 
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "Tambah tugas")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        ) {
            // Header Section
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
                    .padding(top = 28.dp, start = 20.dp, end = 20.dp, bottom = 28.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Calendar",
                                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${uiState.upcomingDeadlinesCount} upcoming deadlines",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "${currentMonth.month.name.lowercase().capitalizeFirst()} ${currentMonth.year}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = padding.calculateBottomPadding() + 80.dp)
            ) {
                // Calendar Grid Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Month Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                currentMonth = if (currentMonth.monthNumber == 1)
                                    LocalDate(currentMonth.year - 1, 12, 1)
                                else
                                    LocalDate(currentMonth.year, currentMonth.monthNumber - 1, 1)
                            }) {
                                Icon(Icons.Default.ChevronLeft, "Prev")
                            }
                            
                            Text(
                                text = "${currentMonth.month.name.lowercase().capitalizeFirst()} ${currentMonth.year}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            IconButton(onClick = {
                                currentMonth = if (currentMonth.monthNumber == 12)
                                    LocalDate(currentMonth.year + 1, 1, 1)
                                else
                                    LocalDate(currentMonth.year, currentMonth.monthNumber + 1, 1)
                            }) {
                                Icon(Icons.Default.ChevronRight, "Next")
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Week Header
                        Row(modifier = Modifier.fillMaxWidth()) {
                            listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach { day ->
                                Text(
                                    text = day,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Days Grid
                        val totalCells = firstDayOfWeek + daysInMonth
                        val rows = (totalCells + 6) / 7
                        
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(rows) { row ->
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    repeat(7) { col ->
                                        val index = row * 7 + col
                                        val day = index - firstDayOfWeek + 1
                                        
                                        Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                                            if (day in 1..daysInMonth) {
                                                val date = LocalDate(currentMonth.year, currentMonth.month, day)
                                                val isSelected = date == uiState.selectedDate
                                                val isToday = date == Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                                                val hasTask = uiState.taskDates.contains(date)

                                                CalendarDayCell(
                                                    dayOfMonth = day,
                                                    isSelected = isSelected,
                                                    isToday = isToday,
                                                    hasTask = hasTask,
                                                    onSelect = { viewModel.selectDate(date) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Selected Day Tasks Section
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                    val headerText = if (uiState.selectedDate == today) {
                        "Today's Tasks"
                    } else {
                        uiState.selectedDate.run {
                            "${dayOfWeek.name.lowercase().capitalizeFirst()}, ${month.name.lowercase().capitalizeFirst().take(3)} $dayOfMonth"
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = headerText,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "${uiState.tasksOnSelectedDate.size} tasks",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    AnimatedContent(
                        targetState = uiState.tasksOnSelectedDate,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(200)) + slideInVertically(animationSpec = tween(200)) { 8 })
                                .togetherWith(fadeOut(animationSpec = tween(200)) + slideOutVertically(animationSpec = tween(200)) { -8 })
                        }
                    ) { tasks ->
                        if (tasks.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🗓️", fontSize = 40.sp)
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "No tasks for this day",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "You're all clear!",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                tasks.forEach { task ->
                                    CalendarTaskCard(
                                        task = task,
                                        onEdit = { 
                                            editingTaskId = task.id
                                            showAddBottomSheet = true 
                                        },
                                        onDelete = { deleteTaskConfirmId = task.id }
                                    )
                                }
                            }
                        }
                    }
                }

                // Month Overview Section
                if (uiState.upcomingMonthTasks.isNotEmpty()) {
                    Spacer(Modifier.height(32.dp))
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Text(
                            "Month Overview",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                uiState.upcomingMonthTasks.take(5).forEachIndexed { index, task ->
                                    OverviewTaskCard(task)
                                    if (index < uiState.upcomingMonthTasks.take(5).size - 1) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(vertical = 16.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                                
                                if (uiState.upcomingMonthTasks.size > 5) {
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        "+${uiState.upcomingMonthTasks.size - 5} more this month",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showAddBottomSheet) {
        AddEditTaskBottomSheet(
            taskId = editingTaskId,
            initialDate = uiState.selectedDate.atStartOfDayMillis(),
            onDismiss = { showAddBottomSheet = false },
            onSuccess = {
                showAddBottomSheet = false
                viewModel.loadData()
            }
        )
    }

    if (deleteTaskConfirmId != null) {
        AlertDialog(
            onDismissRequest = { deleteTaskConfirmId = null },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to delete this task?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTask(deleteTaskConfirmId!!)
                    deleteTaskConfirmId = null
                }) { Text("Delete", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTaskConfirmId = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CalendarTaskCard(
    task: Task,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isDone = task.status == TaskStatus.DONE
    val subjectColor = getSubjectColor(task.subject)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Subject Icon
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(subjectColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    task.subject.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = subjectColor,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (isDone) TextDecoration.LineThrough else null,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Subject Badge
                    Surface(
                        color = subjectColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            task.subject,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = subjectColor,
                            fontSize = 11.sp
                        )
                    }
                    
                    // Time Tag
                    val deadlineText = formatDeadline(task.dueDate)
                    val deadlineColor = when (deadlineText) {
                        "Overdue" -> Color(0xFFEF4444)
                        "Today" -> Color(0xFFF59E0B)
                        "Tomorrow" -> Color(0xFFEAB308)
                        else -> MaterialTheme.colorScheme.outline
                    }
                    
                    Text(
                        deadlineText,
                        style = MaterialTheme.typography.labelSmall,
                        color = deadlineColor,
                        fontSize = 11.sp
                    )
                    
                    Text(
                        "~${task.estimatedMinutes}m",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 11.sp
                    )
                }
            }
            
            // Status Icon
            val statusIcon = when (task.status) {
                TaskStatus.DONE -> Icons.Default.CheckCircle
                TaskStatus.IN_PROGRESS -> Icons.Default.Schedule
                TaskStatus.TODO -> Icons.Default.Warning
            }
            val statusColor = when (task.status) {
                TaskStatus.DONE -> Color(0xFF22C55E)
                TaskStatus.IN_PROGRESS -> Color(0xFF3B82F6)
                TaskStatus.TODO -> MaterialTheme.colorScheme.outline
            }
            
            Icon(
                imageVector = statusIcon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(30.dp).clickable { onEdit() }
            )
        }
    }
}

@Composable
fun OverviewTaskCard(task: Task) {
    val subjectColor = getSubjectColor(task.subject)
    val date = task.dueDate.toLocalDate()
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date Box
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.verticalGradient(listOf(subjectColor, subjectColor.copy(alpha = 0.7f)))
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 18.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 18.sp
                )
                Text(
                    date.month.name.take(3).capitalizeFirst(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 10.sp
                )
            }
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                task.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                task.subject,
                style = MaterialTheme.typography.labelSmall,
                color = subjectColor
            )
        }
        
        // Priority Badge
        val pColor = when (task.priority) {
            Priority.HIGH -> Color(0xFFEF4444)
            Priority.MEDIUM -> Color(0xFFF59E0B)
            Priority.LOW -> Color(0xFF22C55E)
        }
        Surface(
            color = pColor.copy(alpha = 0.1f),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                task.priority.name.lowercase(),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = pColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun getSubjectColor(subject: String): Color {
    val hash = subject.hashCode()
    val colors = listOf(
        Color(0xFF7B6FA0), // Purple
        Color(0xFF6B8F71), // Green
        Color(0xFF8B7355), // Brown
        Color(0xFFC06C84), // Rose
        Color(0xFF355C7D), // Dark Blue
        Color(0xFFF67280), // Salmon
        Color(0xFF45B7D1)  // Light Blue
    )
    return colors[kotlin.math.abs(hash) % colors.size]
}

private fun formatDeadline(epochMillis: Long): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val target = epochMillis.toLocalDate()
    val days = target.toEpochDays() - now.toEpochDays()
    
    return when {
        days == 0 -> "Today"
        days == 1 -> "Tomorrow"
        days > 1 -> "${days}d left"
        else -> "Overdue"
    }
}

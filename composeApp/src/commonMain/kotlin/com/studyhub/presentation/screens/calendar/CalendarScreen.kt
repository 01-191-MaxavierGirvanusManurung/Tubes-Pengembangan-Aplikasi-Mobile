package com.studyhub.presentation.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studyhub.core.util.atStartOfDayMillis
import com.studyhub.core.util.capitalizeFirst
import com.studyhub.core.util.toLocalDate
import com.studyhub.domain.model.Priority
import com.studyhub.domain.model.Task
import com.studyhub.domain.model.TaskStatus
import com.studyhub.presentation.components.*
import com.studyhub.presentation.components.LoadingView
import com.studyhub.presentation.components.ErrorView
import com.studyhub.presentation.components.PillBadge
import com.studyhub.presentation.components.StudyHubHeader
import com.studyhub.presentation.screens.task.AddEditTaskBottomSheet
import com.studyhub.presentation.theme.*
import kotlinx.datetime.*
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateToTaskDetail: (String) -> Unit
) {
    val viewModel: CalendarViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is CalendarUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    var showAddBottomSheet by remember { mutableStateOf(false) }
    var editingTaskId by remember { mutableStateOf<String?>(null) }
    var deleteTaskConfirmId by remember { mutableStateOf<String?>(null) }

    var currentMonth by remember {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        mutableStateOf(LocalDate(today.year, today.month, 1))
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        ) {
            val state = uiState

            // Header Section
            StudyHubHeader(
                title = "Calendar",
                subtitle = {
                    if (state is CalendarUiState.Success) {
                        Text(
                            "${state.upcomingDeadlinesCount} upcoming deadlines", 
                            color = Color.White.copy(alpha = 0.8f), 
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                actions = {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = CircleShape,
                    ) {
                        Text(
                            text = "${currentMonth.month.name.lowercase().capitalizeFirst()} ${currentMonth.year}",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 110.dp, bottom = 100.dp)
            ) {
                when (state) {
                    is CalendarUiState.Loading -> LoadingView(Modifier.height(400.dp))
                    is CalendarUiState.Error -> ErrorView(
                        message = state.message,
                        onRetry = { viewModel.selectDate(currentMonth) }
                    )
                    is CalendarUiState.Success -> {
                        // Calendar Grid Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Spacing.normal),
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(Spacing.normal)) {
                                // Month Selector
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = {
                                            currentMonth = if (currentMonth.monthNumber == 1)
                                                LocalDate(currentMonth.year - 1, 12, 1)
                                            else
                                                LocalDate(currentMonth.year, currentMonth.monthNumber - 1, 1)
                                            viewModel.updateMonthOverview(currentMonth)
                                        }
                                    ) {
                                        Icon(Icons.Default.ChevronLeft, "Bulan Sebelumnya")
                                    }
                                    
                                    Text(
                                        text = "${currentMonth.month.name.lowercase().capitalizeFirst()} ${currentMonth.year}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    
                                    IconButton(
                                        onClick = {
                                            currentMonth = if (currentMonth.monthNumber == 12)
                                                LocalDate(currentMonth.year + 1, 1, 1)
                                            else
                                                LocalDate(currentMonth.year, currentMonth.monthNumber + 1, 1)
                                            viewModel.updateMonthOverview(currentMonth)
                                        }
                                    ) {
                                        Icon(Icons.Default.ChevronRight, "Bulan Selanjutnya")
                                    }
                                }

                                Spacer(Modifier.height(Spacing.normal))

                                // Week Header
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach { day ->
                                        Text(
                                            text = day,
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(Spacing.small))

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
                                                        val isSelected = date == state.selectedDate
                                                        val isToday = date == Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                                                        val hasTask = state.taskDates.contains(date)

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
                        Column(modifier = Modifier.padding(horizontal = Spacing.normal, vertical = Spacing.large)) {
                            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                            val headerText = if (state.selectedDate == today) "Today's Tasks" else {
                                state.selectedDate.run {
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
                                PillBadge(
                                    text = {
                                        Text(
                                            "${state.tasksOnSelectedDate.size} tasks",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }

                            Spacer(Modifier.height(Spacing.normal))

                            if (state.tasksOnSelectedDate.isEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.large,
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(Spacing.extraLarge).fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Default.CalendarToday, 
                                            contentDescription = null, 
                                            modifier = Modifier.size(32.dp),
                                            tint = MaterialTheme.colorScheme.outline
                                        )
                                        Spacer(Modifier.height(Spacing.small))
                                        Text(
                                            "No tasks for this day", 
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                                    state.tasksOnSelectedDate.forEach { task ->
                                        val taskId = task.id
                                        key(taskId) {
                                            CalendarTaskCard(
                                                task = task,
                                                onEdit = { 
                                                    editingTaskId = taskId
                                                    showAddBottomSheet = true 
                                                },
                                                onDelete = { deleteTaskConfirmId = taskId },
                                                onClick = { onNavigateToTaskDetail(taskId) }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Month Overview Section
                        if (state.upcomingMonthTasks.isNotEmpty()) {
                            Column(modifier = Modifier.padding(horizontal = Spacing.normal)) {
                                Text(
                                    "Month Overview", 
                                    style = MaterialTheme.typography.titleLarge, 
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(Spacing.normal))

                                Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
                                    state.upcomingMonthTasks.take(5).forEach { task ->
                                        key(task.id) {
                                            OverviewTaskCard(task)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showAddBottomSheet) {
        val selectedDate = (uiState as? CalendarUiState.Success)?.selectedDate ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        AddEditTaskBottomSheet(
            taskId = editingTaskId,
            initialDate = selectedDate.atStartOfDayMillis(),
            onDismiss = { 
                showAddBottomSheet = false
                editingTaskId = null
            },
            onSuccess = {
                showAddBottomSheet = false
                editingTaskId = null
            }
        )
    }

    if (deleteTaskConfirmId != null) {
        AlertDialog(
            onDismissRequest = { deleteTaskConfirmId = null },
            title = { Text("Hapus Tugas") },
            text = { Text("Apakah Anda yakin ingin menghapus tugas ini? Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTask(deleteTaskConfirmId!!)
                    deleteTaskConfirmId = null
                }) { Text("Hapus", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTaskConfirmId = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun CalendarTaskCard(
    task: Task,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val isDone = task.status == TaskStatus.DONE
    val subjectAccentColor = when (task.subject.lowercase()) {
        "mathematics", "calculus" -> MaterialTheme.colorScheme.primary
        "chemistry", "code" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.tertiary
    }
    
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.normal),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.normal)
        ) {
            // Subject Initial Box
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(subjectAccentColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    task.subject.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = subjectAccentColor,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        textDecoration = if (isDone) TextDecoration.LineThrough else null
                    ),
                    color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
                    Text(
                        task.subject,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isDone) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selesai",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun OverviewTaskCard(task: Task) {
    val subjectAccentColor = when (task.subject.lowercase()) {
        "mathematics", "calculus" -> MaterialTheme.colorScheme.primary
        "chemistry", "code" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.tertiary
    }
    val date = task.dueDate.toLocalDate()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(Spacing.small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.normal)
        ) {
            // Date Badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(subjectAccentColor),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 14.sp
                    )
                    Text(
                        date.month.name.take(3).capitalizeFirst(),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        lineHeight = 8.sp
                    )
                }
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    task.subject,
                    style = MaterialTheme.typography.labelSmall,
                    color = subjectAccentColor
                )
            }
            
            // Priority Badge
            if (task.priority == Priority.HIGH) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = CircleShape
                ) {
                    Text(
                        "high",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (task.priority == Priority.MEDIUM) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = CircleShape
                ) {
                    Text(
                        "medium",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

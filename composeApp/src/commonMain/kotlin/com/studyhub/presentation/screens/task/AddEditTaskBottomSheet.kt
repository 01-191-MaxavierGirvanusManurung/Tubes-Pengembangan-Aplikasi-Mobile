package com.studyhub.presentation.screens.task

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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.studyhub.domain.model.Priority
import com.studyhub.domain.model.TaskStatus
import com.studyhub.presentation.theme.Spacing
import kotlinx.datetime.*
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditTaskBottomSheet(
    taskId: String? = null,
    initialDate: Long? = null,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val viewModel: AddEditTaskViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("Mathematics") }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var status by remember { mutableStateOf(TaskStatus.TODO) }
    var dueDate by remember { mutableStateOf(initialDate ?: Clock.System.now().toEpochMilliseconds()) }
    var estimatedMinutes by remember { mutableStateOf(60) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var newSubjectName by remember { mutableStateOf("") }

    LaunchedEffect(taskId) {
        viewModel.loadSubjects()
        if (taskId != null) {
            viewModel.loadTask(taskId)
        }
    }

    LaunchedEffect(uiState.existingTask) {
        uiState.existingTask?.let { task ->
            title = task.title
            description = task.description
            selectedSubject = task.subject
            priority = task.priority
            status = task.status
            dueDate = task.dueDate
            estimatedMinutes = task.estimatedMinutes
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSuccess()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = Color(0xFFFAF9F6),
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (taskId == null) "New Task" else "Edit Task",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Add a new study task",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.clip(CircleShape).background(Color.White)
                ) {
                    Icon(Icons.Default.Close, null)
                }
            }

            Spacer(Modifier.height(24.dp))

            // Task Title
            Text("Task Title *", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("e.g. Complete Math Assignment...", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(Modifier.height(16.dp))

            // Subject
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Subject", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { showAddSubjectDialog = true }, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(8.dp))
            
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val allSubjects = listOf("Mathematics", "Physics", "English", "History", "Chemistry") + 
                                  uiState.subjects.map { it.name }.filter { it !in listOf("Mathematics", "Physics", "English", "History", "Chemistry") }
                
                allSubjects.forEach { sub ->
                    val isSelected = selectedSubject == sub
                    SuggestionChip(
                        onClick = { selectedSubject = sub },
                        label = { Text(sub, fontSize = 12.sp) },
                        shape = RoundedCornerShape(16.dp),
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                            labelColor = if (isSelected) Color.White else Color.Gray
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = if (isSelected) Color.Transparent else Color(0xFFE5E7EB)
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Due Date
                Column(modifier = Modifier.weight(1f)) {
                    Text("Due Date *", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = Instant.fromEpochMilliseconds(dueDate).toLocalDateTime(TimeZone.currentSystemDefault()).date.toString(),
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, null, Modifier.size(20.dp))
                            }
                        }
                    )
                }

                // Est Time
                Column(modifier = Modifier.weight(1f)) {
                    Text("Est. Time (min)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF3F4F6))
                    ) {
                        IconButton(onClick = { if (estimatedMinutes > 5) estimatedMinutes -= 5 }) {
                            Icon(Icons.Default.Remove, null, Modifier.size(16.dp))
                        }
                        Text(
                            text = estimatedMinutes.toString(),
                            modifier = Modifier.weight(1f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { estimatedMinutes += 5 }) {
                            Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Priority
            Text("Priority", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Priority.entries.forEach { p ->
                    val isSelected = priority == p
                    val pColor = when (p) {
                        Priority.LOW -> Color(0xFF10B981)
                        Priority.MEDIUM -> Color(0xFFF59E0B)
                        Priority.HIGH -> Color(0xFFEF4444)
                    }
                    
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { priority = p },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) pColor.copy(alpha = 0.1f) else Color.White,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, 
                            if (isSelected) pColor else Color(0xFFE5E7EB)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(8.dp).clip(CircleShape).background(pColor))
                            Spacer(Modifier.width(8.dp))
                            Text(p.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 12.sp, color = if (isSelected) pColor else Color.Gray)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Status
            Text("Status", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TaskStatus.entries.forEach { s ->
                    val isSelected = status == s
                    InputChip(
                        selected = isSelected,
                        onClick = { status = s },
                        label = { Text(s.value.replace("_", " ").replaceFirstChar { it.uppercase() }, fontSize = 11.sp) },
                        modifier = Modifier.weight(1f),
                        leadingIcon = {
                           val icon = when(s) {
                               TaskStatus.TODO -> Icons.Default.Description
                               TaskStatus.IN_PROGRESS -> Icons.Default.HourglassEmpty
                               TaskStatus.DONE -> Icons.Default.Check
                           }
                           Icon(icon, null, Modifier.size(14.dp))
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Description
            Text("Description", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Add details about this task...", color = Color.LightGray) },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(Modifier.height(24.dp))

            // Actions
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(0.4f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6), contentColor = Color.Gray),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        viewModel.saveTask(
                            taskId = taskId,
                            title = title,
                            description = description,
                            subject = selectedSubject,
                            priority = priority,
                            dueDate = dueDate,
                            estimatedMinutes = estimatedMinutes
                        )
                    },
                    modifier = Modifier.weight(0.6f).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B7355)),
                    shape = RoundedCornerShape(16.dp),
                    enabled = title.isNotBlank() && !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                    } else {
                        Icon(Icons.Default.Save, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (taskId == null) "Add Task" else "Save Changes")
                    }
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dueDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { dueDate = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // Add Subject Dialog
    if (showAddSubjectDialog) {
        AlertDialog(
            onDismissRequest = { showAddSubjectDialog = false },
            title = { Text("Add Subject") },
            text = {
                OutlinedTextField(
                    value = newSubjectName,
                    onValueChange = { newSubjectName = it },
                    label = { Text("Subject Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newSubjectName.isNotBlank()) {
                        viewModel.addSubject(newSubjectName)
                        selectedSubject = newSubjectName
                        newSubjectName = ""
                        showAddSubjectDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubjectDialog = false }) { Text("Cancel") }
            }
        )
    }
}

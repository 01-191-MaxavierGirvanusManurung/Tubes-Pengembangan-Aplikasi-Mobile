package com.studyhub.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.studyhub.domain.model.Priority
import com.studyhub.domain.model.Task
import com.studyhub.domain.model.TaskStatus
import com.studyhub.presentation.theme.PriorityHigh
import com.studyhub.presentation.theme.PriorityMedium
import com.studyhub.presentation.theme.PriorityLow
import com.studyhub.presentation.theme.Spacing
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: Task,
    onStatusChange: (TaskStatus) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val priorityColor = when (task.priority) {
        Priority.HIGH -> PriorityHigh
        Priority.MEDIUM -> PriorityMedium
        Priority.LOW -> PriorityLow
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete(); true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(end = Spacing.normal),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(Icons.Default.Delete, null,
                    tint = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    ) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Row(
                Modifier.padding(Spacing.normal),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedContent(
                    targetState = task.status == TaskStatus.DONE,
                    transitionSpec = {
                        scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut()
                    }
                ) { isDone ->
                    Checkbox(
                        checked = isDone,
                        onCheckedChange = { checked ->
                            onStatusChange(
                                if (checked) TaskStatus.DONE else TaskStatus.TODO
                            )
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                Box(
                    Modifier
                        .width(3.dp)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(priorityColor)
                )
                Spacer(Modifier.width(Spacing.small))
                Column(Modifier.weight(1f)) {
                    Text(task.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                    Text(task.subject,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "Deadline: ${formatDeadline(task.dueDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (task.dueDate < currentMillis() &&
                            task.status != TaskStatus.DONE)
                            MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

fun formatDeadline(epochMillis: Long): String {
    val dateTime = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault())
    val day = dateTime.dayOfMonth.toString().padStart(2, '0')
    val month = dateTime.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    val year = dateTime.year
    return "$day $month $year"
}

fun currentMillis(): Long {
    return kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
}

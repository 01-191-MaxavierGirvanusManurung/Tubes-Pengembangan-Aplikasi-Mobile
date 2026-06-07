package com.studyhub.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyhub.domain.model.Priority
import com.studyhub.domain.model.Task
import com.studyhub.domain.model.TaskStatus
import com.studyhub.core.util.formatTimeOnly
import com.studyhub.presentation.theme.*
import kotlinx.datetime.*

@Composable
fun TaskCard(
    task: Task,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDone = task.status == TaskStatus.DONE
    val now = com.studyhub.core.util.currentTimeMillis()
    val isOverdue = !isDone && task.dueDate < now
    
    val statusIcon = when {
        isDone -> Icons.Default.CheckCircle
        isOverdue -> Icons.Default.ErrorOutline
        task.status == TaskStatus.IN_PROGRESS -> Icons.Default.Schedule
        else -> Icons.Default.Schedule 
    }

    val statusColor = when {
        isDone -> MaterialTheme.colorScheme.primary // Semantic: Success-ish
        isOverdue -> MaterialTheme.colorScheme.error
        task.status == TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline
    }
    
    val subjectAccentColor = when {
        isOverdue -> MaterialTheme.colorScheme.error
        task.subject.lowercase() == "mathematics" || task.subject.lowercase() == "calculus" -> MaterialTheme.colorScheme.primary
        task.subject.lowercase() == "chemistry" || task.subject.lowercase() == "code" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.tertiary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                             else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isOverdue) 1.5.dp else 1.dp,
            color = if (isOverdue) MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Accent Strip
            Box(
                modifier = Modifier
                    .padding(vertical = Spacing.normal)
                    .width(4.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(subjectAccentColor)
            )
            
            Row(
                modifier = Modifier
                    .padding(Spacing.normal)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = "Status: ${task.status.name}",
                    tint = statusColor,
                    modifier = Modifier.size(24.dp)
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (isDone) TextDecoration.LineThrough else null
                        ),
                        color = when {
                            isDone -> MaterialTheme.colorScheme.onSurfaceVariant
                            isOverdue -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val subjectBg = if (isOverdue) MaterialTheme.colorScheme.errorContainer
                                        else MaterialTheme.colorScheme.surfaceVariant
                        val subjectText = if (isOverdue) MaterialTheme.colorScheme.onErrorContainer
                                          else MaterialTheme.colorScheme.onSurfaceVariant

                        TaskTag(task.subject, subjectBg, subjectText)
                        
                        val (priorityBg, priorityText) = when {
                            task.priority == Priority.HIGH -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
                            task.priority == Priority.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
                            else -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
                        }
                        TaskTag(task.priority.name.lowercase(), priorityBg, priorityText)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    val deadline = formatDeadline(task.dueDate)
                    Text(
                        deadline,
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            deadline == "Overdue" -> MaterialTheme.colorScheme.error
                            deadline == "Tomorrow" -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        Instant.fromEpochMilliseconds(task.dueDate).formatTimeOnly(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Tugas",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Hapus Tugas",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskTag(text: String, containerColor: Color, textColor: Color) {
    Surface(
        color = containerColor,
        shape = CircleShape
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = Spacing.small, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TaskGridCard(
    task: Task,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDone = task.status == TaskStatus.DONE
    val now = com.studyhub.core.util.currentTimeMillis()
    val isOverdue = !isDone && task.dueDate < now

    val statusIcon = when {
        isDone -> Icons.Default.CheckCircle
        isOverdue -> Icons.Default.ErrorOutline
        task.status == TaskStatus.IN_PROGRESS -> Icons.Default.Schedule
        else -> Icons.Default.Schedule
    }
    val statusColor = when {
        isDone -> MaterialTheme.colorScheme.primary
        isOverdue -> MaterialTheme.colorScheme.error
        task.status == TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline
    }
    
    val subjectAccentColor = when {
        isOverdue -> MaterialTheme.colorScheme.error
        task.subject.lowercase() == "mathematics" || task.subject.lowercase() == "calculus" -> MaterialTheme.colorScheme.primary
        task.subject.lowercase() == "chemistry" || task.subject.lowercase() == "code" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.tertiary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                             else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isOverdue) 1.5.dp else 1.dp,
            color = if (isOverdue) MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(subjectAccentColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        task.subject.take(1).uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        color = subjectAccentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Icon(
                    imageVector = statusIcon,
                    contentDescription = "Status: ${task.status.name}",
                    tint = statusColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (isDone) TextDecoration.LineThrough else null
                ),
                color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.defaultMinSize(minHeight = 36.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val deadline = formatDeadline(task.dueDate)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)) {
                    Text(
                        deadline,
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            deadline == "Overdue" -> MaterialTheme.colorScheme.error
                            deadline == "Tomorrow" -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)) {
                    IconButton(onClick = onEdit, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

fun formatDeadline(epochMillis: Long): String {
    val now = com.studyhub.core.util.currentTimeMillis()
    val diff = epochMillis - now
    val days = (diff / 86400000L).toInt()
    
    return when {
        days == 0 -> "Today"
        days == 1 -> "Tomorrow"
        days > 1 -> "${days}d left"
        else -> "Overdue"
    }
}

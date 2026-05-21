package com.studyhub.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.studyhub.presentation.theme.Spacing

@Composable
fun QuickStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(Spacing.normal),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(Spacing.extraSmall))
            Text(value, style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(title, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(subtitle, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

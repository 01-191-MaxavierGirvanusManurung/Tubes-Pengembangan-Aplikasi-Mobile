package com.studyhub.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyhub.presentation.theme.Spacing

@Composable
fun StudyHubBottomBar(
    currentRoute: String?,
    onItemSelected: (Screen) -> Unit
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().height(80.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            val items = listOf(
                Triple(Screen.Home, Icons.Outlined.Home, Icons.Filled.Home) to "Beranda",
                Triple(Screen.Tasks, Icons.Outlined.Assignment, Icons.Filled.Assignment) to "Tugas",
                Triple(Screen.Calendar, Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth) to "Kalender",
                Triple(Screen.Profile, Icons.Outlined.Person, Icons.Filled.Person) to "Profil"
            )
            
            items.forEach { (triple, label) ->
                val (screen, outlineIcon, filledIcon) = triple
                val selected = currentRoute == screen.route
                
                NavigationBarItem(
                    selected = selected,
                    onClick = { onItemSelected(screen) },
                    icon = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                if (selected) filledIcon else outlineIcon,
                                contentDescription = label,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    label = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                label,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 11.sp
                                )
                            )
                            if (selected) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(activeColor)
                                )
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = activeColor,
                        unselectedIconColor = inactiveColor,
                        selectedTextColor = activeColor,
                        unselectedTextColor = inactiveColor,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    )
                )
            }
        }
    }
}

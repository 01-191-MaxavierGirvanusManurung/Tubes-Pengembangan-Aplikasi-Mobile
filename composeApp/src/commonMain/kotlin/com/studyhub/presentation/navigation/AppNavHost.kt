package com.studyhub.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.studyhub.presentation.screens.addnote.AddNoteScreen
import com.studyhub.presentation.screens.detail.NoteDetailScreen
import com.studyhub.presentation.screens.home.HomeScreen
import com.studyhub.presentation.screens.profile.ProfileScreen

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val mainScreens = listOf(
        BottomNavItem("Home", Screen.Home, Icons.Default.Home),
        BottomNavItem("Tasks", Screen.Tasks, Icons.Default.Task),
        BottomNavItem("Calendar", Screen.Calendar, Icons.Default.CalendarMonth),
        BottomNavItem("Profile", Screen.Profile, Icons.Default.Person)
    )

    val showBottomBar = mainScreens.any { item ->
        currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(tonalElevation = 0.dp) {
                    mainScreens.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.hasRoute(item.route::class) } == true
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(Screen.Home) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Screen.Home> {
                HomeScreen(
                    onNavigateToAddNote = { navController.navigate(Screen.AddTask()) },
                    onNavigateToDetail = { noteId -> navController.navigate(Screen.TaskDetail(noteId)) }
                )
            }
            
            composable<Screen.Tasks> {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) { 
                    Text("Tasks Screen Placeholder") 
                }
            }

            composable<Screen.Calendar> {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) { 
                    Text("Calendar Screen Placeholder") 
                }
            }

            composable<Screen.Profile> { ProfileScreen() }
            
            composable<Screen.AddTask> { backStackEntry ->
                val route: Screen.AddTask = backStackEntry.toRoute()
                AddNoteScreen(
                    noteId = if (route.taskId == -1L) null else route.taskId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable<Screen.TaskDetail> { backStackEntry ->
                val route: Screen.TaskDetail = backStackEntry.toRoute()
                NoteDetailScreen(
                    noteId = route.taskId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { noteId -> navController.navigate(Screen.AddTask(noteId)) },
                    onShare = { _ -> }
                )
            }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val route: Any,
    val icon: ImageVector
)

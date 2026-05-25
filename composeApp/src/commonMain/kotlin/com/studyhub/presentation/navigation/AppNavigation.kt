package com.studyhub.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.studyhub.presentation.screens.calendar.CalendarScreen
import com.studyhub.presentation.screens.home.HomeScreen
import com.studyhub.presentation.screens.profile.ProfileScreen
import com.studyhub.presentation.screens.task.AddEditTaskScreen
import com.studyhub.presentation.screens.task.TaskDetailScreen
import com.studyhub.presentation.screens.task.TasksScreen
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Main.route,
        enterTransition = { fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(300)) }
    ) {
        composable(Screen.Main.route) {
            MainScreen(navController)
        }

        composable(
            route = Screen.TaskDetail.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            TaskDetailScreen(
                taskId = backStackEntry.arguments?.getString("taskId") ?: "",
                navController = navController
            )
        }

        composable(
            route = Screen.AddTask.route,
            arguments = listOf(navArgument("date") {
                type = NavType.StringType; nullable = true; defaultValue = null
            })
        ) { backStackEntry ->
            AddEditTaskScreen(
                date = backStackEntry.arguments?.getString("date"),
                taskId = null,
                navController = navController
            )
        }

        composable(
            route = Screen.EditTask.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            AddEditTaskScreen(
                date = null,
                taskId = backStackEntry.arguments?.getString("taskId") ?: "",
                navController = navController
            )
        }
    }
}

@Composable
fun MainScreen(rootNavController: NavController) {
    val mainScreens = listOf(
        Screen.Home,
        Screen.Tasks,
        Screen.Calendar,
        Screen.Profile
    )

    val pagerState = rememberPagerState(pageCount = { mainScreens.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            StudyHubBottomBar(
                currentRoute = mainScreens[pagerState.currentPage].route,
                onItemSelected = { screen ->
                    val index = mainScreens.indexOf(screen)
                    if (index != -1) {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                }
            )
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            beyondViewportPageCount = 3
        ) { page ->
            // Visual effect transition
            val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            val absOffset = abs(pageOffset)
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Fade out when moving away
                        alpha = 1f - (absOffset * 0.3f).coerceIn(0f, 1f)
                        
                        // Slight scale down when moving away
                        val scale = 1f - (absOffset * 0.05f).coerceIn(0f, 1f)
                        scaleX = scale
                        scaleY = scale
                        
                        // Slight rotation or translation could be added here for "premium" feel
                    }
            ) {
                when (mainScreens[page]) {
                    Screen.Home -> HomeScreen(rootNavController)
                    Screen.Tasks -> TasksScreen(rootNavController)
                    Screen.Calendar -> CalendarScreen(rootNavController)
                    Screen.Profile -> ProfileScreen(rootNavController)
                    else -> Unit
                }
            }
        }
    }
}

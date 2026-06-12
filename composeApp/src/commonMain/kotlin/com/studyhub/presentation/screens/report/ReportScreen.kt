package com.studyhub.presentation.screens.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.studyhub.presentation.components.ErrorView
import com.studyhub.presentation.components.LoadingView
import com.studyhub.presentation.screens.report.components.*
import com.studyhub.presentation.theme.Spacing
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(navController: NavController) {
    val viewModel: ReportViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan Belajar") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is ReportUiState.Loading -> LoadingView(Modifier.padding(padding))
            is ReportUiState.Error -> ErrorView(
                message = state.message,
                onRetry = { /* Manual refresh could be added */ },
                modifier = Modifier.padding(padding)
            )
            is ReportUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(Spacing.normal),
                    verticalArrangement = Arrangement.spacedBy(Spacing.large)
                ) {
                    // Summary Metrics
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                        ) {
                            MetricCard(Modifier.weight(1f), "Total Tugas", state.totalTasksMonthly.toString(), MaterialTheme.colorScheme.primary)
                            MetricCard(Modifier.weight(1f), "Selesai", "${state.completionRateMonthly.toInt()}%", MaterialTheme.colorScheme.secondary)
                            MetricCard(Modifier.weight(1f), "Fokus", "${state.totalFocusMinutesMonthly}m", MaterialTheme.colorScheme.tertiary)
                        }
                    }

                    // 1. Daily Usage Chart
                    item {
                        ChartCard("Durasi Penggunaan (7 Hari Terakhir)") {
                            DailyUsageBarChart(state.dailyUsage)
                        }
                    }

                    // 2. Focus Trend Chart
                    item {
                        ChartCard("Tren Konsistensi Fokus") {
                            FocusTrendLineChart(state.focusTrend)
                        }
                    }

                    // 3. Task Status Donut Chart
                    item {
                        ChartCard("Status Penyelesaian Tugas (Bulanan)") {
                            TaskStatusDonutChart(
                                onTime = state.taskStatus.first,
                                late = state.taskStatus.second,
                                unfinished = state.taskStatus.third
                            )
                        }
                    }

                    // 4. Monthly Activity Stacked Bar
                    item {
                        ChartCard("Aktivitas Tugas Bulanan") {
                            Column {
                                MonthlyActivityStackedBarChart(
                                    data = state.monthlyActivity,
                                    overdueData = state.monthlyOverdue
                                )
                                Spacer(Modifier.height(Spacing.normal))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    LegendIndicator(Color(0xFF64B5F6), "Ditambah")
                                    Spacer(Modifier.width(Spacing.medium))
                                    LegendIndicator(Color(0xFF81C784), "Selesai")
                                    Spacer(Modifier.width(Spacing.medium))
                                    LegendIndicator(Color(0xFFFFB74D), "Deadline")
                                }
                            }
                        }
                    }
                    
                    item { Spacer(Modifier.height(40.dp)) }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(modifier: Modifier, label: String, value: String, color: Color) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(Spacing.medium), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ChartCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.large)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(Spacing.large))
            content()
        }
    }
}

@Composable
private fun LegendIndicator(color: Color, label: String) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(8.dp).background(color))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

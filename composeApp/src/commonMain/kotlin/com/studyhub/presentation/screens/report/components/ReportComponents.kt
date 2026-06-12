package com.studyhub.presentation.screens.report.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.studyhub.presentation.theme.Spacing

@Composable
fun DailyUsageBarChart(
    data: List<Pair<String, Int>>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary
) {
    val maxVal = (data.maxOfOrNull { it.second } ?: 0).coerceAtLeast(1)
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)

    Canvas(modifier = modifier.fillMaxWidth().height(200.dp)) {
        val width = size.width
        val height = size.height
        val barWidth = (width / (data.size * 2))
        val space = (width / (data.size * 2))

        data.forEachIndexed { index, pair ->
            val barHeight = (pair.second.toFloat() / maxVal) * (height - 60f)
            val x = space + index * (barWidth + space * 2)
            val y = height - barHeight - 40f

            // Draw bar
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
            )

            // Draw label
            val textLayoutResult = textMeasurer.measure(pair.first, labelStyle)
            drawText(
                textLayoutResult,
                color = labelStyle.color,
                topLeft = Offset(x + (barWidth - textLayoutResult.size.width) / 2, height - 30f)
            )
            
            // Draw value
            if (pair.second > 0) {
                val valStyle = labelStyle.copy(fontSize = 9.sp)
                val valueLayout = textMeasurer.measure(pair.second.toString(), valStyle)
                drawText(
                    valueLayout,
                    color = valStyle.color,
                    topLeft = Offset(x + (barWidth - valueLayout.size.width) / 2, y - 20f)
                )
            }
        }
    }
}

@Composable
fun FocusTrendLineChart(
    data: List<Int>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.secondary
) {
    val maxVal = (data.maxOfOrNull { it } ?: 0).coerceAtLeast(1)
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
    val days = listOf("S", "S", "R", "K", "J", "S", "M")

    Canvas(modifier = modifier.fillMaxWidth().height(200.dp)) {
        val width = size.width
        val height = size.height - 40f
        val stepX = width / (data.size - 1).coerceAtLeast(1)

        val path = Path()
        val fillPath = Path()

        data.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - (value.toFloat() / maxVal) * (height - 40f)

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }

            if (index == data.size - 1) {
                fillPath.lineTo(x, height)
                fillPath.close()
            }

            // Draw Day Label
            if (index < days.size) {
                val textLayout = textMeasurer.measure(days[index], labelStyle)
                drawText(textLayout, color = labelStyle.color, topLeft = Offset(x - textLayout.size.width / 2, height + 10f))
            }
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.3f), Color.Transparent)
            )
        )
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Dots
        data.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - (value.toFloat() / maxVal) * (height - 40f)
            drawCircle(color = lineColor, radius = 4.dp.toPx(), center = Offset(x, y))
            drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(x, y))
        }
    }
}

@Composable
fun TaskStatusDonutChart(
    onTime: Int,
    late: Int,
    unfinished: Int,
    modifier: Modifier = Modifier
) {
    val total = (onTime + late + unfinished).coerceAtLeast(1)
    val onTimeProp = onTime.toFloat() / total
    val lateProp = late.toFloat() / total
    val unfinishedProp = unfinished.toFloat() / total

    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.outlineVariant
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.large)
    ) {
        Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 25.dp.toPx()
                
                var startAngle = -90f
                
                // On Time
                drawArc(
                    color = colors[0],
                    startAngle = startAngle,
                    sweepAngle = onTimeProp * 360f,
                    useCenter = false,
                    style = Stroke(strokeWidth, cap = StrokeCap.Butt)
                )
                startAngle += onTimeProp * 360f

                // Late
                drawArc(
                    color = colors[1],
                    startAngle = startAngle,
                    sweepAngle = lateProp * 360f,
                    useCenter = false,
                    style = Stroke(strokeWidth, cap = StrokeCap.Butt)
                )
                startAngle += lateProp * 360f

                // Unfinished
                drawArc(
                    color = colors[2],
                    startAngle = startAngle,
                    sweepAngle = unfinishedProp * 360f,
                    useCenter = false,
                    style = Stroke(strokeWidth, cap = StrokeCap.Butt)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${((onTime + late).toFloat() / total * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text("Selesai", style = MaterialTheme.typography.labelSmall)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LegendItem(colors[0], "Tepat Waktu", onTime, total)
            LegendItem(colors[1], "Terlambat", late, total)
            LegendItem(colors[2], "Belum Selesai", unfinished, total)
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String, count: Int, total: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Text(
            "$label (${(count.toFloat() / total * 100).toInt()}%)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MonthlyActivityStackedBarChart(
    data: List<Triple<String, Int, Int>>, // Week, Added, Completed
    overdueData: List<Int>,
    modifier: Modifier = Modifier
) {
    val maxVal = data.maxOfOrNull { it.second + it.third }?.coerceAtLeast(1) ?: 1
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)

    Canvas(modifier = modifier.fillMaxWidth().height(200.dp)) {
        val width = size.width
        val height = size.height - 40f
        val barWidth = width / (data.size * 2)
        val space = width / (data.size * 2)

        data.forEachIndexed { index, triple ->
            val x = space + index * (barWidth + space * 2)
            
            val addedHeight = (triple.second.toFloat() / maxVal) * (height - 40f)
            val completedHeight = (triple.third.toFloat() / maxVal) * (height - 40f)
            val overdueHeight = if (index < overdueData.size) (overdueData[index].toFloat() / maxVal) * (height - 40f) else 0f

            // Stack: Overdue (bottom), Completed (middle), Added (top)
            var currentY = height
            
            // Overdue
            if (overdueHeight > 0) {
                drawRect(
                    color = Color(0xFFFFB74D), // Warning orange
                    topLeft = Offset(x, currentY - overdueHeight),
                    size = Size(barWidth, overdueHeight)
                )
                currentY -= overdueHeight
            }

            // Completed
            if (completedHeight > 0) {
                drawRect(
                    color = Color(0xFF81C784), // Success green
                    topLeft = Offset(x, currentY - completedHeight),
                    size = Size(barWidth, completedHeight)
                )
                currentY -= completedHeight
            }

            // Added
            if (addedHeight > 0) {
                drawRect(
                    color = Color(0xFF64B5F6), // Info blue
                    topLeft = Offset(x, currentY - addedHeight),
                    size = Size(barWidth, addedHeight)
                )
            }

            // Label
            val textLayout = textMeasurer.measure(triple.first, labelStyle)
            drawText(textLayout, color = labelStyle.color, topLeft = Offset(x + (barWidth - textLayout.size.width) / 2, height + 10f))
        }
    }
}

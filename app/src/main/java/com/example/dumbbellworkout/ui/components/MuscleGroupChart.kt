package com.example.dumbbellworkout.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val muscleColors = mapOf(
    "Грудь" to Color(0xFFE53935),
    "Спина" to Color(0xFF1E88E5),
    "Плечи" to Color(0xFFFFA726),
    "Бицепс" to Color(0xFF66BB6A),
    "Трицепс" to Color(0xFFAB47BC),
    "Ноги" to Color(0xFF26C6DA),
    "Икры" to Color(0xFF78909C),
    "Кор" to Color(0xFFFFEE58),
    "Предплечья" to Color(0xFF8D6E63),
    "Другое" to Color(0xFF9E9E9E)
)

@Composable
fun MuscleDistributionPieChart(
    muscleDistribution: Map<String, Float>
) {
    val totalVolume = muscleDistribution.values.sum()
    if (totalVolume <= 0f) {
        Text(
            "Начните тренироваться, чтобы увидеть распределение нагрузки",
            color = Color.White.copy(alpha = 0.3f), fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
        )
        return
    }

    val animatedSweep by animateFloatAsState(
        targetValue = 360f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing), label = "pie"
    )

    val sortedMuscles = muscleDistribution.entries.sortedByDescending { it.value }
    val desc = sortedMuscles.joinToString(", ") { "${it.key}: ${(it.value / totalVolume * 100).toInt()}%" }

    Column(modifier = Modifier.semantics { contentDescription = "Распределение нагрузки по мышцам. $desc" }) {
        Box(modifier = Modifier.fillMaxWidth().height(240.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(200.dp)) {
                val strokeWidth = 32.dp.toPx()
                var startAngle = -90f
                sortedMuscles.forEach { (muscle, volume) ->
                    val sweep = (volume / totalVolume) * animatedSweep
                    drawArc(
                        color = muscleColors[muscle] ?: Color.Gray,
                        startAngle = startAngle,
                        sweepAngle = sweep - 2f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = Size(size.width - strokeWidth, size.height - strokeWidth)
                    )
                    startAngle += sweep
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${String.format("%.0f", totalVolume)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("кг объём", fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f))
            }
        }

        // Legend
        sortedMuscles.forEach { (muscle, volume) ->
            val percentage = (volume / totalVolume * 100).toInt()
            val color = muscleColors[muscle] ?: Color.Gray
            val shape = RoundedCornerShape(12.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clip(shape)
                    .background(color.copy(alpha = 0.06f))
                    .border(1.dp, color.copy(alpha = 0.12f), shape)
                    .padding(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(muscle, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White, modifier = Modifier.weight(1f))
                    Text("$percentage%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${String.format("%.0f", volume)} кг", fontSize = 12.sp, color = Color.White.copy(alpha = 0.4f))
                }
            }
        }
    }
}

@Composable
fun WeeklyMuscleVolumeChart(
    weeklyData: Map<String, Map<String, Float>>
) {
    if (weeklyData.isEmpty()) {
        Text("Недостаточно данных для понедельного анализа", color = Color.White.copy(alpha = 0.3f), fontSize = 13.sp)
        return
    }

    val sortedWeeks = weeklyData.keys.sorted().takeLast(8) // Последние 8 недель
    val allMuscles = weeklyData.values.flatMap { it.keys }.toSet().sorted()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Понедельный объём нагрузки по мышечным группам за последние ${sortedWeeks.size} недель" }
    ) {
        Text("Нагрузка по неделям", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.height(12.dp))

        sortedWeeks.forEach { weekKey ->
            val muscleMap = weeklyData[weekKey] ?: return@forEach
            val totalWeekVolume = muscleMap.values.sum()

            Column(modifier = Modifier.padding(bottom = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(weekKey, fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                    Text("${String.format("%.0f", totalWeekVolume)} кг", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.7f))
                }
                Spacer(modifier = Modifier.height(4.dp))

                // Stacked bar
                if (totalWeekVolume > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                    ) {
                        muscleMap.entries.sortedByDescending { it.value }.forEach { (muscle, volume) ->
                            val fraction = volume / totalWeekVolume
                            if (fraction > 0.02f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fraction)
                                        .background(muscleColors[muscle] ?: Color.Gray)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

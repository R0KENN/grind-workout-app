package com.example.dumbbellworkout

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*
import android.content.Context

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeatMapScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val workoutDates = remember { StreakManager.getWorkoutDates(context) }
    val workoutIntensity = remember { getWorkoutIntensityMap(context) }

    val cal = Calendar.getInstance()
    val currentYear = cal.get(Calendar.YEAR)
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    // build 12 months of data
    val monthsData = remember {
        (0..11).map { monthOffset ->
            val monthCal = Calendar.getInstance()
            monthCal.set(Calendar.YEAR, currentYear)
            monthCal.set(Calendar.MONTH, monthOffset)
            monthCal.set(Calendar.DAY_OF_MONTH, 1)

            val daysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val firstDayOfWeek = (monthCal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday = 0

            val monthName = SimpleDateFormat("LLLL", Locale("ru")).format(monthCal.time)
                .replaceFirstChar { it.uppercase() }

            Triple(monthName, firstDayOfWeek, (1..daysInMonth).map { day ->
                monthCal.set(Calendar.DAY_OF_MONTH, day)
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(monthCal.time)
                val intensity = workoutIntensity[dateStr] ?: 0
                Triple(day, dateStr, intensity)
            })
        }
    }

    val totalWorkouts = workoutDates.size
    val currentStreak = StreakManager.getCurrentStreak(context)
    val bestStreak = StreakManager.getBestStreak(context)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("📅 Тепловая карта", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 24.sp, color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // stats summary
            item {
                GlassCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatColumn("🏋️", "$totalWorkouts", "Тренировок")
                        StatColumn("🔥", "$currentStreak", "Серия")
                        StatColumn("🏆", "$bestStreak", "Рекорд серии")
                    }
                }
            }

            // legend
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Меньше", fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.width(8.dp))
                    listOf(0, 1, 2, 3, 4).forEach { level ->
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(getHeatColor(level))
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Больше", fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f))
                }
            }

            // day-of-week header
            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.width(40.dp))
                    listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { day ->
                        Text(
                            day,
                            modifier = Modifier.weight(1f),
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // months
            monthsData.forEach { (monthName, firstDay, days) ->
                item {
                    Column {
                        Text(
                            monthName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        val weeks = mutableListOf<List<Triple<Int, String, Int>?>>()
                        var currentWeek = MutableList<Triple<Int, String, Int>?>(7) { null }

                        // fill initial empty days
                        var dayIndex = firstDay

                        days.forEach { dayData ->
                            currentWeek[dayIndex] = dayData
                            dayIndex++
                            if (dayIndex >= 7) {
                                weeks.add(currentWeek.toList())
                                currentWeek = MutableList(7) { null }
                                dayIndex = 0
                            }
                        }
                        if (currentWeek.any { it != null }) {
                            weeks.add(currentWeek.toList())
                        }

                        weeks.forEach { week ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 1.dp)
                            ) {
                                // week number placeholder
                                Spacer(modifier = Modifier.width(40.dp))

                                week.forEach { dayData ->
                                    if (dayData != null) {
                                        val (day, dateStr, intensity) = dayData
                                        val isToday = dateStr == today
                                        val color = getHeatColor(intensity)

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .padding(1.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(color)
                                                .then(
                                                    if (isToday) Modifier.border(
                                                        1.dp,
                                                        Color.White,
                                                        RoundedCornerShape(3.dp)
                                                    ) else Modifier
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // optionally show day number for readability
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun StatColumn(emoji: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f))
    }
}

private fun getHeatColor(intensity: Int): Color = when (intensity) {
    0 -> Color(0xFF161622)
    1 -> Color(0xFF2D1B69)
    2 -> Color(0xFF4A2B99)
    3 -> Color(0xFF6C63FF)
    4 -> Color(0xFF9D97FF)
    else -> Color(0xFF9D97FF)
}

private fun getWorkoutIntensityMap(context: Context): Map<String, Int> {
    val prefs = context.getSharedPreferences("workout_log", Context.MODE_PRIVATE)
    val allEntries = prefs.all
    val dateIntensity = mutableMapOf<String, Int>()
    val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    allEntries.forEach { (_, value) ->
        if (value is String) {
            try {
                val parts = value.split("|")
                parts.forEach { entry ->
                    val fields = entry.split(",")
                    if (fields.size >= 3) {
                        val timestamp = fields[0].toLongOrNull() ?: 0L
                        if (timestamp > 0) {
                            val date = df.format(Date(timestamp))
                            dateIntensity[date] = (dateIntensity[date] ?: 0) + 1
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    // normalize to 0-4 scale
    val maxSets = dateIntensity.values.maxOrNull() ?: 1
    return dateIntensity.mapValues { (_, sets) ->
        when {
            sets == 0 -> 0
            sets <= maxSets * 0.25 -> 1
            sets <= maxSets * 0.5 -> 2
            sets <= maxSets * 0.75 -> 3
            else -> 4
        }
    }
}

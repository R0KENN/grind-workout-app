package com.example.dumbbellworkout

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

// ═══════════════════════════════════════
// WORKOUT DETAIL SCREEN
// ═══════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(workoutId: String, onBack: () -> Unit, onStart: () -> Unit) {
    val context = LocalContext.current
    val workout = ALL_WORKOUTS[workoutId] ?: return

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(workout.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${workout.exercises.size} упр. · ${workout.time}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад") }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                val shape = RoundedCornerShape(14.dp)
                Box(
                    modifier = Modifier.fillMaxWidth().clip(shape)
                        .background(Brush.horizontalGradient(listOf(Purple, Purple.copy(alpha = 0.7f))))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), shape)
                        .clickable { onStart() }.padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) { Text("▶  Начать тренировку", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White) }
            }
        },
        containerColor = Color.Black
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(workout.exercises) { index, exercise ->
                val shape = RoundedCornerShape(14.dp)
                Box(
                    modifier = Modifier.fillMaxWidth().clip(shape)
                        .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.02f))))
                        .border(1.dp, Color.White.copy(alpha = 0.06f), shape)
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Purple.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                            Text("${exercise.num}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Purple)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(exercise.name, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Color.White)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 2.dp)) {
                                Text("${exercise.sets}×${exercise.reps}", fontSize = 12.sp, color = Purple.copy(alpha = 0.8f))
                                Text(exercise.restDisplay, fontSize = 12.sp, color = Color.White.copy(alpha = 0.3f))
                                val lastW = WorkoutLog.getLastWeight(context, exercise.name)
                                if (lastW != null) Text("$lastW кг", fontSize = 12.sp, color = Color.White.copy(alpha = 0.3f))
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

// ═══════════════════════════════════════
// SCHEDULE SCREEN
// ═══════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(onBack: () -> Unit) {
    val calendar = Calendar.getInstance()
    val todayIndex = when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> 0; Calendar.TUESDAY -> 1; Calendar.WEDNESDAY -> 2
        Calendar.THURSDAY -> 3; Calendar.FRIDAY -> 4; Calendar.SATURDAY -> 5
        Calendar.SUNDAY -> 6; else -> 0
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Расписание", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(DAY_NAMES.size) { index ->
                val workoutId = SCHEDULE[index] ?: "rest"
                val workout = ALL_WORKOUTS[workoutId]
                val isToday = index == todayIndex
                val isRest = workoutId == "rest"
                val accentColor = when { isToday && !isRest -> Purple; isToday && isRest -> Green; else -> Color.White }
                val shape = RoundedCornerShape(14.dp)
                Box(
                    modifier = Modifier.fillMaxWidth().clip(shape)
                        .background(if (isToday) accentColor.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.04f))
                        .border(1.dp, if (isToday) accentColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.06f), shape)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(DAY_NAMES[index].take(2).uppercase(), fontWeight = FontWeight.Bold, fontSize = 13.sp,
                            color = if (isToday) accentColor else Color.White.copy(alpha = 0.35f), modifier = Modifier.width(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (isRest) "Отдых" else workout?.name ?: "",
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal, fontSize = 14.sp,
                                color = if (isRest) Color.White.copy(alpha = 0.4f) else Color.White)
                            if (!isRest && workout != null) {
                                Text("${workout.exercises.size} упр. · ${workout.time}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.3f))
                            }
                        }
                        if (isToday) {
                            Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(accentColor.copy(alpha = 0.15f)).padding(horizontal = 6.dp, vertical = 3.dp)) {
                                Text("сейчас", fontSize = 10.sp, color = accentColor, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════
// STATS SCREEN with 1RM + Muscle Pie Chart
// ═══════════════════════════════════════

private val muscleColors = mapOf(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val allLogs = remember { WorkoutLog.loadAllLogs(context) }
    val (streak, _, totalWorkouts) = remember { WorkoutLog.calculateStreak(context) }
    val todayTonnage = remember { WorkoutLog.getTodayTonnage(context) }
    val muscleDistribution = remember { WorkoutLog.getMuscleDistribution(context) }
    val totalVolume = muscleDistribution.values.sum()

    // Tab state: 0 = Overview, 1 = 1RM, 2 = Muscles
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Статистика", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top stats row
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        Triple("🔥", "$streak", "Недель"),
                        Triple("💪", "$totalWorkouts", "Тренировок"),
                        Triple("🏋️", "${String.format("%.0f", todayTonnage)}", "Тоннаж")
                    ).forEach { (icon, value, label) ->
                        Box(modifier = Modifier.weight(1f)) {
                            val shape = RoundedCornerShape(14.dp)
                            Box(
                                modifier = Modifier.fillMaxWidth().clip(shape).background(Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, Color.White.copy(alpha = 0.06f), shape).padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(icon, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f))
                                }
                            }
                        }
                    }
                }
            }

            // Tabs
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Обзор", "1RM", "Мышцы").forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        val shape = RoundedCornerShape(10.dp)
                        Box(
                            modifier = Modifier.weight(1f).clip(shape)
                                .background(if (isSelected) Purple.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.04f))
                                .border(1.dp, if (isSelected) Purple.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.06f), shape)
                                .clickable { selectedTab = index }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(title, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Purple else Color.White.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            when (selectedTab) {
                // ──── TAB 0: Overview ────
                0 -> {
                    item {
                        Text("Рекорды", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White.copy(alpha = 0.4f))
                    }

                    val allExercises = ALL_WORKOUTS.values.flatMap { it.exercises }.distinctBy { it.name }
                    val exercisesWithRecords = allExercises.filter { WorkoutLog.getMaxWeight(context, it.name) > 0 }

                    items(exercisesWithRecords, key = { "rec_${it.name}" }) { exercise ->
                        val maxW = WorkoutLog.getMaxWeight(context, exercise.name)
                        val shape = RoundedCornerShape(12.dp)
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(shape).background(Color.White.copy(alpha = 0.04f))
                                .border(1.dp, Color.White.copy(alpha = 0.05f), shape).padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(exercise.name, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.weight(1f))
                                Text("$maxW кг", fontWeight = FontWeight.Bold, color = Purple, fontSize = 13.sp)
                            }
                        }
                    }

                    if (exercisesWithRecords.isEmpty()) {
                        item {
                            Text("Пока нет рекордов. Начните тренировку! 💪", textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), color = Color.White.copy(alpha = 0.3f), fontSize = 13.sp)
                        }
                    }

                    item {
                        Text("Последние тренировки", fontWeight = FontWeight.Bold, fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.4f), modifier = Modifier.padding(top = 4.dp))
                    }

                    val recentDates = allLogs.keys.sortedDescending().take(7)
                    items(recentDates, key = { "recent_$it" }) { date ->
                        val log = allLogs[date] ?: return@items
                        val tonnage = log.sets.sumOf { (it.weight * it.reps).toDouble() }.toFloat()
                        val shape = RoundedCornerShape(12.dp)
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(shape).background(Color.White.copy(alpha = 0.04f))
                                .border(1.dp, Color.White.copy(alpha = 0.05f), shape).padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(date, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Color.White)
                                    Text("${log.sets.size} подходов", fontSize = 11.sp, color = Color.White.copy(alpha = 0.35f))
                                }
                                Text("${String.format("%.0f", tonnage)} кг", fontWeight = FontWeight.Bold, color = Green, fontSize = 13.sp)
                            }
                        }
                    }

                    if (recentDates.isEmpty()) {
                        item {
                            Text("Пока нет данных", textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), color = Color.White.copy(alpha = 0.3f), fontSize = 13.sp)
                        }
                    }
                }

                // ──── TAB 1: 1RM Calculator ────
                1 -> {
                    item {
                        val shape = RoundedCornerShape(14.dp)
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(shape)
                                .background(Purple.copy(alpha = 0.06f))
                                .border(1.dp, Purple.copy(alpha = 0.1f), shape)
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💡", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("1RM (одноповторный максимум) рассчитан по формуле Эпли на основе ваших лучших подходов.",
                                    fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f), lineHeight = 18.sp)
                            }
                        }
                    }

                    val allExercises = ALL_WORKOUTS.values.flatMap { it.exercises }.distinctBy { it.name }
                    val exercisesWith1RM = allExercises.map { ex ->
                        Triple(ex.name, WorkoutLog.getBest1RM(context, ex.name), WorkoutLog.getMaxWeight(context, ex.name))
                    }.filter { it.second > 0 }.sortedByDescending { it.second }

                    items(exercisesWith1RM, key = { "1rm_${it.first}" }) { (name, est1rm, maxWeight) ->
                        val shape = RoundedCornerShape(12.dp)
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(shape)
                                .background(Color.White.copy(alpha = 0.04f))
                                .border(1.dp, Color.White.copy(alpha = 0.05f), shape)
                                .padding(12.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.White)
                                    Text("Макс. вес: $maxWeight кг", fontSize = 11.sp, color = Color.White.copy(alpha = 0.35f))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${String.format("%.1f", est1rm)} кг", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                                    Text("расч. 1RM", fontSize = 10.sp, color = Color(0xFFFFD700).copy(alpha = 0.5f))
                                }
                            }
                        }
                    }

                    if (exercisesWith1RM.isEmpty()) {
                        item {
                            Text("Начните тренироваться, чтобы увидеть расчёт 1RM 🏋️", textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), color = Color.White.copy(alpha = 0.3f), fontSize = 13.sp)
                        }
                    }
                }

                // ──── TAB 2: Muscle Distribution Pie Chart ────
                2 -> {
                    item {
                        if (totalVolume > 0) {
                            // Animated pie chart
                            val animatedSweep by animateFloatAsState(
                                targetValue = 360f,
                                animationSpec = tween(1200, easing = FastOutSlowInEasing),
                                label = "pie_sweep"
                            )

                            Box(
                                modifier = Modifier.fillMaxWidth().height(240.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(200.dp)) {
                                    val strokeWidth = 32.dp.toPx()
                                    var startAngle = -90f
                                    val sortedMuscles = muscleDistribution.entries.sortedByDescending { it.value }

                                    sortedMuscles.forEach { (muscle, volume) ->
                                        val sweep = (volume / totalVolume) * animatedSweep
                                        val color = muscleColors[muscle] ?: Color.Gray

                                        drawArc(
                                            color = color,
                                            startAngle = startAngle,
                                            sweepAngle = sweep - 2f, // gap between segments
                                            useCenter = false,
                                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                                            size = Size(size.width - strokeWidth, size.height - strokeWidth)
                                        )
                                        startAngle += sweep
                                    }
                                }

                                // Center text
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${String.format("%.0f", totalVolume)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text("кг объём", fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f))
                                }
                            }
                        } else {
                            Text("Начните тренироваться, чтобы увидеть распределение нагрузки", textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), color = Color.White.copy(alpha = 0.3f), fontSize = 13.sp)
                        }
                    }

                    // Legend
                    if (totalVolume > 0) {
                        val sortedMuscles = muscleDistribution.entries.sortedByDescending { it.value }
                        items(sortedMuscles.toList(), key = { "muscle_${it.key}" }) { (muscle, volume) ->
                            val percentage = (volume / totalVolume * 100).toInt()
                            val color = muscleColors[muscle] ?: Color.Gray
                            val shape = RoundedCornerShape(12.dp)

                            Box(
                                modifier = Modifier.fillMaxWidth().clip(shape)
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
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

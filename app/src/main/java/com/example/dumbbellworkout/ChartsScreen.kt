package com.example.dumbbellworkout

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val allLogs = remember { WorkoutLog.loadAllLogs(context) }
    val bwData = remember { WorkoutLog.loadBodyweight(context) }

    val allExercises = remember {
        val set = mutableSetOf<String>()
        for ((_, log) in allLogs) { for (s in log.sets) { set.add(s.exerciseName) } }
        set.sorted()
    }

    var selectedExercise by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Графики", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedExercise != null) selectedExercise = null else onBack()
                    }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад") }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        if (selectedExercise != null) {
            ExerciseDetailChart(
                exerciseName = selectedExercise!!,
                allLogs = allLogs,
                modifier = Modifier.padding(padding)
            )
        } else {
            ChartsMainList(
                allLogs = allLogs,
                bwData = bwData,
                allExercises = allExercises,
                onSelectExercise = { selectedExercise = it },
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun ChartsMainList(
    allLogs: Map<String, DayLog>,
    bwData: Map<String, Float>,
    allExercises: List<String>,
    onSelectExercise: (String) -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Body weight chart
        item(key = "bodyweight") {
            val shape = RoundedCornerShape(16.dp)
            Box(
                modifier = Modifier.fillMaxWidth().clip(shape)
                    .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.02f))))
                    .border(1.dp, Color.White.copy(alpha = 0.06f), shape)
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFFFA726).copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                            Text("⚖️", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Вес тела", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                            if (bwData.size >= 2) {
                                val sorted = bwData.toSortedMap()
                                val diff = sorted.values.last() - sorted.values.first()
                                val sign = if (diff >= 0) "+" else ""
                                Text("$sign${String.format("%.1f", diff)} кг", fontSize = 12.sp,
                                    color = if (diff <= 0) Color(0xFF4CAF50) else Color(0xFFFFA726))
                            } else {
                                Text("Нужно минимум 2 записи", fontSize = 12.sp, color = Color.White.copy(alpha = 0.3f))
                            }
                        }
                    }
                    if (bwData.size >= 2) {
                        Spacer(modifier = Modifier.height(12.dp))
                        val bwPoints = bwData.toSortedMap().values.toList()
                        GlassLineChart(data = bwPoints, lineColor = Color(0xFFFFA726), height = 100)
                    }
                }
            }
        }

        // Tonnage chart
        item(key = "tonnage") {
            val tonnagePoints = remember {
                allLogs.keys.sorted().mapNotNull { date ->
                    val t = WorkoutLog.calculateTonnage(context, date)
                    if (t > 0) t else null
                }
            }

            val shape = RoundedCornerShape(16.dp)
            Box(
                modifier = Modifier.fillMaxWidth().clip(shape)
                    .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.02f))))
                    .border(1.dp, Color.White.copy(alpha = 0.06f), shape)
                    .padding(16.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF4CAF50).copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                            Text("🏋️", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Тоннаж", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                            if (tonnagePoints.size >= 2) {
                                val diff = tonnagePoints.last() - tonnagePoints.first()
                                val sign = if (diff >= 0) "+" else ""
                                Text("$sign${String.format("%.0f", diff)} кг", fontSize = 12.sp, color = Color(0xFF4CAF50))
                            } else {
                                Text("Нужно минимум 2 тренировки", fontSize = 12.sp, color = Color.White.copy(alpha = 0.3f))
                            }
                        }
                    }
                    if (tonnagePoints.size >= 2) {
                        Spacer(modifier = Modifier.height(12.dp))
                        GlassLineChart(data = tonnagePoints, lineColor = Color(0xFF4CAF50), height = 100)
                    }
                }
            }
        }

        // Exercise list header
        item(key = "exercises_header") {
            Text("Прогресс по упражнениям", fontWeight = FontWeight.Bold, fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.4f), modifier = Modifier.padding(top = 4.dp))
        }

        // Exercise items
        items(allExercises, key = { "ex_$it" }) { exName ->
            val maxW = WorkoutLog.getMaxWeight(context, exName)
            val est1RM = WorkoutLog.getBest1RM(context, exName)

            // Mini sparkline data
            val sparkData = remember {
                allLogs.keys.sorted().mapNotNull { date ->
                    allLogs[date]?.sets?.filter { it.exerciseName == exName }?.maxOfOrNull { it.weight }
                }
            }

            val shape = RoundedCornerShape(14.dp)
            Box(
                modifier = Modifier.fillMaxWidth().clip(shape)
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(1.dp, Color.White.copy(alpha = 0.05f), shape)
                    .clickable { onSelectExercise(exName) }
                    .padding(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(exName, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color.White)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (maxW > 0) Text("Макс: $maxW кг", fontSize = 11.sp, color = Purple.copy(alpha = 0.7f))
                            if (est1RM > 0) Text("1RM: ${String.format("%.0f", est1RM)}", fontSize = 11.sp, color = Color(0xFFFFD700).copy(alpha = 0.6f))
                        }
                    }
                    // Mini sparkline
                    if (sparkData.size >= 2) {
                        Box(modifier = Modifier.width(60.dp).height(28.dp)) {
                            GlassSparkline(data = sparkData, lineColor = Purple)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("›", fontSize = 20.sp, color = Color.White.copy(alpha = 0.2f))
                }
            }
        }

        if (allExercises.isEmpty()) {
            item {
                Text("Начните тренироваться, чтобы увидеть графики 📈", textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), color = Color.White.copy(alpha = 0.3f), fontSize = 13.sp)
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun ExerciseDetailChart(
    exerciseName: String,
    allLogs: Map<String, DayLog>,
    modifier: Modifier
) {
    val dataPoints = remember {
        allLogs.keys.sorted().mapNotNull { date ->
            val log = allLogs[date] ?: return@mapNotNull null
            val maxW = log.sets.filter { it.exerciseName == exerciseName }.maxOfOrNull { it.weight }
            if (maxW != null) Pair(date, maxW) else null
        }
    }

    val values = dataPoints.map { it.second }
    val est1RM = if (values.isNotEmpty()) {
        dataPoints.maxOf { (_, w) ->
            val reps = allLogs.values.flatMap { it.sets }.filter { it.exerciseName == exerciseName && it.weight == w }.maxOfOrNull { it.reps } ?: 1
            WorkoutLog.calculate1RM(w, reps)
        }
    } else 0f

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header
        item(key = "header") {
            Column {
                Text(exerciseName, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (values.isNotEmpty()) {
                        val diff = values.last() - values.first()
                        val sign = if (diff >= 0) "+" else ""
                        Text("Прогресс: $sign${String.format("%.1f", diff)} кг", fontSize = 13.sp,
                            fontWeight = FontWeight.Bold, color = if (diff >= 0) Color(0xFF4CAF50) else Color(0xFFFF6B6B))
                    }
                    if (est1RM > 0) {
                        Text("1RM: ${String.format("%.0f", est1RM)} кг", fontSize = 13.sp,
                            fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                    }
                }
            }
        }

        // Stats row
        if (values.isNotEmpty()) {
            item(key = "stats") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        Triple("Макс", "${values.max()} кг", Purple),
                        Triple("Мин", "${values.min()} кг", Color(0xFF78909C)),
                        Triple("Средн", "${String.format("%.1f", values.average())} кг", Color(0xFF26C6DA))
                    ).forEach { (label, value, color) ->
                        Box(modifier = Modifier.weight(1f)) {
                            val shape = RoundedCornerShape(12.dp)
                            Box(
                                modifier = Modifier.fillMaxWidth().clip(shape)
                                    .background(color.copy(alpha = 0.08f))
                                    .border(1.dp, color.copy(alpha = 0.12f), shape)
                                    .padding(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.4f))
                                    Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Main chart
        if (values.size >= 2) {
            item(key = "chart") {
                val shape = RoundedCornerShape(16.dp)
                Box(
                    modifier = Modifier.fillMaxWidth().clip(shape)
                        .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.02f))))
                        .border(1.dp, Color.White.copy(alpha = 0.06f), shape)
                        .padding(16.dp)
                ) {
                    GlassLineChart(data = values, lineColor = Purple, height = 160)
                }
            }
        }

        // Bar chart
        if (values.isNotEmpty()) {
            item(key = "bars") {
                val shape = RoundedCornerShape(16.dp)
                Box(
                    modifier = Modifier.fillMaxWidth().clip(shape)
                        .background(Color.White.copy(alpha = 0.03f))
                        .border(1.dp, Color.White.copy(alpha = 0.04f), shape)
                        .padding(16.dp)
                ) {
                    GlassBarChart(dataPoints = dataPoints.takeLast(10), barColor = Purple)
                }
            }
        }

        // History table
        item(key = "history_header") {
            Text("История весов", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White.copy(alpha = 0.4f))
        }

        items(dataPoints.reversed().take(15), key = { "hist_${it.first}" }) { (date, weight) ->
            val shape = RoundedCornerShape(10.dp)
            Box(
                modifier = Modifier.fillMaxWidth().clip(shape)
                    .background(Color.White.copy(alpha = 0.03f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(date, fontSize = 13.sp, color = Color.White.copy(alpha = 0.4f))
                    Text("$weight кг", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Purple)
                }
            }
        }

        if (dataPoints.isEmpty()) {
            item {
                Text("Нет данных для этого упражнения", textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), color = Color.White.copy(alpha = 0.3f), fontSize = 13.sp)
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

// ═══════════════════════════════════════
// Glass Line Chart (animated)
// ═══════════════════════════════════════

@Composable
fun GlassLineChart(data: List<Float>, lineColor: Color, height: Int = 120) {
    if (data.size < 2) return

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "chart_anim"
    )

    val minVal = data.min()
    val maxVal = data.max()
    val range = if (maxVal - minVal > 0) maxVal - minVal else 1f

    Box(modifier = Modifier.fillMaxWidth().height(height.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val padding = 8.dp.toPx()
            val chartW = w - padding * 2
            val chartH = h - padding * 2

            // Grid lines
            for (i in 0..3) {
                val y = padding + chartH * i / 3f
                drawLine(Color.White.copy(alpha = 0.04f), Offset(padding, y), Offset(w - padding, y), strokeWidth = 1f)
            }

            // Build path
            val path = Path()
            val fillPath = Path()
            val pointCount = (data.size * animatedProgress).toInt().coerceAtLeast(2)

            for (i in 0 until pointCount) {
                val x = padding + chartW * i / (data.size - 1).toFloat()
                val y = padding + chartH * (1f - (data[i] - minVal) / range)
                if (i == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, h)
                    fillPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }

            // Fill gradient
            val lastX = padding + chartW * (pointCount - 1) / (data.size - 1).toFloat()
            fillPath.lineTo(lastX, h)
            fillPath.close()

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    listOf(lineColor.copy(alpha = 0.15f), lineColor.copy(alpha = 0.0f))
                )
            )

            // Line
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // Dots
            for (i in 0 until pointCount) {
                val x = padding + chartW * i / (data.size - 1).toFloat()
                val y = padding + chartH * (1f - (data[i] - minVal) / range)
                drawCircle(lineColor, radius = 3.dp.toPx(), center = Offset(x, y))
                drawCircle(Color.Black, radius = 1.5.dp.toPx(), center = Offset(x, y))
            }
        }

        // Min/max labels
        Text("${String.format("%.0f", maxVal)}", fontSize = 9.sp, color = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.align(Alignment.TopStart).padding(start = 2.dp))
        Text("${String.format("%.0f", minVal)}", fontSize = 9.sp, color = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 2.dp))
    }
}

// ═══════════════════════════════════════
// Glass Sparkline (mini chart for list items)
// ═══════════════════════════════════════

@Composable
fun GlassSparkline(data: List<Float>, lineColor: Color) {
    if (data.size < 2) return

    val minVal = data.min()
    val maxVal = data.max()
    val range = if (maxVal - minVal > 0) maxVal - minVal else 1f

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        val path = Path()
        for (i in data.indices) {
            val x = w * i / (data.size - 1).toFloat()
            val y = h * (1f - (data[i] - minVal) / range)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(path, lineColor.copy(alpha = 0.7f), style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))
    }
}

// ═══════════════════════════════════════
// Glass Bar Chart
// ═══════════════════════════════════════

@Composable
fun GlassBarChart(dataPoints: List<Pair<String, Float>>, barColor: Color) {
    if (dataPoints.isEmpty()) return

    val maxVal = dataPoints.maxOf { it.second }

    val animatedProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "bar_anim"
    )

    Column {
        Text("Последние ${dataPoints.size} тренировок", fontSize = 11.sp, color = Color.White.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            dataPoints.forEach { (date, value) ->
                val fraction = if (maxVal > 0) (value / maxVal) * animatedProgress else 0f

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    Text(String.format("%.0f", value), fontSize = 8.sp, color = barColor.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .width(16.dp)
                            .fillMaxHeight(fraction.coerceIn(0.05f, 1f))
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(Brush.verticalGradient(listOf(barColor, barColor.copy(alpha = 0.4f))))
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(date.takeLast(5), fontSize = 7.sp, color = Color.White.copy(alpha = 0.25f))
                }
            }
        }
    }
}

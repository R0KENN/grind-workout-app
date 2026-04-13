package com.example.dumbbellworkout

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*


fun calculateCompletedDays(context: Context): Int {
    val allLogs = WorkoutLog.loadAllLogs(context)
    val cal = Calendar.getInstance()
    val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
        cal.add(Calendar.DAY_OF_MONTH, -1)
    }
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val weekStart = cal.time

    return allLogs.count { (dateStr, log) ->
        if (log.sets.isEmpty()) return@count false
        try {
            val date = df.parse(dateStr)
            date != null && !date.before(weekStart)
        } catch (_: Exception) { false }
    }
}

fun calculateTotalDays(): Int = SCHEDULE.values.count { it != "rest" }

@Composable
fun AnimatedItem(
    index: Int,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 60L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically(
            initialOffsetY = { 40 },
            animationSpec = tween(400, easing = FastOutSlowInEasing)
        )
    ) {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartWorkout: (String) -> Unit,
    onViewWorkout: (String) -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToBodyweight: () -> Unit,
    onNavigateToCharts: () -> Unit,
    onNavigateToEditLog: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAchievements: () -> Unit,
    onNavigateToHeatMap: () -> Unit
) {
    val context = LocalContext.current
    val todayWorkout = remember { getTodayWorkout() }
    val isRestDay = todayWorkout.id == "rest"
    val scrollState = rememberLazyListState()

    val quotes = listOf(
        "Тяжело в зале — легко по жизни. 💪",
        "Прогресс, а не совершенство.",
        "Дисциплина — мост между целями и достижениями.",
        "Каждый повтор приближает тебя к цели.",
        "Не останавливайся, пока не станешь гордиться собой.",
        "Боль временна, гордость — навсегда. 🔥",
        "Ты сильнее, чем думаешь.",
        "Успех начинается за пределами зоны комфорта.",
        "Сегодняшняя тренировка — завтрашняя сила.",
        "Маленькие шаги ведут к большим результатам.",
        "Тело достигает того, во что верит разум.",
        "Будь сильнее своих отговорок.",
        "Каждый день — новый шанс стать лучше.",
        "Нет коротких путей к месту, которое стоит достижения.",
        "Пот — это жир, который плачет. 💧",
        "Твоё тело может всё. Убеди свой разум.",
        "Результат = постоянство × время.",
        "Один час тренировки — это 4% твоего дня.",
        "Не жди мотивацию. Создавай привычку.",
        "Сильное тело — сильный дух. 🔱",
        "Рекорды созданы, чтобы их бить.",
        "Ты не проиграл, пока не сдался.",
        "Инвестиция в себя — лучшая инвестиция.",
        "Фитнес — не наказание, а награда телу.",
        "Делай сегодня то, за что скажешь спасибо завтра.",
        "Лучшая версия тебя ждёт в зале.",
        "Нет ничего невозможного для того, кто пробует.",
        "Путь в тысячу миль начинается с одного шага.",
        "Железо не лжёт — ты либо поднял, либо нет.",
        "Постоянство бьёт талант.",
        "Чем тяжелее тренировка, тем слаще победа. 🏆",
        "Не считай дни — делай так, чтобы дни считались.",
        "Сила не приходит от побед, а от борьбы.",
        "Будь тем, кем ты хотел бы стать.",
        "Мышцы растут не в зале, а после него.",
        "Секрет успеха? Не пропускай тренировки.",
        "Ты vs ты вчерашний. Побеждай.",
        "Цель без плана — просто мечта.",
        "Поднимай тяжёлое, живи легко. ⚡",
        "Каждая тренировка — это шаг вперёд."
    )
    val todayQuote = remember {
        quotes[Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % quotes.size]
    }

    val completedDays = remember { calculateCompletedDays(context) }
    val totalDays = remember { calculateTotalDays() }
    val weeklyProgress = if (totalDays > 0) completedDays.toFloat() / totalDays else 0f
    val userLevel = remember { LevelManager.getUserLevel(context) }
    val currentStreak = remember { StreakManager.getCurrentStreak(context) }
    val advice = remember { SmartAdvice.getAdvice(context) }

    val animatedWeeklyProgress by animateFloatAsState(
        targetValue = weeklyProgress,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "weekly"
    )
    val animatedXP by animateFloatAsState(
        targetValue = userLevel.currentXP.toFloat() / userLevel.xpForNextLevel.toFloat(),
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "xp"
    )

    val levelColor = when {
        userLevel.level <= 3 -> Color(0xFF4CAF50)
        userLevel.level <= 7 -> Color(0xFF2196F3)
        userLevel.level <= 12 -> Purple
        userLevel.level <= 17 -> Color(0xFFFF9800)
        else -> Color(0xFFFF5722)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("GRIND", fontWeight = FontWeight.Bold, fontSize = 22.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Уровень + Серия + Неделя ──
            item(key = "top_stats") {
                AnimatedItem(index = 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Уровень
                        Box(modifier = Modifier.weight(1f)) {
                            val shape = RoundedCornerShape(14.dp)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(shape)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.White.copy(alpha = 0.07f), Color.White.copy(alpha = 0.02f))
                                        )
                                    )
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), shape)
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(36.dp)) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            drawArc(Color.White.copy(alpha = 0.1f), -90f, 360f, false, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
                                            drawArc(levelColor, -90f, animatedXP * 360f, false, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
                                        }
                                        Text("${userLevel.level}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = levelColor)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(userLevel.title, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f), maxLines = 1)
                                }
                            }
                        }

                        // Серия
                        Box(modifier = Modifier.weight(1f)) {
                            val shape = RoundedCornerShape(14.dp)
                            val canRecover = remember { StreakManager.canRecoverStreak(context) }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(shape)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                if (canRecover) Color(0xFFFF9800).copy(alpha = 0.1f) else Color.White.copy(alpha = 0.07f),
                                                Color.White.copy(alpha = 0.02f)
                                            )
                                        )
                                    )
                                    .border(1.dp, if (canRecover) Color(0xFFFF9800).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f), shape)
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(if (canRecover) "⚠️" else "🔥", fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "$currentStreak",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (canRecover) Color(0xFFFF9800) else Color.White
                                    )
                                    Text(
                                        if (canRecover) "спаси!" else "дней",
                                        fontSize = 11.sp,
                                        color = if (canRecover) Color(0xFFFF9800).copy(alpha = 0.7f)
                                        else Color.White.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }

                        // Неделя
                        Box(modifier = Modifier.weight(1f)) {
                            val shape = RoundedCornerShape(14.dp)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(shape)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.White.copy(alpha = 0.07f), Color.White.copy(alpha = 0.02f))
                                        )
                                    )
                                    .border(1.dp, Color.White.copy(alpha = 0.08f), shape)
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(36.dp)) {
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            drawArc(Color.White.copy(alpha = 0.1f), -90f, 360f, false, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
                                            drawArc(Purple, -90f, animatedWeeklyProgress * 360f, false, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
                                        }
                                        Text("${(animatedWeeklyProgress * 100).toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Purple)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("$completedDays/$totalDays", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }
            }

            // ── Баннер восстановления серии ──
            item(key = "recovery") {
                AnimatedItem(index = 1) {
                    val missedDate = remember { StreakManager.getMissedTrainingDay(context) }
                    if (missedDate != null) {
                        val missedName = remember { StreakManager.getMissedWorkoutName(missedDate) }
                        val missedId = remember { StreakManager.getMissedWorkoutId(missedDate) }
                        val shape = RoundedCornerShape(14.dp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(shape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFFF9800).copy(alpha = 0.2f), Color(0xFFFF9800).copy(alpha = 0.05f))
                                    )
                                )
                                .border(1.dp, Color(0xFFFF9800).copy(alpha = 0.25f), shape)
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "⚠️ Серия под угрозой!",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF9800)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "Пройди $missedName чтобы восстановить",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFFF9800).copy(alpha = 0.3f))
                                        .border(1.dp, Color(0xFFFF9800).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                        .clickable { onStartWorkout(missedId) }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text("▶", fontSize = 16.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // ── Совет дня ──
            item(key = "advice") {
                AnimatedItem(index = 2) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Purple.copy(alpha = 0.08f))
                            .border(1.dp, Purple.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🧠", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(advice, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f), lineHeight = 18.sp)
                    }
                }
            }

            // ── Сегодняшняя тренировка ──
            item(key = "today") {
                AnimatedItem(index = 3) {
                    val bannerColor = if (isRestDay) Color(0xFF4CAF50) else Purple
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(bannerColor.copy(alpha = 0.35f), bannerColor.copy(alpha = 0.1f))
                                )
                            )
                            .border(1.dp, bannerColor.copy(alpha = 0.25f), RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    if (isRestDay) "😴 День отдыха" else "📋 Сегодня",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    if (isRestDay) "Восстановление" else todayWorkout.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                if (!isRestDay) {
                                    Text(
                                        "${todayWorkout.exercises.size} упр. · ~${todayWorkout.exercises.size * 5} мин",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.4f)
                                    )
                                }
                            }
                            if (!isRestDay) {
                                Spacer(modifier = Modifier.width(12.dp))
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(bannerColor.copy(alpha = 0.5f))
                                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                                        .clickable { onStartWorkout(todayWorkout.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("▶", fontSize = 20.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // ── Челленджи ──
            item(key = "challenges") {
                AnimatedItem(index = 4) {
                    val challenges = remember { ChallengeManager.getWeeklyChallenges(context) }
                    val shape = RoundedCornerShape(14.dp)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(shape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.02f))
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.08f), shape)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("⚔️ Челленджи", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(
                                "${challenges.count { it.isCompleted }}/${challenges.size}",
                                fontSize = 13.sp,
                                color = Purple
                            )
                        }
                        challenges.forEach { ch ->
                            val prog = if (ch.targetValue > 0) ch.currentValue.toFloat() / ch.targetValue else 0f
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(ch.icon, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            ch.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (ch.isCompleted) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.8f)
                                        )
                                        Text(
                                            if (ch.isCompleted) "✅" else "${ch.currentValue}/${ch.targetValue}",
                                            fontSize = 11.sp,
                                            color = if (ch.isCompleted) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.4f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(3.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(Color.White.copy(alpha = 0.08f))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(prog.coerceIn(0f, 1f))
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(if (ch.isCompleted) Color(0xFF4CAF50) else Purple)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Цитата ──
            item(key = "quote") {
                AnimatedItem(index = 5) {
                    Text(
                        "\"$todayQuote\"",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.35f),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // ── Быстрые действия ──
            item(key = "actions") {
                AnimatedItem(index = 6) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("⚖️", "Вес", onNavigateToBodyweight),
                            Triple("📈", "Графики", onNavigateToCharts),
                            Triple("🗓️", "Карта", onNavigateToHeatMap),
                            Triple("✏️", "Записи", onNavigateToEditLog),
                            Triple("⚙️", "Ещё", onNavigateToSettings)
                        ).forEach { (icon, label, action) ->
                            Box(modifier = Modifier.weight(1f)) {
                                val s = RoundedCornerShape(12.dp)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(s)
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .border(1.dp, Color.White.copy(alpha = 0.06f), s)
                                        .clickable { action() }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(icon, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Программы ──
            item(key = "programs_title") {
                AnimatedItem(index = 7) {
                    Text(
                        "Программы",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            val programs = ALL_WORKOUTS.values.filter { it.id != "rest" }.toList()
            itemsIndexed(programs, key = { _, p -> p.id }) { idx, program ->
                AnimatedItem(index = 8 + idx) {
                    val shape = RoundedCornerShape(14.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(shape)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.02f))
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.06f), shape)
                            .clickable { onViewWorkout(program.id) }
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val emoji = when {
                                program.name.contains("PUSH", true) -> "💪"
                                program.name.contains("PULL", true) -> "🏋️"
                                program.name.contains("SQUAD", true) -> "🦵"
                                program.name.contains("POWER", true) -> "⚡"
                                else -> "🏋️"
                            }
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Purple.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(program.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text(
                                    "${program.exercises.size} упр. · ${program.time}",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.35f)
                                )
                            }
                            Text("›", fontSize = 20.sp, color = Color.White.copy(alpha = 0.2f))
                        }
                    }
                }
            }

            item(key = "spacer") { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

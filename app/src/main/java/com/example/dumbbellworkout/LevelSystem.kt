package com.example.dumbbellworkout

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

// ─── Уровни и XP ───

data class UserLevel(
    val level: Int,
    val title: String,
    val currentXP: Int,
    val xpForNextLevel: Int,
    val totalXP: Int
)

object LevelManager {
    private const val PREFS = "level_prefs"
    private const val KEY_TOTAL_XP = "total_xp"

    private val levels = listOf(
        1 to "Новичок",
        2 to "Стажёр",
        3 to "Любитель",
        4 to "Спортсмен",
        5 to "Атлет",
        6 to "Продвинутый",
        7 to "Опытный",
        8 to "Мастер",
        9 to "Элита",
        10 to "Про",
        11 to "Ветеран",
        12 to "Титан",
        13 to "Чемпион",
        14 to "Герой",
        15 to "Легенда",
        16 to "Мифический",
        17 to "Бессмертный",
        18 to "Бог зала",
        19 to "Вселенский",
        20 to "GRIND Master"
    )

    private fun xpRequired(level: Int): Int = when {
        level <= 3 -> 100 * level
        level <= 7 -> 150 * level
        level <= 12 -> 200 * level
        level <= 17 -> 300 * level
        else -> 400 * level
    }

    fun getTotalXP(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY_TOTAL_XP, 0)

    fun addXP(context: Context, amount: Int) {
        val current = getTotalXP(context)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_TOTAL_XP, current + amount).apply()
    }

    fun getUserLevel(context: Context): UserLevel {
        val totalXP = getTotalXP(context)
        var accumulated = 0
        for (i in levels.indices) {
            val lvl = levels[i].first
            val title = levels[i].second
            val required = xpRequired(lvl)
            if (totalXP < accumulated + required || i == levels.lastIndex) {
                return UserLevel(
                    level = lvl,
                    title = title,
                    currentXP = totalXP - accumulated,
                    xpForNextLevel = required,
                    totalXP = totalXP
                )
            }
            accumulated += required
        }
        return UserLevel(20, "GRIND Master", 0, 1, totalXP)
    }

    // XP rewards
    fun xpForWorkout(): Int = 50
    fun xpForRecord(): Int = 30
    fun xpForStreak(days: Int): Int = when {
        days >= 30 -> 25
        days >= 14 -> 15
        days >= 7 -> 10
        days >= 3 -> 5
        else -> 0
    }
    fun xpForChallenge(): Int = 40
}

// ─── Streak (серия тренировок) ───

object StreakManager {
    private const val PREFS = "streak_prefs"
    private const val KEY_DATES = "workout_dates"
    private const val KEY_RECOVERY = "recovery_done"

    private fun dateFormat() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private fun dayIndex(dateStr: String): Int {
        val cal = Calendar.getInstance()
        cal.time = dateFormat().parse(dateStr) ?: return -1
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 0; Calendar.TUESDAY -> 1; Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3; Calendar.FRIDAY -> 4; Calendar.SATURDAY -> 5
            Calendar.SUNDAY -> 6; else -> 0
        }
    }

    private fun isTrainingDay(dateStr: String): Boolean {
        val idx = dayIndex(dateStr)
        val key = SCHEDULE[idx] ?: "rest"
        return key != "rest"
    }

    private fun getWorkoutIdForDate(dateStr: String): String {
        val idx = dayIndex(dateStr)
        return SCHEDULE[idx] ?: "rest"
    }

    fun getWorkoutDates(context: Context): Set<String> =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY_DATES, emptySet()) ?: emptySet()

    fun recordWorkout(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val today = dateFormat().format(Date())
        val dates = (prefs.getStringSet(KEY_DATES, emptySet()) ?: emptySet()).toMutableSet()
        dates.add(today)
        prefs.edit().putStringSet(KEY_DATES, dates).apply()
    }

    // Записать восстановление пропущенного дня
    fun recordRecovery(context: Context, missedDate: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val dates = (prefs.getStringSet(KEY_DATES, emptySet()) ?: emptySet()).toMutableSet()
        dates.add(missedDate)
        // Сохраняем что восстановление выполнено
        prefs.edit()
            .putStringSet(KEY_DATES, dates)
            .putString(KEY_RECOVERY, "")
            .apply()
    }

    // Найти пропущенный тренировочный день (ближайший назад от сегодня)
    fun getMissedTrainingDay(context: Context): String? {
        val df = dateFormat()
        val today = df.format(Date())
        val dates = getWorkoutDates(context)
        val cal = Calendar.getInstance()

        // Если сегодня тренировочный день и ещё не пройден — это не пропуск, день идёт
        // Начинаем проверку со вчера
        cal.add(Calendar.DAY_OF_MONTH, -1)

        // Ищем максимум 7 дней назад
        for (i in 0..6) {
            val dateStr = df.format(cal.time)
            if (isTrainingDay(dateStr) && dateStr !in dates) {
                return dateStr // нашли пропущенный день
            }
            if (isTrainingDay(dateStr) && dateStr in dates) {
                return null // предыдущий тренировочный день пройден — пропусков нет
            }
            cal.add(Calendar.DAY_OF_MONTH, -1)
        }
        return null
    }

    // Получить название тренировки для пропущенного дня
    fun getMissedWorkoutName(missedDate: String): String {
        val workoutId = getWorkoutIdForDate(missedDate)
        return ALL_WORKOUTS[workoutId]?.name ?: ""
    }

    // Получить ID тренировки для пропущенного дня
    fun getMissedWorkoutId(missedDate: String): String {
        return getWorkoutIdForDate(missedDate)
    }

    // Можно ли восстановить серию
    fun canRecoverStreak(context: Context): Boolean {
        return getMissedTrainingDay(context) != null
    }

    fun getCurrentStreak(context: Context): Int {
        val dates = getWorkoutDates(context)
        val df = dateFormat()
        val cal = Calendar.getInstance()
        var streak = 0

        while (true) {
            val dateStr = df.format(cal.time)

            if (isTrainingDay(dateStr)) {
                if (dateStr in dates) {
                    streak++
                } else {
                    val today = df.format(Date())
                    if (dateStr == today) {
                        cal.add(Calendar.DAY_OF_MONTH, -1)
                        continue
                    } else {
                        break
                    }
                }
            }

            cal.add(Calendar.DAY_OF_MONTH, -1)

            val daysBack = ((Date().time - cal.time.time) / (1000 * 60 * 60 * 24)).toInt()
            if (daysBack > 400 || streak > 365) break
        }

        return streak
    }

    fun getBestStreak(context: Context): Int {
        val dates = getWorkoutDates(context)
        if (dates.isEmpty()) return 0

        val df = dateFormat()
        val sortedDates = dates.sorted()
        val firstDate = df.parse(sortedDates.first()) ?: return 0
        val lastDate = df.parse(sortedDates.last()) ?: return 0

        val cal = Calendar.getInstance()
        cal.time = firstDate

        var bestStreak = 0
        var currentStreak = 0

        while (true) {
            val dateStr = df.format(cal.time)

            if (isTrainingDay(dateStr)) {
                if (dateStr in dates) {
                    currentStreak++
                    bestStreak = maxOf(bestStreak, currentStreak)
                } else {
                    currentStreak = 0
                }
            }

            cal.add(Calendar.DAY_OF_MONTH, 1)
            if (dateStr > sortedDates.last()) break
        }

        return maxOf(bestStreak, getCurrentStreak(context))
    }

    fun getLastDate(context: Context): String {
        return getWorkoutDates(context).maxOrNull() ?: ""
    }
}



// ─── Еженедельные челленджи ───

data class WeeklyChallenge(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val targetValue: Int,
    val currentValue: Int,
    val xpReward: Int,
    val isCompleted: Boolean
)

object ChallengeManager {
    private const val PREFS = "challenge_prefs"
    private const val KEY_WEEK = "current_week"
    private const val KEY_CHALLENGES = "active_challenges"

    private fun currentWeek(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}-W${cal.get(Calendar.WEEK_OF_YEAR)}"
    }

    private val challengePool = listOf(
        Triple("tonnage_5000", "Титан тоннажа" to "Набери 5 000 кг тоннажа за неделю", "🏋️" to 5000),
        Triple("tonnage_10000", "Железный человек" to "Набери 10 000 кг тоннажа за неделю", "💪" to 10000),
        Triple("workouts_3", "Три в одном" to "Проведи 3 тренировки за неделю", "🔥" to 3),
        Triple("workouts_4", "Четвёрка" to "Проведи 4 тренировки за неделю", "⚡" to 4),
        Triple("workouts_5", "Железная воля" to "Проведи 5 тренировок за неделю", "🦾" to 5),
        Triple("records_1", "Рекордсмен" to "Установи 1 личный рекорд", "🏆" to 1),
        Triple("records_3", "Разрушитель рекордов" to "Установи 3 личных рекорда", "💥" to 3),
        Triple("sets_50", "Подходный мастер" to "Выполни 50 подходов за неделю", "📊" to 50),
        Triple("sets_80", "Машина подходов" to "Выполни 80 подходов за неделю", "🤖" to 80),
        Triple("streak_3", "Тройной удар" to "Тренируйся 3 дня подряд", "🎯" to 3),
        Triple("streak_5", "Пятидневка" to "Тренируйся 5 дней подряд", "🔥" to 5),
        Triple("calories_500", "Жиросжигатель" to "Сожги 500 ккал за неделю", "🔥" to 500)
    )

    fun getWeeklyChallenges(context: Context): List<WeeklyChallenge> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val savedWeek = prefs.getString(KEY_WEEK, "") ?: ""
        val thisWeek = currentWeek()

        if (savedWeek != thisWeek) {
            // generate 3 new challenges for this week
            val selected = challengePool.shuffled().take(3)
            val ids = selected.map { it.first }.toSet()
            prefs.edit()
                .putString(KEY_WEEK, thisWeek)
                .putStringSet(KEY_CHALLENGES, ids)
                .apply()
            // clear completed flags
            ids.forEach { prefs.edit().putBoolean("completed_$it", false).apply() }
        }

        val activeIds = prefs.getStringSet(KEY_CHALLENGES, emptySet()) ?: emptySet()
        val stats = getWeeklyStats(context)

        return challengePool.filter { it.first in activeIds }.map { challenge ->
            val (id, titleDesc, iconTarget) = challenge
            val (title, description) = titleDesc
            val (icon, target) = iconTarget

            val currentValue = when {
                id.startsWith("tonnage") -> stats.tonnage.toInt()
                id.startsWith("workouts") -> stats.workouts
                id.startsWith("records") -> stats.records
                id.startsWith("sets") -> stats.sets
                id.startsWith("streak") -> StreakManager.getCurrentStreak(context)
                id.startsWith("calories") -> stats.calories.toInt()
                else -> 0
            }
            val completed = prefs.getBoolean("completed_$id", false) || currentValue >= target

            if (currentValue >= target && !prefs.getBoolean("completed_$id", false)) {
                prefs.edit().putBoolean("completed_$id", true).apply()
                LevelManager.addXP(context, LevelManager.xpForChallenge())
            }

            WeeklyChallenge(id, title, description, icon, target, minOf(currentValue, target), 40, completed)
        }
    }

    data class WeeklyStats(
        val tonnage: Float,
        val workouts: Int,
        val records: Int,
        val sets: Int,
        val calories: Float
    )

    private fun getWeeklyStats(context: Context): WeeklyStats {
        val allLogs = WorkoutLog.loadAllLogs(context)
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Находим начало текущей недели (понедельник)
        val cal = Calendar.getInstance()
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DAY_OF_MONTH, -1)
        }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val weekStart = cal.time

        var tonnage = 0f
        var sets = 0
        val workoutDays = mutableSetOf<String>()

        for ((dateStr, log) in allLogs) {
            if (log.sets.isEmpty()) continue
            val date = try { df.parse(dateStr) } catch (_: Exception) { null } ?: continue
            if (date.before(weekStart)) continue

            workoutDays.add(dateStr)
            for (s in log.sets) {
                tonnage += s.weight * s.reps
                sets++
            }
        }

        val recordPrefs = context.getSharedPreferences("records_prefs", Context.MODE_PRIVATE)
        val records = recordPrefs.getInt("weekly_records", 0)
        val durationMinutes = sets * 1.5f
        val calories = tonnage * 0.05f + durationMinutes * 5.5f

        return WeeklyStats(tonnage, workoutDays.size, records, sets, calories)
    }
}

// ─── AI совет дня ───

object SmartAdvice {
    fun getAdvice(context: Context): String {
        val streakDays = StreakManager.getCurrentStreak(context)
        val level = LevelManager.getUserLevel(context)
        val lastDate = StreakManager.getLastDate(context)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayWorkout = getTodayWorkout()
        val isRestDay = todayWorkout.id == "rest"

        val cal = Calendar.getInstance()
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)

        // build advice list based on context
        val advices = mutableListOf<String>()

        // streak-based
        if (streakDays == 0) {
            advices.add("💡 Начни серию тренировок сегодня! Первый шаг — самый важный.")
            advices.add("💡 Ты давно не тренировался. Даже 20 минут — это прогресс!")
        }
        if (streakDays in 1..2) {
            advices.add("🔥 Серия $streakDays дня! Не останавливайся — привычка формируется за 21 день.")
        }
        if (streakDays in 3..6) {
            advices.add("🔥 $streakDays дней подряд! Отличный темп, держи ритм!")
            advices.add("💪 Твоё тело адаптируется. Попробуй увеличить вес на 2.5 кг в основных упражнениях.")
        }
        if (streakDays >= 7) {
            advices.add("🏆 $streakDays дней подряд! Ты на пути к великим результатам!")
            advices.add("⚡ Невероятная серия! Убедись, что достаточно спишь и пьёшь воду.")
        }

        // level-based
        if (level.level <= 3) {
            advices.add("📈 Фокусируйся на технике, а не на весах. Правильная форма = безопасный прогресс.")
            advices.add("🎯 Начинающим важно тренироваться регулярно. 3 раза в неделю — отличный старт.")
        }
        if (level.level in 4..8) {
            advices.add("💪 Ты уже ${level.title}! Пора увеличить интенсивность — добавь повторение или вес.")
            advices.add("📊 Следи за прогрессом в графиках. Рост силы = рост мышц.")
        }
        if (level.level >= 9) {
            advices.add("🦾 Уровень ${level.title}! Попробуй суперсеты или дроп-сеты для нового стимула.")
            advices.add("🧠 На твоём уровне восстановление важнее объёма. Качество > количество.")
        }

        // workout-day based
        if (!isRestDay) {
            advices.add("🏋️ Сегодня ${todayWorkout.name}. Разомнись 5 минут перед началом!")
            advices.add("⏱ Держи паузы отдыха 60-90 секунд для гипертрофии, 2-3 минуты для силы.")
        }
        if (isRestDay) {
            advices.add("😴 День отдыха. Мышцы растут во сне! Ложись пораньше.")
            advices.add("🧘 В день отдыха сделай лёгкую растяжку или прогулку 20 минут.")
            advices.add("🥩 День отдыха — не забывай про белок: 1.6-2.2 г на кг массы тела.")
        }

        // day-specific
        if (dayOfWeek == Calendar.MONDAY && !isRestDay) {
            advices.add("💥 Понедельник — лучший день для мощного старта недели!")
        }
        if (dayOfWeek == Calendar.FRIDAY) {
            advices.add("🎉 Пятница! Закончи неделю сильной тренировкой и заслужи выходные!")
        }

        // nutrition tips
        advices.add("🥛 Не забывай пить воду — 30 мл на каждый кг веса тела в день.")
        advices.add("🍳 Съешь белок и углеводы в течение 2 часов после тренировки для лучшего восстановления.")
        advices.add("🥑 Жиры — не враг. Они нужны для гормонов и суставов. 0.8-1 г на кг тела.")

        // use day of year as seed for consistent daily advice
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        return advices[dayOfYear % advices.size]
    }
}

// ─── UI-компоненты ───

@Composable
fun LevelCard(context: Context) {
    val userLevel = remember { LevelManager.getUserLevel(context) }
    val progress = userLevel.currentXP.toFloat() / userLevel.xpForNextLevel.toFloat()

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "xp_progress"
    )

    val levelColor = when {
        userLevel.level <= 3 -> Color(0xFF4CAF50)
        userLevel.level <= 7 -> Color(0xFF2196F3)
        userLevel.level <= 12 -> Purple
        userLevel.level <= 17 -> Color(0xFFFF9800)
        else -> Color(0xFFFF5722)
    }

    GlassCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // level circle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(56.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        // bg ring
                        drawArc(
                            color = Color.White.copy(alpha = 0.1f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                        // progress ring
                        drawArc(
                            color = levelColor,
                            startAngle = -90f,
                            sweepAngle = animatedProgress * 360f,
                            useCenter = false,
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        "${userLevel.level}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = levelColor
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        userLevel.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // XP bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White.copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedProgress)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(levelColor, levelColor.copy(alpha = 0.6f))
                                    )
                                )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${userLevel.currentXP} / ${userLevel.xpForNextLevel} XP",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun StreakCard(context: Context) {
    val currentStreak = remember { StreakManager.getCurrentStreak(context) }
    val bestStreak = remember { StreakManager.getBestStreak(context) }

    val infiniteTransition = rememberInfiniteTransition(label = "fire")
    val fireScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (currentStreak >= 3) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            tween(600, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "fire_scale"
    )

    val streakColor = when {
        currentStreak >= 14 -> Color(0xFFFF5722)
        currentStreak >= 7 -> Color(0xFFFF9800)
        currentStreak >= 3 -> Color(0xFFFFC107)
        else -> Color.White.copy(alpha = 0.5f)
    }

    GlassCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "🔥",
                fontSize = 32.sp,
                modifier = Modifier.scale(fireScale)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Серия: $currentStreak ${getDaysWord(currentStreak)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = streakColor
                )
                Text(
                    "Лучшая серия: $bestStreak ${getDaysWord(bestStreak)}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.4f)
                )
            }
            if (currentStreak >= 7) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(streakColor.copy(alpha = 0.3f), streakColor.copy(alpha = 0.1f))
                            )
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        if (currentStreak >= 14) "🔥🔥🔥" else "🔥🔥",
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

private fun getDaysWord(n: Int): String {
    val mod10 = n % 10
    val mod100 = n % 100
    return when {
        mod100 in 11..14 -> "дней"
        mod10 == 1 -> "день"
        mod10 in 2..4 -> "дня"
        else -> "дней"
    }
}

@Composable
fun ChallengesCard(context: Context) {
    val challenges = remember { ChallengeManager.getWeeklyChallenges(context) }

    GlassCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "⚔️ Челленджи недели",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "${challenges.count { it.isCompleted }}/${challenges.size}",
                    fontSize = 14.sp,
                    color = Purple
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            challenges.forEach { challenge ->
                ChallengeItem(challenge)
                if (challenge != challenges.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ChallengeItem(challenge: WeeklyChallenge) {
    val progress = if (challenge.targetValue > 0)
        challenge.currentValue.toFloat() / challenge.targetValue.toFloat()
    else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "challenge_progress"
    )

    val progressColor = if (challenge.isCompleted) Color(0xFF4CAF50) else Purple

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(challenge.icon, fontSize = 24.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    challenge.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (challenge.isCompleted) Color(0xFF4CAF50) else Color.White
                )
                Text(
                    if (challenge.isCompleted) "✅" else "${challenge.currentValue}/${challenge.targetValue}",
                    fontSize = 12.sp,
                    color = if (challenge.isCompleted) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .clip(RoundedCornerShape(2.dp))
                        .background(progressColor)
                )
            }
        }
    }
}

@Composable
fun AdviceCard(context: Context) {
    val advice = remember { SmartAdvice.getAdvice(context) }

    GlassCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text("🧠", fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    "Совет дня",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Purple
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    advice,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

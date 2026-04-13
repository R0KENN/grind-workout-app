package com.example.dumbbellworkout

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val requirement: Int,
    val type: String
)

object AchievementsManager {
    private const val PREFS = "achievements_prefs"
    private const val KEY_UNLOCKED = "unlocked_achievements"
    private val gson = Gson()

    val ALL_ACHIEVEMENTS = listOf(
        Achievement("first_workout", "Первый шаг", "Завершите первую тренировку", "🏋️", 1, "workouts"),
        Achievement("workouts_5", "Новичок", "Завершите 5 тренировок", "💪", 5, "workouts"),
        Achievement("workouts_10", "Боец", "Завершите 10 тренировок", "🥊", 10, "workouts"),
        Achievement("workouts_25", "Ветеран", "Завершите 25 тренировок", "🎖️", 25, "workouts"),
        Achievement("workouts_50", "Легенда", "Завершите 50 тренировок", "🏆", 50, "workouts"),
        Achievement("workouts_100", "Машина", "Завершите 100 тренировок", "🤖", 100, "workouts"),

        Achievement("streak_2", "Стабильность", "Тренируйтесь 2 недели подряд", "🔥", 2, "streak"),
        Achievement("streak_4", "Привычка", "Тренируйтесь 4 недели подряд", "⚡", 4, "streak"),
        Achievement("streak_8", "Несокрушимый", "Тренируйтесь 8 недель подряд", "🛡️", 8, "streak"),
        Achievement("streak_12", "Железная воля", "Тренируйтесь 12 недель подряд", "🔱", 12, "streak"),

        Achievement("tonnage_1000", "Первая тонна", "Наберите 1 000 кг тоннажа за тренировку", "📦", 1000, "tonnage"),
        Achievement("tonnage_2500", "Тяжеловоз", "Наберите 2 500 кг тоннажа за тренировку", "🚛", 2500, "tonnage"),
        Achievement("tonnage_5000", "Монстр", "Наберите 5 000 кг тоннажа за тренировку", "👹", 5000, "tonnage"),

        Achievement("record_1", "Первый рекорд", "Установите первый персональный рекорд", "🥇", 1, "records"),
        Achievement("record_5", "Рекордсмен", "Установите 5 рекордов", "🌟", 5, "records"),
        Achievement("record_10", "Покоритель вершин", "Установите 10 рекордов", "⛰️", 10, "records"),
        Achievement("record_25", "Неудержимый", "Установите 25 рекордов", "🚀", 25, "records"),

        Achievement("sets_100", "Сотня", "Выполните 100 подходов всего", "💯", 100, "total_sets"),
        Achievement("sets_500", "Полтысячи", "Выполните 500 подходов всего", "🎯", 500, "total_sets"),
        Achievement("sets_1000", "Тысячник", "Выполните 1000 подходов всего", "👑", 1000, "total_sets"),

        Achievement("bodyweight_7", "Следящий за собой", "Запишите вес тела 7 раз", "📊", 7, "bodyweight"),
        Achievement("bodyweight_30", "Дисциплина", "Запишите вес тела 30 раз", "📈", 30, "bodyweight"),

        Achievement("early_bird", "Ранняя пташка", "Первая тренировка завершена!", "🐣", 1, "workouts"),
    )

    fun getUnlocked(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_UNLOCKED, null) ?: return emptySet()
        val type = object : TypeToken<Set<String>>() {}.type
        return try { gson.fromJson(json, type) } catch (e: Exception) { emptySet() }
    }

    private fun saveUnlocked(context: Context, unlocked: Set<String>) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_UNLOCKED, gson.toJson(unlocked)).apply()
    }

    fun checkAndUnlock(context: Context): List<Achievement> {
        val unlocked = getUnlocked(context).toMutableSet()
        val newlyUnlocked = mutableListOf<Achievement>()

        val allLogs = WorkoutLog.loadAllLogs(context)
        val totalWorkouts = allLogs.count { it.value.sets.isNotEmpty() }
        val (streak, _, _) = WorkoutLog.calculateStreak(context)
        val todayTonnage = WorkoutLog.getTodayTonnage(context)
        val bodyweightEntries = WorkoutLog.loadBodyweight(context).size
        val totalSets = allLogs.values.sumOf { it.sets.size }

        // Count records
        val allExercises = ALL_WORKOUTS.values.flatMap { it.exercises }.distinctBy { it.name }
        var recordCount = 0
        for (exercise in allExercises) {
            val maxWeight = WorkoutLog.getMaxWeight(context, exercise.name)
            if (maxWeight > 0) recordCount++
        }

        for (achievement in ALL_ACHIEVEMENTS) {
            if (achievement.id in unlocked) continue

            val currentValue = when (achievement.type) {
                "workouts" -> totalWorkouts
                "streak" -> streak
                "tonnage" -> todayTonnage.toInt()
                "records" -> recordCount
                "total_sets" -> totalSets
                "bodyweight" -> bodyweightEntries
                else -> 0
            }

            if (currentValue >= achievement.requirement) {
                unlocked.add(achievement.id)
                newlyUnlocked.add(achievement)
            }
        }

        if (newlyUnlocked.isNotEmpty()) {
            saveUnlocked(context, unlocked)
        }

        return newlyUnlocked
    }

    fun getProgress(context: Context): Map<String, Pair<Int, Int>> {
        val allLogs = WorkoutLog.loadAllLogs(context)
        val totalWorkouts = allLogs.count { it.value.sets.isNotEmpty() }
        val (streak, _, _) = WorkoutLog.calculateStreak(context)
        val todayTonnage = WorkoutLog.getTodayTonnage(context)
        val bodyweightEntries = WorkoutLog.loadBodyweight(context).size
        val totalSets = allLogs.values.sumOf { it.sets.size }
        val allExercises = ALL_WORKOUTS.values.flatMap { it.exercises }.distinctBy { it.name }
        var recordCount = 0
        for (exercise in allExercises) {
            if (WorkoutLog.getMaxWeight(context, exercise.name) > 0) recordCount++
        }

        val progress = mutableMapOf<String, Pair<Int, Int>>()
        for (achievement in ALL_ACHIEVEMENTS) {
            val current = when (achievement.type) {
                "workouts" -> totalWorkouts
                "streak" -> streak
                "tonnage" -> todayTonnage.toInt()
                "records" -> recordCount
                "total_sets" -> totalSets
                "bodyweight" -> bodyweightEntries
                else -> 0
            }
            progress[achievement.id] = Pair(current, achievement.requirement)
        }
        return progress
    }
}

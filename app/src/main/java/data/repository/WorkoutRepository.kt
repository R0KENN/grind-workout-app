package com.example.dumbbellworkout.data.repository

import android.content.Context
import com.example.dumbbellworkout.*
import com.example.dumbbellworkout.data.db.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class WorkoutRepository(context: Context) {

    private val dao = AppDatabase.getInstance(context).workoutDao()
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private fun today(): String = sdf.format(Date())

    // ══════════ Миграция из SharedPreferences ══════════

    suspend fun migrateFromSharedPrefs(context: Context) {
        // Проверяем, была ли уже миграция
        val prefs = context.getSharedPreferences("migration", Context.MODE_PRIVATE)
        if (prefs.getBoolean("done", false)) return

        // Мигрируем подходы
        val oldLogs = WorkoutLog.loadAllLogs(context)
        for ((date, dayLog) in oldLogs) {
            for (set in dayLog.sets) {
                dao.insertSet(
                    WorkoutSetEntity(
                        date = date,
                        exerciseName = set.exerciseName,
                        setNumber = set.setNumber,
                        weight = set.weight,
                        reps = set.reps,
                        type = set.type
                    )
                )
            }
        }

        // Мигрируем вес тела
        val oldBw = WorkoutLog.loadBodyweight(context)
        for ((date, weight) in oldBw) {
            dao.insertBodyweight(BodyweightEntity(date = date, weight = weight))
        }

        prefs.edit().putBoolean("done", true).apply()
    }

    // ══════════ Подходы ══════════

    suspend fun addSet(exerciseName: String, setNumber: Int, weight: Float, reps: Int) {
        dao.insertSet(
            WorkoutSetEntity(
                date = today(),
                exerciseName = exerciseName,
                setNumber = setNumber,
                weight = weight,
                reps = reps
            )
        )
    }

    fun getAllSetsFlow(): Flow<List<WorkoutSetEntity>> = dao.getAllSetsFlow()

    suspend fun getAllSets(): List<WorkoutSetEntity> = dao.getAllSets()

    suspend fun getSetsByDate(date: String): List<WorkoutSetEntity> = dao.getSetsByDate(date)

    suspend fun getMaxWeight(exerciseName: String): Float = dao.getMaxWeight(exerciseName) ?: 0f

    suspend fun getLastWeight(exerciseName: String): Float? = dao.getLastWeight(exerciseName)

    suspend fun getExerciseHistory(exerciseName: String): List<WorkoutSetEntity> =
        dao.getExerciseHistory(exerciseName, today())

    suspend fun getTonnageForDate(date: String): Float = dao.getTonnageForDate(date) ?: 0f

    suspend fun getTodayTonnage(): Float = getTonnageForDate(today())

    suspend fun updateSet(id: Long, newWeight: Float, newReps: Int) =
        dao.updateSet(id, newWeight, newReps)

    suspend fun deleteSet(id: Long) = dao.deleteSet(id)

    // ══════════ Вес тела ══════════

    suspend fun saveBodyweight(weight: Float) {
        dao.insertBodyweight(BodyweightEntity(date = today(), weight = weight))
    }

    fun getBodyweightFlow(): Flow<List<BodyweightEntity>> = dao.getBodyweightFlow()

    suspend fun getAllBodyweight(): List<BodyweightEntity> = dao.getAllBodyweight()

    // ══════════ Статистика ══════════

    suspend fun getAllDates(): List<String> = dao.getAllDates()

    suspend fun getTotalWorkoutDays(): Int = dao.getTotalWorkoutDays()

    suspend fun getVolumePerExercise(): List<ExerciseVolume> = dao.getVolumePerExercise()

    suspend fun getAllExerciseNames(): List<String> = dao.getAllExerciseNames()

    suspend fun getFilteredDates(exercise: String? = null): List<String> =
        dao.getFilteredDates(exercise)

    // ══════════ 1RM ══════════

    fun calculate1RM(weight: Float, reps: Int): Float {
        if (reps <= 0 || weight <= 0) return 0f
        if (reps == 1) return weight
        return weight * (1f + reps / 30f)
    }

    suspend fun getBest1RM(exerciseName: String): Float {
        val sets = dao.getSetsForExercise(exerciseName)
        return sets.maxOfOrNull { calculate1RM(it.weight, it.reps) } ?: 0f
    }

    // ══════════ Мышечные группы ══════════

    suspend fun getMuscleDistribution(): Map<String, Float> {
        val volumes = dao.getVolumePerExercise()
        val allExercises = ALL_WORKOUTS.values.flatMap { it.exercises }
        val result = mutableMapOf<String, Float>()

        for (vol in volumes) {
            val exercise = allExercises.firstOrNull { it.name == vol.exerciseName }
            if (exercise != null) {
                val group = simplifyMuscleGroup(exercise.target)
                result[group] = (result[group] ?: 0f) + vol.totalVolume
            }
        }
        return result
    }

    suspend fun getWeeklyMuscleVolume(): Map<String, Map<String, Float>> {
        val dailyVolumes = dao.getDailyVolumePerExercise()
        val allExercises = ALL_WORKOUTS.values.flatMap { it.exercises }
        val cal = Calendar.getInstance()

        // week key -> muscle -> volume
        val weeklyData = mutableMapOf<String, MutableMap<String, Float>>()

        for (dv in dailyVolumes) {
            val date = try { sdf.parse(dv.date) } catch (_: Exception) { continue } ?: continue
            cal.time = date
            val weekKey = "${cal.get(Calendar.YEAR)}-W${cal.get(Calendar.WEEK_OF_YEAR)}"

            val exercise = allExercises.firstOrNull { it.name == dv.exerciseName }
            if (exercise != null) {
                val group = simplifyMuscleGroup(exercise.target)
                val weekMap = weeklyData.getOrPut(weekKey) { mutableMapOf() }
                weekMap[group] = (weekMap[group] ?: 0f) + dv.totalVolume
            }
        }
        return weeklyData
    }

    // ══════════ Прогрессивная перегрузка ══════════

    suspend fun getProgressiveOverloadSuggestion(exerciseName: String, targetReps: Int): OverloadSuggestion? {
        val recentSets = dao.getRecentSetsForExercise(exerciseName, 50)
        if (recentSets.isEmpty()) return null

        // Группируем по дате (последние 2 тренировки)
        val byDate = recentSets.groupBy { it.date }
        val lastTwoDates = byDate.keys.sortedDescending().take(2)

        if (lastTwoDates.size < 2) return null

        val session1 = byDate[lastTwoDates[0]] ?: return null
        val session2 = byDate[lastTwoDates[1]] ?: return null

        // Проверяем: выполнены ли все подходы на максимуме диапазона
        val allSetsMaxReps1 = session1.all { it.reps >= targetReps }
        val allSetsMaxReps2 = session2.all { it.reps >= targetReps }

        val currentWeight = session1.maxOfOrNull { it.weight } ?: return null

        return if (allSetsMaxReps1 && allSetsMaxReps2) {
            // Рекомендуем увеличить вес на 2.5 кг
            val suggestedWeight = currentWeight + 2.5f
            OverloadSuggestion(
                exerciseName = exerciseName,
                currentWeight = currentWeight,
                suggestedWeight = suggestedWeight,
                reason = "Все подходы на макс. повторениях 2 тренировки подряд"
            )
        } else {
            null
        }
    }

    // ══════════ Для тепловой карты ══════════

    suspend fun getWorkoutIntensityMap(): Map<String, Int> {
        val allSets = dao.getAllSets()
        val dateSetCount = allSets.groupBy { it.date }.mapValues { it.value.size }
        if (dateSetCount.isEmpty()) return emptyMap()
        val maxSets = dateSetCount.values.max()
        return dateSetCount.mapValues { (_, sets) ->
            when {
                sets <= 0 -> 0
                sets <= maxSets * 0.25 -> 1
                sets <= maxSets * 0.5 -> 2
                sets <= maxSets * 0.75 -> 3
                else -> 4
            }
        }
    }

    // ══════════ Для завершённых дней недели ══════════

    suspend fun getCompletedDaysThisWeek(): Int {
        val cal = Calendar.getInstance()
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DAY_OF_MONTH, -1)
        }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val startDate = sdf.format(cal.time)
        val endDate = sdf.format(Date())
        return dao.getDatesBetween(startDate, endDate).size
    }

    private fun simplifyMuscleGroup(target: String): String {
        val t = target.lowercase()
        return when {
            "груд" in t -> "Грудь"
            "спин" in t || "широч" in t || "ромб" in t -> "Спина"
            "плеч" in t || "дельт" in t -> "Плечи"
            "бицепс" in t || "бицеп" in t -> "Бицепс"
            "трицепс" in t || "трицеп" in t -> "Трицепс"
            "ног" in t || "бёдр" in t || "бедр" in t || "ягод" in t -> "Ноги"
            "икр" in t -> "Икры"
            "прес" in t || "кор" in t -> "Кор"
            "предплеч" in t || "хват" in t -> "Предплечья"
            else -> "Другое"
        }
    }
}

data class OverloadSuggestion(
    val exerciseName: String,
    val currentWeight: Float,
    val suggestedWeight: Float,
    val reason: String
)

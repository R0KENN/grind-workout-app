package com.example.dumbbellworkout

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class LoggedSet(
    val exerciseName: String,
    val setNumber: Int,
    val weight: Float,
    val reps: Int,
    val type: String = "normal",
    val time: String = ""
)

data class DayLog(
    val date: String,
    val sets: MutableList<LoggedSet> = mutableListOf()
)

object WorkoutLog {
    private const val PREFS_NAME = "workout_log"
    private const val KEY_LOG = "log_data"
    private const val KEY_BODYWEIGHT = "bodyweight_data"
    private val gson = Gson()
    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private fun today(): String = sdf.format(Date())

    fun loadAllLogs(context: Context): Map<String, DayLog> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_LOG, null) ?: return emptyMap()
        val type = object : TypeToken<Map<String, DayLog>>() {}.type
        return try { gson.fromJson(json, type) } catch (e: Exception) { emptyMap() }
    }

    private fun saveAllLogs(context: Context, logs: Map<String, DayLog>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LOG, gson.toJson(logs)).apply()
    }

    fun addSet(context: Context, loggedSet: LoggedSet) {
        val allLogs = loadAllLogs(context).toMutableMap()
        val todayStr = today()
        val dayLog = allLogs[todayStr] ?: DayLog(date = todayStr)
        dayLog.sets.add(loggedSet)
        allLogs[todayStr] = dayLog
        saveAllLogs(context, allLogs)
    }

    fun getLastWeight(context: Context, exerciseName: String): Float? {
        val allLogs = loadAllLogs(context)
        for (date in allLogs.keys.sortedDescending()) {
            val log = allLogs[date] ?: continue
            val lastSet = log.sets.lastOrNull { it.exerciseName == exerciseName }
            if (lastSet != null) return lastSet.weight
        }
        return null
    }

    fun getMaxWeight(context: Context, exerciseName: String): Float {
        val allLogs = loadAllLogs(context)
        var max = 0f
        for ((_, log) in allLogs) {
            for (s in log.sets) {
                if (s.exerciseName == exerciseName && s.weight > max) max = s.weight
            }
        }
        return max
    }

    fun calculateTonnage(context: Context, date: String): Float {
        val allLogs = loadAllLogs(context)
        val log = allLogs[date] ?: return 0f
        return log.sets.sumOf { (it.weight * it.reps).toDouble() }.toFloat()
    }

    fun getTodayTonnage(context: Context): Float = calculateTonnage(context, today())

    fun calculateStreak(context: Context): Triple<Int, Int, Int> {
        val allLogs = loadAllLogs(context)
        val dates = allLogs.filter { it.value.sets.isNotEmpty() }.keys.sorted()
        if (dates.isEmpty()) return Triple(0, 0, 0)
        val totalWorkouts = dates.size
        val cal = Calendar.getInstance()
        val weeksWithTraining = mutableSetOf<Pair<Int, Int>>()
        for (d in dates) {
            val date = sdf.parse(d) ?: continue
            cal.time = date
            weeksWithTraining.add(Pair(cal.get(Calendar.YEAR), cal.get(Calendar.WEEK_OF_YEAR)))
        }
        cal.time = Date()
        var streak = 0
        while (true) {
            val pair = Pair(cal.get(Calendar.YEAR), cal.get(Calendar.WEEK_OF_YEAR))
            if (pair in weeksWithTraining) {
                streak++
                cal.add(Calendar.WEEK_OF_YEAR, -1)
            } else break
        }
        return Triple(streak, streak, totalWorkouts)
    }

    fun saveBodyweight(context: Context, weight: Float) {
        val data = loadBodyweight(context).toMutableMap()
        data[today()] = weight
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_BODYWEIGHT, gson.toJson(data)).apply()
    }

    fun loadBodyweight(context: Context): Map<String, Float> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_BODYWEIGHT, null) ?: return emptyMap()
        val type = object : TypeToken<Map<String, Float>>() {}.type
        return try { gson.fromJson(json, type) } catch (e: Exception) { emptyMap() }
    }

    fun updateSet(context: Context, date: String, oldSet: LoggedSet, newWeight: Float, newReps: Int) {
        val allLogs = loadAllLogs(context).toMutableMap()
        val dayLog = allLogs[date] ?: return
        val index = dayLog.sets.indexOfFirst {
            it.exerciseName == oldSet.exerciseName &&
                    it.setNumber == oldSet.setNumber &&
                    it.weight == oldSet.weight &&
                    it.reps == oldSet.reps
        }
        if (index >= 0) {
            dayLog.sets[index] = oldSet.copy(weight = newWeight, reps = newReps)
            allLogs[date] = dayLog
            saveAllLogs(context, allLogs)
        }
    }

    fun deleteSet(context: Context, date: String, setToDelete: LoggedSet) {
        val allLogs = loadAllLogs(context).toMutableMap()
        val dayLog = allLogs[date] ?: return
        val index = dayLog.sets.indexOfFirst {
            it.exerciseName == setToDelete.exerciseName &&
                    it.setNumber == setToDelete.setNumber &&
                    it.weight == setToDelete.weight &&
                    it.reps == setToDelete.reps
        }
        if (index >= 0) {
            dayLog.sets.removeAt(index)
            if (dayLog.sets.isEmpty()) {
                allLogs.remove(date)
            } else {
                allLogs[date] = dayLog
            }
            saveAllLogs(context, allLogs)
        }
    }
    // Get last workout history for a specific exercise (for in-workout display)
    fun getExerciseHistory(context: Context, exerciseName: String): List<LoggedSet> {
        val allLogs = loadAllLogs(context)
        val today = today()
        for (date in allLogs.keys.sortedDescending()) {
            if (date == today) continue // skip today's ongoing workout
            val log = allLogs[date] ?: continue
            val sets = log.sets.filter { it.exerciseName == exerciseName }
            if (sets.isNotEmpty()) return sets
        }
        return emptyList()
    }

    // Calculate estimated 1RM using Epley formula: weight × (1 + reps/30)
    fun calculate1RM(weight: Float, reps: Int): Float {
        if (reps <= 0 || weight <= 0) return 0f
        if (reps == 1) return weight
        return weight * (1f + reps / 30f)
    }

    // Get best estimated 1RM for an exercise across all logs
    fun getBest1RM(context: Context, exerciseName: String): Float {
        val allLogs = loadAllLogs(context)
        var best1RM = 0f
        for ((_, log) in allLogs) {
            for (s in log.sets) {
                if (s.exerciseName == exerciseName) {
                    val est = calculate1RM(s.weight, s.reps)
                    if (est > best1RM) best1RM = est
                }
            }
        }
        return best1RM
    }

    // Get muscle group distribution from all logs
    fun getMuscleDistribution(context: Context): Map<String, Float> {
        val allLogs = loadAllLogs(context)
        val muscleVolume = mutableMapOf<String, Float>()
        val allExercises = ALL_WORKOUTS.values.flatMap { it.exercises }

        for ((_, log) in allLogs) {
            for (s in log.sets) {
                val exercise = allExercises.firstOrNull { it.name == s.exerciseName }
                if (exercise != null) {
                    val group = simplifyMuscleGroup(exercise.target)
                    val volume = s.weight * s.reps
                    muscleVolume[group] = (muscleVolume[group] ?: 0f) + volume
                }
            }
        }
        return muscleVolume
    }

    private fun simplifyMuscleGroup(target: String): String {
        val t = target.lowercase()
        return when {
            t.contains("грудь") -> "Грудь"
            t.contains("спина") || t.contains("широчайш") || t.contains("ромбовидн") -> "Спина"
            t.contains("плеч") || t.contains("дельт") -> "Плечи"
            t.contains("бицепс") || t.contains("брахиалис") -> "Бицепс"
            t.contains("трицепс") -> "Трицепс"
            t.contains("квадрицепс") || t.contains("ягодиц") || t.contains("приводящ") -> "Ноги"
            t.contains("бедр") || t.contains("задняя цепь") -> "Ноги"
            t.contains("икр") || t.contains("камбаловидн") -> "Икры"
            t.contains("пресс") || t.contains("кор") -> "Кор"
            t.contains("предплечь") || t.contains("запясть") || t.contains("сгибател") || t.contains("разгибател") -> "Предплечья"
            t.contains("трапец") -> "Спина"
            else -> "Другое"
        }
    }
}

package com.example.dumbbellworkout.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dumbbellworkout.*
import com.example.dumbbellworkout.data.db.WorkoutSetEntity
import com.example.dumbbellworkout.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class StatsUiState(
    val streakWeeks: Int = 0,
    val totalWorkouts: Int = 0,
    val todayTonnage: Float = 0f,
    val exerciseRecords: List<Pair<String, Float>> = emptyList(),
    val recentWorkouts: List<Pair<String, Pair<Int, Float>>> = emptyList(), // date -> (sets, tonnage)
    val exercise1RMs: List<Triple<String, Float, Float>> = emptyList(), // name, est1rm, maxWeight
    val muscleDistribution: Map<String, Float> = emptyMap(),
    val weeklyMuscleVolume: Map<String, Map<String, Float>> = emptyMap(),
    val isLoading: Boolean = true
)

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val repo = WorkoutRepository(context)

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            val allSets = repo.getAllSets()
            val allDates = repo.getAllDates()
            val totalWorkouts = repo.getTotalWorkoutDays()
            val todayTonnage = repo.getTodayTonnage()
            val muscleDist = repo.getMuscleDistribution()
            val weeklyMuscle = repo.getWeeklyMuscleVolume()

            // Рекорды
            val allExercises = ALL_WORKOUTS.values.flatMap { it.exercises }.distinctBy { it.name }
            val records = allExercises.mapNotNull { ex ->
                val max = repo.getMaxWeight(ex.name)
                if (max > 0f) Pair(ex.name, max) else null
            }

            // 1RM
            val oneRMs = allExercises.mapNotNull { ex ->
                val est = repo.getBest1RM(ex.name)
                val max = repo.getMaxWeight(ex.name)
                if (est > 0f) Triple(ex.name, est, max) else null
            }.sortedByDescending { it.second }

            // Последние тренировки
            val recentDates = allDates.take(7)
            val recentWorkouts = recentDates.map { date ->
                val sets = repo.getSetsByDate(date)
                val tonnage = sets.sumOf { (it.weight * it.reps).toDouble() }.toFloat()
                Pair(date, Pair(sets.size, tonnage))
            }

            // Стрик недель (упрощённо)
            val streak = WorkoutLog.calculateStreak(context).first

            _uiState.value = StatsUiState(
                streakWeeks = streak,
                totalWorkouts = totalWorkouts,
                todayTonnage = todayTonnage,
                exerciseRecords = records,
                recentWorkouts = recentWorkouts,
                exercise1RMs = oneRMs,
                muscleDistribution = muscleDist,
                weeklyMuscleVolume = weeklyMuscle,
                isLoading = false
            )
        }
    }
}

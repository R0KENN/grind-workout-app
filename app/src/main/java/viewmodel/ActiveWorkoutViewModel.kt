package com.example.dumbbellworkout.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dumbbellworkout.*
import com.example.dumbbellworkout.data.repository.OverloadSuggestion
import com.example.dumbbellworkout.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ActiveWorkoutUiState(
    val exerciseIndex: Int = 0,
    val currentSet: Int = 1,
    val isResting: Boolean = false,
    val isFinished: Boolean = false,
    val restSecondsLeft: Int = 0,
    val elapsedSeconds: Int = 0,
    val sessionTonnage: Float = 0f,
    val totalSets: Int = 0,
    val sessionRecords: Int = 0,
    val showRecord: Boolean = false,
    val recordOldWeight: Float = 0f,
    val noteText: String = "",
    val exerciseVisible: Boolean = true,
    val lastWeight: Float? = null,
    val overloadSuggestion: OverloadSuggestion? = null,
    val exerciseHistorySets: List<com.example.dumbbellworkout.data.db.WorkoutSetEntity> = emptyList(),
    val prevTonnage: Float = 0f
)

class ActiveWorkoutViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val repo = WorkoutRepository(context)

    private val _uiState = MutableStateFlow(ActiveWorkoutUiState())
    val uiState: StateFlow<ActiveWorkoutUiState> = _uiState

    private var workout: Workout? = null

    fun init(workoutId: String) {
        workout = ALL_WORKOUTS[workoutId]
        loadExerciseData(0)
    }

    private fun loadExerciseData(index: Int) {
        val w = workout ?: return
        val exercise = w.exercises.getOrNull(index) ?: return
        viewModelScope.launch {
            val lastWeight = repo.getLastWeight(exercise.name)
            val history = repo.getExerciseHistory(exercise.name)
            val note = NotesManager.getNote(context, exercise.name)

            // Прогрессивная перегрузка
            val targetReps = exercise.reps
                .replace(" на руку", "").replace(" на сторону", "")
                .split("-").lastOrNull()?.trim()?.toIntOrNull() ?: 12
            val suggestion = repo.getProgressiveOverloadSuggestion(exercise.name, targetReps)

            _uiState.value = _uiState.value.copy(
                lastWeight = lastWeight,
                exerciseHistorySets = history,
                noteText = note,
                overloadSuggestion = suggestion,
                exerciseVisible = true
            )
        }
    }

    suspend fun saveSet(weightInput: String, repsInput: String): Boolean {
        val w = workout ?: return false
        val exercise = w.exercises.getOrNull(_uiState.value.exerciseIndex) ?: return false
        val weight = weightInput.replace(",", ".").toFloatOrNull() ?: return false
        val reps = repsInput.toIntOrNull() ?: return false

        val oldMax = repo.getMaxWeight(exercise.name)
        var records = _uiState.value.sessionRecords
        var showRecord = false
        var recordOld = 0f

        if (weight > oldMax && oldMax > 0f) {
            showRecord = true
            recordOld = oldMax
            records++
            VibrationHelper.vibrateRecord(context)
        }

        repo.addSet(exercise.name, _uiState.value.currentSet, weight, reps)

        val newTonnage = _uiState.value.sessionTonnage + weight * reps
        val newTotalSets = _uiState.value.totalSets + 1

        _uiState.value = _uiState.value.copy(
            sessionTonnage = newTonnage,
            totalSets = newTotalSets,
            sessionRecords = records,
            showRecord = showRecord,
            recordOldWeight = recordOld
        )

        if (_uiState.value.currentSet < exercise.sets) {
            _uiState.value = _uiState.value.copy(
                currentSet = _uiState.value.currentSet + 1,
                restSecondsLeft = exercise.restSeconds,
                isResting = true
            )
        } else if (_uiState.value.exerciseIndex < w.exercises.size - 1) {
            moveToNextExercise(true, exercise.restSeconds)
        } else {
            finishWorkout()
        }
        return true
    }

    fun moveToNextExercise(withRest: Boolean = false, restSec: Int = 0) {
        val w = workout ?: return
        val nextIndex = _uiState.value.exerciseIndex + 1
        if (nextIndex >= w.exercises.size) {
            finishWorkout()
            return
        }

        _uiState.value = _uiState.value.copy(
            exerciseVisible = false
        )

        viewModelScope.launch {
            kotlinx.coroutines.delay(300)
            _uiState.value = _uiState.value.copy(
                exerciseIndex = nextIndex,
                currentSet = 1,
                isResting = withRest,
                restSecondsLeft = if (withRest) restSec else 0,
                showRecord = false
            )
            loadExerciseData(nextIndex)
        }
    }

    fun skipExercise() {
        val w = workout ?: return
        if (_uiState.value.exerciseIndex < w.exercises.size - 1) {
            moveToNextExercise()
        } else {
            finishWorkout()
        }
    }

    fun tickRestTimer() {
        val current = _uiState.value.restSecondsLeft
        if (current > 0) {
            _uiState.value = _uiState.value.copy(restSecondsLeft = current - 1)
        } else {
            _uiState.value = _uiState.value.copy(isResting = false)
            VibrationHelper.vibrateRestEnd(context)
        }
    }

    fun skipRest() {
        _uiState.value = _uiState.value.copy(isResting = false, restSecondsLeft = 0)
    }

    fun tickElapsedTimer() {
        _uiState.value = _uiState.value.copy(
            elapsedSeconds = _uiState.value.elapsedSeconds + 1
        )
    }

    fun dismissRecord() {
        _uiState.value = _uiState.value.copy(showRecord = false)
    }

    fun updateNote(exercise: Exercise, text: String) {
        NotesManager.saveNote(context, exercise.name, text)
        _uiState.value = _uiState.value.copy(noteText = text)
    }

    private fun finishWorkout() {
        viewModelScope.launch {
            val allDates = repo.getAllDates()
            val prevTonnage = if (allDates.size >= 2) {
                repo.getTonnageForDate(allDates[1])  // second most recent
            } else 0f

            StreakManager.recordWorkout(context)
            LevelManager.addXP(context, LevelManager.xpForWorkout())
            val streak = StreakManager.getCurrentStreak(context)
            LevelManager.addXP(context, LevelManager.xpForStreak(streak))

            _uiState.value = _uiState.value.copy(
                isFinished = true,
                prevTonnage = prevTonnage
            )
        }
    }

    fun getWorkout(): Workout? = workout
}

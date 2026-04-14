package com.example.dumbbellworkout.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dumbbellworkout.data.db.WorkoutSetEntity
import com.example.dumbbellworkout.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HistoryUiState(
    val allDates: List<String> = emptyList(),
    val filteredDates: List<String> = emptyList(),
    val selectedDate: String? = null,
    val setsForDate: List<WorkoutSetEntity> = emptyList(),
    val exerciseNames: List<String> = emptyList(),
    val selectedExerciseFilter: String? = null,
    val isLoading: Boolean = true
)

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val repo = WorkoutRepository(context)

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            val dates = repo.getAllDates()
            val exercises = repo.getAllExerciseNames()

            _uiState.value = HistoryUiState(
                allDates = dates,
                filteredDates = dates,
                exerciseNames = exercises,
                isLoading = false
            )
        }
    }

    fun filterByExercise(exercise: String?) {
        viewModelScope.launch {
            val filtered = repo.getFilteredDates(exercise)
            _uiState.value = _uiState.value.copy(
                filteredDates = filtered,
                selectedExerciseFilter = exercise,
                selectedDate = null,
                setsForDate = emptyList()
            )
        }
    }

    fun selectDate(date: String) {
        viewModelScope.launch {
            val sets = repo.getSetsByDate(date)
            _uiState.value = _uiState.value.copy(
                selectedDate = date,
                setsForDate = sets
            )
        }
    }

    fun deleteSet(id: Long) {
        viewModelScope.launch {
            repo.deleteSet(id)
            // Reload current date
            val date = _uiState.value.selectedDate ?: return@launch
            selectDate(date)
        }
    }

    fun updateSet(id: Long, newWeight: Float, newReps: Int) {
        viewModelScope.launch {
            repo.updateSet(id, newWeight, newReps)
            val date = _uiState.value.selectedDate ?: return@launch
            selectDate(date)
        }
    }
}

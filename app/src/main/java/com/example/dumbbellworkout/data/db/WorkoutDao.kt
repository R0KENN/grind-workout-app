package com.example.dumbbellworkout.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    // ── Вставка ──
    @Insert
    suspend fun insertSet(set: WorkoutSetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBodyweight(entry: BodyweightEntity)

    // ── Все подходы ──
    @Query("SELECT * FROM workout_sets ORDER BY date DESC, setNumber ASC")
    fun getAllSetsFlow(): Flow<List<WorkoutSetEntity>>

    @Query("SELECT * FROM workout_sets ORDER BY date DESC, setNumber ASC")
    suspend fun getAllSets(): List<WorkoutSetEntity>

    // ── По дате ──
    @Query("SELECT * FROM workout_sets WHERE date = :date ORDER BY setNumber")
    suspend fun getSetsByDate(date: String): List<WorkoutSetEntity>

    @Query("SELECT DISTINCT date FROM workout_sets ORDER BY date DESC")
    suspend fun getAllDates(): List<String>

    @Query("SELECT DISTINCT date FROM workout_sets WHERE date BETWEEN :startDate AND :endDate ORDER BY date")
    suspend fun getDatesBetween(startDate: String, endDate: String): List<String>

    // ── По упражнению ──
    @Query("SELECT * FROM workout_sets WHERE exerciseName = :name ORDER BY date DESC, setNumber ASC")
    suspend fun getSetsForExercise(name: String): List<WorkoutSetEntity>

    @Query("SELECT MAX(weight) FROM workout_sets WHERE exerciseName = :name")
    suspend fun getMaxWeight(name: String): Float?

    @Query("""
        SELECT * FROM workout_sets 
        WHERE exerciseName = :name AND date != :today
        ORDER BY date DESC 
        LIMIT 20
    """)
    suspend fun getExerciseHistory(name: String, today: String): List<WorkoutSetEntity>

    @Query("""
        SELECT weight FROM workout_sets 
        WHERE exerciseName = :name 
        ORDER BY date DESC, setNumber DESC 
        LIMIT 1
    """)
    suspend fun getLastWeight(name: String): Float?

    // ── Тоннаж ──
    @Query("SELECT SUM(weight * reps) FROM workout_sets WHERE date = :date")
    suspend fun getTonnageForDate(date: String): Float?

    // ── Обновление и удаление ──
    @Query("""
        UPDATE workout_sets 
        SET weight = :newWeight, reps = :newReps 
        WHERE id = :id
    """)
    suspend fun updateSet(id: Long, newWeight: Float, newReps: Int)

    @Query("DELETE FROM workout_sets WHERE id = :id")
    suspend fun deleteSet(id: Long)

    @Query("DELETE FROM workout_sets WHERE date = :date")
    suspend fun deleteDay(date: String)

    // ── Bodyweight ──
    @Query("SELECT * FROM bodyweight ORDER BY date ASC")
    fun getBodyweightFlow(): Flow<List<BodyweightEntity>>

    @Query("SELECT * FROM bodyweight ORDER BY date ASC")
    suspend fun getAllBodyweight(): List<BodyweightEntity>

    // ── Для статистики мышечных групп ──
    @Query("SELECT exerciseName, SUM(weight * reps) as totalVolume FROM workout_sets GROUP BY exerciseName")
    suspend fun getVolumePerExercise(): List<ExerciseVolume>

    // ── Для прогрессивной перегрузки ──
    @Query("""
        SELECT * FROM workout_sets 
        WHERE exerciseName = :name 
        ORDER BY date DESC, setNumber ASC 
        LIMIT :limit
    """)
    suspend fun getRecentSetsForExercise(name: String, limit: Int = 50): List<WorkoutSetEntity>

    // ── Для истории с фильтрами ──
    @Query("""
        SELECT DISTINCT date FROM workout_sets 
        WHERE (:exercise IS NULL OR exerciseName = :exercise)
        ORDER BY date DESC
    """)
    suspend fun getFilteredDates(exercise: String? = null): List<String>

    @Query("SELECT DISTINCT exerciseName FROM workout_sets ORDER BY exerciseName")
    suspend fun getAllExerciseNames(): List<String>

    // ── Подсчёт тренировок ──
    @Query("SELECT COUNT(DISTINCT date) FROM workout_sets")
    suspend fun getTotalWorkoutDays(): Int

    // ── Для графиков по мышечным группам по неделям ──
    @Query("""
        SELECT date, exerciseName, SUM(weight * reps) as totalVolume 
        FROM workout_sets 
        GROUP BY date, exerciseName
        ORDER BY date ASC
    """)
    suspend fun getDailyVolumePerExercise(): List<DailyExerciseVolume>
}

data class ExerciseVolume(
    val exerciseName: String,
    val totalVolume: Float
)

data class DailyExerciseVolume(
    val date: String,
    val exerciseName: String,
    val totalVolume: Float
)

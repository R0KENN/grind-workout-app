package com.example.dumbbellworkout.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sets")
data class WorkoutSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val exerciseName: String,
    val setNumber: Int,
    val weight: Float,
    val reps: Int,
    val type: String = "normal",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bodyweight")
data class BodyweightEntity(
    @PrimaryKey val date: String,
    val weight: Float,
    val timestamp: Long = System.currentTimeMillis()
)

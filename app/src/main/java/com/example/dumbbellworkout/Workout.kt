package com.example.dumbbellworkout

data class Workout(
    val id: String,
    val name: String,
    val time: String,
    val exercises: List<Exercise>
)

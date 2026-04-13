package com.example.dumbbellworkout

data class Exercise(
    val num: Int,
    val name: String,
    val sets: Int,
    val reps: String,
    val restSeconds: Int,
    val restDisplay: String,
    val target: String,
    val gifRes: Int = 0  // ← ссылка на GIF в папке raw
)

package com.example.dumbbellworkout.data.prefs

import kotlinx.serialization.Serializable

@Serializable
data class UserPrefs(
    val userName: String = "",
    val weightKg: Float = 0f,
    val heightCm: Int = 0,
    val theme: ThemeMode = ThemeMode.System,
    val useDynamicColor: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val motivationalNotificationsEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val defaultRestSeconds: Int = 90,
    val units: Units = Units.Metric
)

@Serializable
enum class ThemeMode { Light, Dark, System }

@Serializable
enum class Units { Metric, Imperial }

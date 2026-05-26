package com.example.dumbbellworkout

import android.content.Context

/**
 * Пользовательские настройки приложения.
 * Простой объект-обёртка над SharedPreferences, по аналогии с NotesManager.
 */
object UserPreferences {
    private const val PREFS = "user_prefs"
    private const val KEY_PROGRESSION_STEP = "progression_step_kg"
    private const val KEY_AUTOSUGGEST_ENABLED = "autosuggest_enabled"

    /** Шаг прогрессии веса в кг. По умолчанию 2.5 кг — стандарт для гантелей. */
    fun getProgressionStep(context: Context): Float {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getFloat(KEY_PROGRESSION_STEP, 2.5f)
    }

    fun setProgressionStep(context: Context, stepKg: Float) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putFloat(KEY_PROGRESSION_STEP, stepKg).apply()
    }

    /** Включена ли автоподсказка веса в активной тренировке. По умолчанию — да. */
    fun isAutosuggestEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTOSUGGEST_ENABLED, true)
    }

    fun setAutosuggestEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTOSUGGEST_ENABLED, enabled).apply()
    }
}

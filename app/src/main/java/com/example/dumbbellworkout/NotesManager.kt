package com.example.dumbbellworkout

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object NotesManager {
    private const val PREFS = "exercise_notes"
    private val gson = Gson()

    fun saveNote(context: Context, exerciseName: String, note: String) {
        val notes = loadAll(context).toMutableMap()
        notes[exerciseName] = note
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString("all_notes", gson.toJson(notes)).apply()
    }

    fun getNote(context: Context, exerciseName: String): String {
        return loadAll(context)[exerciseName] ?: ""
    }

    private fun loadAll(context: Context): Map<String, String> {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = prefs.getString("all_notes", null) ?: return emptyMap()
        val type = object : TypeToken<Map<String, String>>() {}.type
        return try { gson.fromJson(json, type) } catch (_: Exception) { emptyMap() }
    }
}

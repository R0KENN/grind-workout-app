package com.example.dumbbellworkout.data.prefs

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPrefsRepository @Inject constructor(
    private val dataStore: DataStore<UserPrefs>
) {
    val prefs: Flow<UserPrefs> = dataStore.data

    suspend fun update(transform: (UserPrefs) -> UserPrefs) {
        dataStore.updateData(transform)
    }
}

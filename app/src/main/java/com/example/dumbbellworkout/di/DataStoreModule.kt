package com.example.dumbbellworkout.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import com.example.dumbbellworkout.data.prefs.UserPrefs
import com.example.dumbbellworkout.data.prefs.UserPrefsSerializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideUserPrefsDataStore(
        @ApplicationContext ctx: Context
    ): DataStore<UserPrefs> = DataStoreFactory.create(
        serializer = UserPrefsSerializer,
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
        produceFile = { ctx.dataStoreFile("user_prefs.json") }
    )
}

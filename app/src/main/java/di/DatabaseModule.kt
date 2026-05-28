package com.example.dumbbellworkout.di

import android.content.Context
import androidx.room.Room
import com.example.dumbbellworkout.data.db.AppDatabase
import com.example.dumbbellworkout.data.db.WorkoutDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "grind.db")
            // .addMigrations(...) ← добавим в этапе 4
            .build()

    @Provides
    fun provideWorkoutDao(db: AppDatabase): WorkoutDao = db.workoutDao()
}

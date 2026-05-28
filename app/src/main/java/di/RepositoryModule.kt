package com.example.dumbbellworkout.di

import com.example.dumbbellworkout.data.repository.WorkoutRepository
import com.example.dumbbellworkout.data.db.WorkoutDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideWorkoutRepository(dao: WorkoutDao): WorkoutRepository =
        WorkoutRepository(dao)
}

package com.example.dumbbellworkout.di

import android.content.Context
import com.example.dumbbellworkout.data.repository.WorkoutRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideWorkoutRepository(
        @ApplicationContext ctx: Context
    ): WorkoutRepository = WorkoutRepository(ctx)
}

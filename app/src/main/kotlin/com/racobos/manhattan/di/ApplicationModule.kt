package com.racobos.manhattan.di

import com.racobos.manhattan.repository.AuthDataRepository
import com.racobos.manhattan.repository.AuthRepository
import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import javax.inject.Singleton

@Module
class ApplicationModule(private val androidScheduler: Scheduler) {
    @Provides
    @Singleton
    fun provideAuthRepository(repository: AuthDataRepository): AuthRepository = repository

    @Provides
    @Singleton
    fun androidScheduler() = androidScheduler
}
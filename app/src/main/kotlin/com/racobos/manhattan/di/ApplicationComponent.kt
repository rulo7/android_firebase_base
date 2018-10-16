package com.racobos.manhattan.di

import com.racobos.manhattan.AndroidApplication
import com.racobos.manhattan.main.MainActivity
import com.racobos.manhattan.repository.AuthRepository
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [(ApplicationModule::class)])
interface ApplicationComponent {
    fun inject(androidApplication: AndroidApplication)
    fun inject(mainActivity: MainActivity)
    fun repository(): AuthRepository
}
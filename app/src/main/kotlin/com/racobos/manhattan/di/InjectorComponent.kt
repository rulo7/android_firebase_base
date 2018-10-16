package com.racobos.manhattan.di

import io.reactivex.android.schedulers.AndroidSchedulers

interface InjectorComponent {
    val injector: ApplicationComponent
}

fun injectorComponent() = object : InjectorComponent {
    override val injector: ApplicationComponent by lazy {
        DaggerApplicationComponent.builder()
            .applicationModule(ApplicationModule(AndroidSchedulers.mainThread()))
            .build()
    }
}
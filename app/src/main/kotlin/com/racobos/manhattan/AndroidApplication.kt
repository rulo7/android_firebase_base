package com.racobos.manhattan

import android.support.multidex.MultiDexApplication
import com.racobos.manhattan.di.InjectorComponent
import com.racobos.manhattan.di.injectorComponent

class AndroidApplication : MultiDexApplication(), InjectorComponent by injectorComponent() {

    override fun onCreate() {
        injector.inject(this)
        super.onCreate()
    }
}


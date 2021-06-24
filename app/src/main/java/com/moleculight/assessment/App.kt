package com.moleculight.assessment

import android.app.Application
import android.util.Log
import java.util.logging.LogManager

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        instance = this

        if (BuildConfig.DEBUG) {
            Log.d("App", "onCreate: DEBUG")
        }
    }

    companion object {
        lateinit var instance: App
        private set
    }
}
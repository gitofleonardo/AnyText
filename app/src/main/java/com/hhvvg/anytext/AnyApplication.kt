package com.hhvvg.anytext

import android.app.Application

class AnyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        application = this
    }

    companion object {
        lateinit var application: AnyApplication
    }
}
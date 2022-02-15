package com.hhvvg.anytext

import android.app.Application
import com.hhvvg.anytext.utils.SharedPreferenceTools

class AnyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        application = this
        SharedPreferenceTools.setup(this.applicationContext)
    }

    companion object {
        lateinit var application: AnyApplication
    }
}

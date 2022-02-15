package com.hhvvg.anytext.utils

import android.content.Context
import android.content.SharedPreferences

object SharedPreferenceTools {
    private lateinit var spInstance: SharedPreferences

    fun setup(context: Context) {
        spInstance =
            context.getSharedPreferences(DEFAULT_SHARED_PREFERENCES_FILE_NAME, Context.MODE_PRIVATE)
    }

    fun getInstance(): SharedPreferences {
        return spInstance
    }
}

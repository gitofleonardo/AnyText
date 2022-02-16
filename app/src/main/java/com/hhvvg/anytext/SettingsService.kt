package com.hhvvg.anytext

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.util.Log
import com.hhvvg.anytext.utils.DEFAULT_SHARED_PREFERENCES_FILE_NAME
import com.hhvvg.anytext.utils.KEY_SHOW_TEXT_BORDER

/**
 * @author hhvvg
 *
 * Provides settings.
 */

private const val TAG = "SettingsService"

class SettingsService : Service() {
    private lateinit var binder: IBinder
    private lateinit var spInstance: SharedPreferences

    override fun onCreate() {
        super.onCreate()
        binder = SettingProvider()
        spInstance = applicationContext.getSharedPreferences(
            DEFAULT_SHARED_PREFERENCES_FILE_NAME,
            Context.MODE_PRIVATE
        )
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    private inner class SettingProvider : ISettingProvider.Stub() {
        override fun textHintEnabled(): Boolean {
            return spInstance.getBoolean(KEY_SHOW_TEXT_BORDER, false)
        }
    }
}

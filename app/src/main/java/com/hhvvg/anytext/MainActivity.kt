package com.hhvvg.anytext

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.os.Bundle
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import com.hhvvg.anytext.databinding.ActivityMainBinding
import com.hhvvg.anytext.utils.KEY_SHOW_TEXT_BORDER
import com.hhvvg.anytext.utils.PACKAGE_NAME
import com.hhvvg.anytext.utils.SETTING_SERVICE_ACTION
import com.hhvvg.anytext.utils.SharedPreferenceTools

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val spInstance: SharedPreferences
        get() = SharedPreferenceTools.getInstance()
    private var settingBinder: ISettingProvider? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            service?.let {
                settingBinder = ISettingProvider.Stub.asInterface(it)
                readSettings()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupViews()
        bindSettings()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }

    private fun setupViews() {
        binding.cbEnableColorBorder.setOnCheckedChangeListener { _, isChecked ->
            val edit = spInstance.edit()
            edit.putBoolean(KEY_SHOW_TEXT_BORDER, isChecked)
            edit.apply()
        }
    }

    private fun bindSettings() {
        val intent = Intent().apply {
            action = SETTING_SERVICE_ACTION
            `package` = PACKAGE_NAME
        }
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun readSettings() {
        val enable = settingBinder?.textHintEnabled() ?: false
        binding.cbEnableColorBorder.isChecked = enable
    }
}

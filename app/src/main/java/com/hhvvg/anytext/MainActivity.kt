package com.hhvvg.anytext

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hhvvg.anytext.databinding.ActivityMainBinding
import com.hhvvg.anytext.utils.KEY_SHOW_TEXT_BORDER
import com.hhvvg.anytext.utils.SharedPreferenceTools

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val spInstance: SharedPreferences
        get() = SharedPreferenceTools.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupViews()
    }

    private fun setupViews() {
        binding.cbEnableColorBorder.setOnCheckedChangeListener { _, isChecked ->
            val edit = spInstance.edit()
            edit.putBoolean(KEY_SHOW_TEXT_BORDER, isChecked)
            edit.apply()
        }
    }
}

package com.betterbrainmemory.opentoday.gui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.betterbrainmemory.opentoday.R
import com.betterbrainmemory.opentoday.databinding.ActivitySetupBinding
import com.betterbrainmemory.opentoday.gui.UI
import com.betterbrainmemory.opentoday.util.InlineUtil.viewClick

class SetupActivity : AppCompatActivity() {

    private lateinit var settingsManager: com.betterbrainmemory.opentoday.app.settings.SettingsManager
    private lateinit var telemetry: com.betterbrainmemory.opentoday.app.Telemetry
    private lateinit var binding: ActivitySetupBinding
    private lateinit var themeAdapter: com.betterbrainmemory.opentoday.util.SimpleSpinnerAdapter<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = com.betterbrainmemory.opentoday.app.App.get(this)
        settingsManager = app.settingsManager
        telemetry = app.telemetry

        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewClick(binding.setupDone, this::done)

        themeAdapter = com.betterbrainmemory.opentoday.util.SimpleSpinnerAdapter<Int>(
            this
        )
            .add(getString(R.string.settings_theme_system), AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            .add(getString(R.string.settings_theme_light), AppCompatDelegate.MODE_NIGHT_NO)
            .add(getString(R.string.settings_theme_night), AppCompatDelegate.MODE_NIGHT_YES)

        binding.setupTheme.adapter = themeAdapter
        binding.setupTheme.setSelection(themeAdapter.getValuePosition(settingsManager.theme))
        binding.setupTheme.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val theme = themeAdapter.getItem(position)
                UI.setTheme(theme)
                settingsManager.theme = theme
                settingsManager.save()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun done() {
        val isTelemetry = binding.setupTelemetry.isChecked

        com.betterbrainmemory.opentoday.app.settings.SettingsManager.IS_TELEMETRY.set(settingsManager, isTelemetry)
        settingsManager.save()
        telemetry.isEnabled = isTelemetry
        setupDone()
        startMain()
    }

    private fun setupDone() {
        com.betterbrainmemory.opentoday.app.settings.SettingsManager.IS_FIRST_LAUNCH[settingsManager] = false
        settingsManager.save()
    }

    private fun startMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
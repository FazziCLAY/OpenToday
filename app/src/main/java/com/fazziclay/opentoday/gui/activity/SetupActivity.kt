package com.fazziclay.opentoday.gui.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.fazziclay.opentoday.R
import com.fazziclay.opentoday.app.App
import com.fazziclay.opentoday.app.Telemetry
import com.fazziclay.opentoday.app.settings.SettingsManager
import com.fazziclay.opentoday.databinding.ActivitySetupBinding
import com.fazziclay.opentoday.gui.UI
import com.fazziclay.opentoday.util.InlineUtil.viewClick
import com.fazziclay.opentoday.util.SimpleSpinnerAdapter

class SetupActivity : AppCompatActivity() {

    private lateinit var settingsManager: SettingsManager
    private lateinit var telemetry: Telemetry
    private lateinit var binding: ActivitySetupBinding
    private lateinit var themeAdapter: SimpleSpinnerAdapter<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = App.get(this)
        settingsManager = app.settingsManager
        telemetry = app.telemetry

        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewClick(binding.setupDone, this::done)

        themeAdapter = SimpleSpinnerAdapter<Int>(this)
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

        SettingsManager.IS_TELEMETRY.set(settingsManager, isTelemetry)
        settingsManager.save()
        telemetry.isEnabled = isTelemetry
        setupDone()
        startMain()
    }

    private fun setupDone() {
        SettingsManager.IS_FIRST_LAUNCH[settingsManager] = false
        settingsManager.save()
    }

    private fun startMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
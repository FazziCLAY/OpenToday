package com.fazziclay.opentoday.gui.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fazziclay.opentoday.app.App
import com.fazziclay.opentoday.databinding.ActivitySetupBinding
import com.fazziclay.opentoday.util.InlineUtil.viewClick

class SetupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySetupBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar!!.hide()
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewClick(binding.done, this::done)
    }

    private fun done() {
        val app = App.get(this)
        val isTelemetry = binding.telemetry.isChecked
        app.settingsManager.isTelemetry = isTelemetry
        app.settingsManager.save()
        app.telemetry.setEnabled(isTelemetry)
        setupDone()
        startMain()
    }

    private fun setupDone() {
        getSharedPreferences(App.SHARED_NAME, MODE_PRIVATE).edit().putBoolean(App.SHARED_KEY_IS_SETUP_DONE, true).apply()
    }

    private fun startMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
package com.fazziclay.opentoday.gui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.fazziclay.opentoday.app.App
import com.fazziclay.opentoday.app.FeatureFlag
import com.fazziclay.opentoday.gui.UI
import com.fazziclay.opentoday.util.DebugUtil

class PreMainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        run()
        finish()
    }

    private fun run() {
        DebugUtil.sleep(App.DEBUG_MAIN_ACTIVITY_START_SLEEP)
        if (App.DEBUG_TEST_EXCEPTION_ONCREATE_MAINACTIVITY) {
            throw RuntimeException("This is cute Runtime Exception :)", RuntimeException("DEBUG_TEST_EXCEPTION_ONCREATE_MAINACTIVITY is enabled :)"))
        }
        if (!(App.DEBUG_MAIN_ACTIVITY == MainActivity::class.java || App.DEBUG_MAIN_ACTIVITY == null)) {
            startActivity(Intent(this, App.DEBUG_MAIN_ACTIVITY))
            return
        }
        val app = App.get(this)
        UI.setTheme(app.settingsManager.theme)
        if (app.isFeatureFlag(FeatureFlag.SHOW_APP_STARTUP_TIME_IN_PREMAIN_ACTIVITY)) {
            val text = StringBuilder("App startup time:\n")
            text.append(app.appStartupTime).append("ms")
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
        }
        val sharedPreferences = getSharedPreferences(App.SHARED_NAME, MODE_PRIVATE)
        val isSetupDone = sharedPreferences.getBoolean(App.SHARED_KEY_IS_SETUP_DONE, false)
        if (isSetupDone) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, SetupActivity::class.java))
        }
    }
}
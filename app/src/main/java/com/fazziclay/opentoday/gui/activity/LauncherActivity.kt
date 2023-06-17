package com.fazziclay.opentoday.gui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.fazziclay.opentoday.app.App
import com.fazziclay.opentoday.gui.UI
import com.fazziclay.opentoday.util.DebugUtil

class LauncherActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        run()
        finish()
    }

    private fun run() {
        if (App.DEBUG) {
            DebugUtil.sleep(App.DEBUG_MAIN_ACTIVITY_START_SLEEP)
            if (App.DEBUG_TEST_EXCEPTION_ON_LAUNCH) {
                throw RuntimeException(
                    "This is cute Runtime Exception in LauncherActivity (debug)",
                    RuntimeException("DEBUG_TEST_EXCEPTION_ON_LAUNCH is enabled :)")
                )
            }
            if (App.DEBUG_MAIN_ACTIVITY != null) {
                startActivity(Intent(this, App.DEBUG_MAIN_ACTIVITY))
                return
            }
        }
        val app = App.get(this)
        UI.setTheme(app.settingsManager.theme)
        val sharedPreferences = getSharedPreferences(App.SHARED_NAME, MODE_PRIVATE)
        val isSetupDone = sharedPreferences.getBoolean(App.SHARED_KEY_IS_SETUP_DONE, false)
        if (isSetupDone) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, SetupActivity::class.java))
        }
    }
}
package com.fazziclay.opentoday.gui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.fazziclay.opentoday.Debug
import com.fazziclay.opentoday.app.App
import com.fazziclay.opentoday.app.settings.SettingsManager
import com.fazziclay.opentoday.gui.BackendInitializer
import com.fazziclay.opentoday.gui.EnumsRegistry
import com.fazziclay.opentoday.gui.UI
import com.fazziclay.opentoday.util.DebugUtil
import com.fazziclay.opentoday.util.InlineUtil.IPROF
import com.fazziclay.opentoday.util.profiler.Profiler

class LauncherActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        PROFILER.push("LauncherActivity:onCreate")

        PROFILER.push("backend_initializer_start_thread")
        BackendInitializer.startBackInitializerThread()

        PROFILER.swap("super.onCreate")
        super.onCreate(savedInstanceState)

        PROFILER.swap("run")
        run()

        PROFILER.swap("finish")
        finish()

        PROFILER.pop()
        PROFILER.pop()
    }

    private fun run() {
        if (App.DEBUG) {
            PROFILER.push("debugs")
            DebugUtil.sleep(Debug.DEBUG_MAIN_ACTIVITY_START_SLEEP)
            if (Debug.DEBUG_TEST_EXCEPTION_ON_LAUNCH) {
                throw RuntimeException(
                    "This is cute Runtime Exception in LauncherActivity (debug)",
                    RuntimeException("DEBUG_TEST_EXCEPTION_ON_LAUNCH is enabled :)")
                )
            }
            if (Debug.DEBUG_MAIN_ACTIVITY != null) {
                startActivity(Intent(this, Debug.DEBUG_MAIN_ACTIVITY))
                return
            }

            EnumsRegistry.missingChecks()
            PROFILER.pop()
        }

        PROFILER.push("app.get")
        val app = App.get(this)

        PROFILER.swap("settings")

        PROFILER.push("wait_settings_init")
        val startWait = System.currentTimeMillis()
        while (BackendInitializer.isWaitForModule(BackendInitializer.Module.SETTINGS_MANAGER)) {
            // waiting init settings
            if (System.currentTimeMillis() - startWait > 1000 * 10) {
                Toast.makeText(this, "Long...", Toast.LENGTH_SHORT).show()
                break
            }
        }
        PROFILER.pop()

        val isSetupDone = !SettingsManager.IS_FIRST_LAUNCH.get(app.settingsManager)
        val theme = SettingsManager.THEME.get(app.settingsManager)

        PROFILER.swap("theme")
        UI.setTheme(theme)

        PROFILER.swap("launches_activities")
        if (isSetupDone) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, SetupActivity::class.java))
        }
        PROFILER.pop()
    }

    companion object {
        private val PROFILER: Profiler = IPROF
    }
}
package com.betterbrainmemory.opentoday.gui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.betterbrainmemory.opentoday.app.App
import com.betterbrainmemory.opentoday.app.App.get
import com.betterbrainmemory.opentoday.gui.BackendInitializer.Module
import com.betterbrainmemory.opentoday.gui.BackendInitializer.isWaitForModule
import com.betterbrainmemory.opentoday.gui.BackendInitializer.startBackInitializerThread
import com.betterbrainmemory.opentoday.gui.UI
import com.betterbrainmemory.opentoday.util.InlineUtil.IPROF

class LauncherActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        PROFILER.push("LauncherActivity:onCreate")

        PROFILER.push("backend_initializer_start_thread")
        startBackInitializerThread()

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
            com.betterbrainmemory.opentoday.util.DebugUtil.sleep(
                com.betterbrainmemory.opentoday.Debug.DEBUG_MAIN_ACTIVITY_START_SLEEP)
            if (com.betterbrainmemory.opentoday.Debug.DEBUG_TEST_EXCEPTION_ON_LAUNCH) {
                throw RuntimeException(
                    "This is cute Runtime Exception in LauncherActivity (debug)",
                    RuntimeException("DEBUG_TEST_EXCEPTION_ON_LAUNCH is enabled :)")
                )
            }
            if (com.betterbrainmemory.opentoday.Debug.DEBUG_MAIN_ACTIVITY != null) {
                startActivity(Intent(this, com.betterbrainmemory.opentoday.Debug.DEBUG_MAIN_ACTIVITY))
                return
            }

            com.betterbrainmemory.opentoday.gui.EnumsRegistry.missingChecks()
            PROFILER.pop()
        }

        PROFILER.push("app.get")
        val app = get(this)

        PROFILER.swap("settings")

        PROFILER.push("wait_settings_init")
        val startWait = System.currentTimeMillis()
        while (isWaitForModule(
                Module.SETTINGS_MANAGER)
        ) {
            // waiting init settings
            if (System.currentTimeMillis() - startWait > 1000 * 10) {
                Toast.makeText(this, "Long...", Toast.LENGTH_SHORT).show()
                break
            }
        }
        PROFILER.pop()

        val isSetupDone = !com.betterbrainmemory.opentoday.app.settings.SettingsManager.IS_FIRST_LAUNCH.get(app.settingsManager)
        val theme = com.betterbrainmemory.opentoday.app.settings.SettingsManager.THEME.get(app.settingsManager)

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
        private val PROFILER: com.betterbrainmemory.opentoday.util.profiler.Profiler = IPROF
    }
}
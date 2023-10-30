package com.fazziclay.opentoday.gui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.fazziclay.opentoday.Debug
import com.fazziclay.opentoday.app.App
import com.fazziclay.opentoday.gui.EnumsRegistry
import com.fazziclay.opentoday.gui.UI
import com.fazziclay.opentoday.util.DebugUtil
import com.fazziclay.opentoday.util.InlineUtil.IPROF
import com.fazziclay.opentoday.util.profiler.Profiler

class LauncherActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        PROFILER.push("LauncherActivity:onCreate")
        PROFILER.push("super.onCreate")
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

        PROFILER.swap("manually_init_all")

            PROFILER.push("settings")
            val settingsManager = app.settingsManager
            val theme = settingsManager.theme
            val isSetupDone = settingsManager.isSetupDone
            PROFILER.swap("tabsManager")
            app.tabsManager

            PROFILER.pop()

        PROFILER.swap("theme")
        UI.setTheme(theme)

        PROFILER.swap("plugins")
        app.reinitPlugins()

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
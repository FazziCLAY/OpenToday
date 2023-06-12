package com.fazziclay.opentoday.gui.activity

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.fazziclay.opentoday.Debug
import com.fazziclay.opentoday.R
import com.fazziclay.opentoday.app.App
import com.fazziclay.opentoday.app.CrashReportContext
import com.fazziclay.opentoday.app.FeatureFlag
import com.fazziclay.opentoday.app.ImportantDebugCallback
import com.fazziclay.opentoday.app.SettingsManager
import com.fazziclay.opentoday.app.Telemetry.UiClosedLPacket
import com.fazziclay.opentoday.app.Telemetry.UiOpenLPacket
import com.fazziclay.opentoday.app.UpdateChecker
import com.fazziclay.opentoday.app.items.QuickNoteReceiver
import com.fazziclay.opentoday.databinding.ActivityMainBinding
import com.fazziclay.opentoday.databinding.NotificationDebugappBinding
import com.fazziclay.opentoday.databinding.NotificationUpdateAvailableBinding
import com.fazziclay.opentoday.gui.ActivitySettings
import com.fazziclay.opentoday.gui.EnumsRegistry
import com.fazziclay.opentoday.gui.UI
import com.fazziclay.opentoday.gui.UINotification
import com.fazziclay.opentoday.gui.UIRoot
import com.fazziclay.opentoday.gui.fragment.MainRootFragment
import com.fazziclay.opentoday.gui.interfaces.BackStackMember
import com.fazziclay.opentoday.util.ColorUtil
import com.fazziclay.opentoday.util.InlineUtil.nullStat
import com.fazziclay.opentoday.util.InlineUtil.viewClick
import com.fazziclay.opentoday.util.InlineUtil.viewVisible
import com.fazziclay.opentoday.util.Logger
import com.fazziclay.opentoday.util.NetworkUtil
import com.fazziclay.opentoday.util.callback.CallbackImportance
import com.fazziclay.opentoday.util.callback.Status
import java.text.SimpleDateFormat
import java.util.GregorianCalendar
import java.util.Locale
import java.util.Stack

@SuppressLint("NonConstantResourceId")
class MainActivity : AppCompatActivity(), UIRoot {
    companion object {
        private const val TAG = "MainActivity"
        private const val CONTAINER_ID = R.id.mainActivity_rootFragmentContainer
        private val DEFAULT_ACTIVITY_SETTINGS: ActivitySettings = ActivitySettings()
    }
    private lateinit var binding: ActivityMainBinding
    private lateinit var app: App
    private lateinit var settingsManager: SettingsManager
    private var lastExitClick: Long = 0

    // Current Date
    private lateinit var currentDateHandler: Handler
    private lateinit var currentDateRunnable: Runnable
    private lateinit var currentDateCalendar: GregorianCalendar
    private var activitySettingsStack: Stack<ActivitySettings> = Stack()
    private var debugView = false
    private var debugHandler: Handler? = null
    private lateinit var debugRunnable: Runnable
    private var debugViewSize = 13
    private var debugViewBackground: Int = 0x33000000
    @SuppressLint("SetTextI18n")
    private var importantDebugCallback = ImportantDebugCallback { m ->
        if (!App.DEBUG_IMPORTANT_NOTIFICATIONS) return@ImportantDebugCallback Status.Builder()
            .setRemoveCallback(true)
            .build()
        val text = TextView(this@MainActivity)
        val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        p.topMargin = 10
        p.bottomMargin = 10
        text.layoutParams = p
        text.setBackgroundColor(Color.RED)
        text.text = "Debug: $m"
        addNotification(UINotification.create(text, 2500).setEndCallback {
            Logger.d(TAG, "ImportantDebugCallback notification ended successfully")
        })
        return@ImportantDebugCallback Status.NONE
    }

    // Activity overrides
    override fun onCreate(savedInstanceState: Bundle?) {
        val startTime = System.currentTimeMillis()
        CrashReportContext.mainActivityCreate()
        CrashReportContext.FRONT.push("MainActivity")
        Logger.d(TAG, "onCreate", nullStat(savedInstanceState))
        if (App.DEBUG) EnumsRegistry.missingChecks()
        app = App.get(this)
        settingsManager = app.settingsManager
        UI.setTheme(settingsManager.theme)
        app.telemetry.send(UiOpenLPacket())
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        super.onCreate(savedInstanceState)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.hide()
        debugRunnable = Runnable {
            binding.debugInfo.text = ColorUtil.colorize(Debug.getDebugInfoText(), Color.WHITE, Color.TRANSPARENT, Typeface.NORMAL)
            if (debugView && debugHandler != null) {
                debugHandler!!.postDelayed(this.debugRunnable, 99)
            }
        }
        if (Debug.CUSTOM_MAINACTIVITY_BACKGROUND) binding.root.setBackgroundColor(Color.parseColor("#00ffff"))
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(CONTAINER_ID, MainRootFragment.create(), "MainRootFragment")
                    .commit()
        }
        setupNotifications()
        setupCurrentDate()
        if (settingsManager.isQuickNoteNotification) {
            QuickNoteReceiver.sendQuickNoteNotification(this)
        }
        updateDebugView()
        onBackPressedDispatcher.addCallback(object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val exit = Runnable { this@MainActivity.finish() }
                val def = Runnable {
                    if (System.currentTimeMillis() - lastExitClick > 2000) {
                        Toast.makeText(this@MainActivity, R.string.abc_pressAgainForExitWarning, Toast.LENGTH_SHORT).show()
                        lastExitClick = System.currentTimeMillis()
                    } else {
                        exit.run()
                    }
                }
                val fragment = supportFragmentManager.findFragmentById(CONTAINER_ID)
                if (fragment is BackStackMember) {
                    if (!fragment.popBackStack()) {
                        def.run()
                    }
                } else {
                    def.run()
                }
            }
        })
        updateByActivitySettings()
        Debug.mainActivityStartupTime = System.currentTimeMillis() - startTime
        app.importantDebugCallbacks.addCallback(CallbackImportance.DEFAULT, importantDebugCallback)
        if (App.DEBUG_LOG_ALL_IN_MAINACTIVITY) {
            Logger.i(TAG, "------------------")
            Logger.d(TAG, "Example debug message", 10, 20, 30, Exception("Exception in debug logging"))
            Logger.w(TAG, "Example warning message")
            Logger.e(TAG, "Example error message", Exception("Example exception for logger"))
            Logger.i(TAG, "------------------")
        }
    }

    override fun onPause() {
        super.onPause()
        CrashReportContext.mainActivityPause()
    }

    override fun onSupportNavigateUp(): Boolean {
        val toolbarSettings = getCurrentActivitySettings().toolbarSettings
        if (toolbarSettings != null) {
            if (toolbarSettings.backButtonRunnable != null) {
                toolbarSettings.backButtonRunnable.run()
                return true
            }
        }
        return false
    }

    private fun setupNotifications() {
        setupAppDebugNotify()
        setupUpdateAvailableNotify()
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.d(TAG, "onDestroy")
        app.telemetry.send(UiClosedLPacket())
        currentDateHandler.removeCallbacks(currentDateRunnable)
        app.importantDebugCallbacks.removeCallback(importantDebugCallback)
        CrashReportContext.mainActivityDestroy()
        CrashReportContext.FRONT.pop()
    }

    // Current Date
    private fun setupCurrentDate() {
        currentDateCalendar = GregorianCalendar()
        setCurrentDate()
        currentDateHandler = Handler(mainLooper)
        currentDateRunnable = Runnable {
                if (isDestroyed) return@Runnable
                setCurrentDate()
                internalItemsTick()
                val millis = System.currentTimeMillis() % 1000
                currentDateHandler.postDelayed(currentDateRunnable, 1000 - millis)
        }
        currentDateHandler.post(currentDateRunnable)
        viewClick(binding.currentDateDate, Runnable {
            val dialog = DatePickerDialog(this)
            dialog.datePicker.firstDayOfWeek = app.settingsManager.firstDayOfWeek
            dialog.show()
        })
        viewClick(binding.currentDateTime, Runnable {
            val dialog = DatePickerDialog(this)
            dialog.datePicker.firstDayOfWeek = app.settingsManager.firstDayOfWeek
            dialog.show()
        })
    }

    private fun internalItemsTick() {
        if (!app.isFeatureFlag(FeatureFlag.DISABLE_AUTOMATIC_TICK)) {
            app.tickThread.requestTick()
        }
    }

    private fun setCurrentDate() {
        currentDateCalendar.timeInMillis = System.currentTimeMillis()
        val time = currentDateCalendar.time

        // Date
        var dateFormat = SimpleDateFormat(settingsManager.datePattern, Locale.getDefault())
        binding.currentDateDate.text = dateFormat.format(time)

        // Time
        dateFormat = SimpleDateFormat(settingsManager.timePattern, Locale.getDefault())
        binding.currentDateTime.text = dateFormat.format(time)
    }

    // Update checker
    private fun setupUpdateAvailableNotify() {
        UpdateChecker.check(app) { available: Boolean, url: String?, name: String? ->
            runOnUiThread {
                if (available) {
                    val updateAvailableLayout = NotificationUpdateAvailableBinding.inflate(layoutInflater)
                    updateAvailableLayout.updateAvailableText.text = getString(R.string.notification_ui_updateAvailable,
                        name ?: "OTN"
                    )
                    binding.notifications.addView(updateAvailableLayout.root)
                    if (url != null) {
                        viewClick(updateAvailableLayout.root, Runnable { NetworkUtil.openBrowser(this@MainActivity, url) })
                    }
                } else {
                    ImportantDebugCallback.pushStatic("update checked return false..")
                }
            }
        }
    }

    // App is DEBUG warning notify
    private fun setupAppDebugNotify() {
        if (!App.DEBUG || app.isFeatureFlag(FeatureFlag.DISABLE_DEBUG_MODE_NOTIFICATION)) return

        val b = NotificationDebugappBinding.inflate(layoutInflater)
        b.notificationText.text = getString(R.string.notification_ui_debugApp, App.VERSION_BRANCH)
        binding.notifications.addView(b.root)
    }

    override fun addNotification(notification: UINotification) {
        binding.notifications.addView(notification.view)
        notification.attach {
            binding.notifications.removeView(notification.view)
        }
        if (notification.duration != UINotification.DURATION_PERMANENT) {
            Handler(mainLooper).postDelayed({
                notification.remove()
            }, notification.duration)
        }
    }

    fun toggleLogsOverlay() {
        debugView = !debugView
        updateDebugView()
    }

    private fun updateDebugView() {
        if (debugView) {
            if (debugHandler == null) {
                debugHandler = Handler(Looper.getMainLooper())
            }
            debugHandler?.post(debugRunnable)
            binding.debugInfo.visibility = View.VISIBLE
            binding.debugLogsSizeUp.visibility = View.VISIBLE
            binding.debugLogsSizeDown.visibility = View.VISIBLE
            binding.debugLogsSwitch.visibility = View.VISIBLE
            binding.debugLogsSwitch.setOnClickListener {
                viewVisible(binding.debugLogsScroll, binding.debugLogsSwitch.isChecked, View.GONE)
                binding.debugLogsText.text = ColorUtil.colorize("\n\n\n\n\n\n"+Logger.getLOGS().toString(), Color.BLUE, Color.TRANSPARENT, 0, false)
            }
            binding.debugLogsSwitch.setOnLongClickListener {
                toggleLogsOverlay()
                return@setOnLongClickListener true
            }
            binding.debugLogsText.textSize = debugViewSize.toFloat()
            binding.debugLogsSizeUp.setOnClickListener {
                debugViewSize++
                binding.debugLogsText.textSize = debugViewSize.toFloat()
            }
            binding.debugLogsSizeDown.setOnClickListener {
                debugViewSize--
                binding.debugLogsText.textSize = debugViewSize.toFloat()
            }

            binding.debugLogsSizeUp.setOnLongClickListener {
                debugViewBackground+=0x21000000
                binding.debugLogsScroll.setBackgroundColor(debugViewBackground)
                return@setOnLongClickListener true
            }
            binding.debugLogsSizeDown.setOnLongClickListener {
                debugViewBackground-=0x21000000
                binding.debugLogsScroll.setBackgroundColor(debugViewBackground)
                return@setOnLongClickListener true
            }
        } else {
            debugHandler?.removeCallbacks(debugRunnable)
            binding.debugInfo.visibility = View.GONE
            binding.debugLogsSizeUp.visibility = View.GONE
            binding.debugLogsSizeDown.visibility = View.GONE
            binding.debugLogsSwitch.visibility = View.GONE
            binding.debugLogsText.text = ""
            binding.debugLogsScroll.visibility = View.GONE
        }
    }

    override fun pushActivitySettings(a: ActivitySettings) {
        Logger.d(TAG, "pushActivitySettings(value)")
        activitySettingsStack.push(a)
        updateByActivitySettings()
    }

    override fun pushActivitySettings(a: ActivitySettingsPush) {
        Logger.d(TAG, "pushActivitySettings(interface)")
        val copy = getCurrentActivitySettings().clone()
        a.validate(copy)
        activitySettingsStack.push(copy)
        updateByActivitySettings()
    }

    fun interface ActivitySettingsPush {
        fun validate(a: ActivitySettings)
    }

    override fun popActivitySettings() {
        Logger.d(TAG, "popActivitySettings")
        activitySettingsStack.pop()
        updateByActivitySettings()
    }

    private fun getCurrentActivitySettings(): ActivitySettings {
        if (activitySettingsStack.empty()) {
            activitySettingsStack.push(DEFAULT_ACTIVITY_SETTINGS)
        }
        return activitySettingsStack.lastElement()
    }

    private fun updateByActivitySettings() {
        val settings = getCurrentActivitySettings()
        viewVisible(binding.currentDateDate, settings.isClockVisible, View.GONE)
        viewVisible(binding.currentDateTime, settings.isClockVisible, View.GONE)
        binding.currentDateDate.isEnabled = settings.isDateClickCalendar
        binding.currentDateTime.isEnabled = settings.isDateClickCalendar
        viewVisible(binding.notifications, settings.isNotificationsVisible || App.DEBUG_ALWAYS_SHOW_UI_NOTIFICATIONS, View.GONE)

        val toolbarSettings = settings.toolbarSettings
        if (toolbarSettings == null) {
            supportActionBar?.hide()
        } else {
            supportActionBar?.show()
            if (toolbarSettings.title != null) {
                supportActionBar?.title = toolbarSettings.title
            } else {
                supportActionBar?.setTitle(toolbarSettings.titleResId)
            }
            supportActionBar?.setDisplayShowHomeEnabled(toolbarSettings.isBackButton)
            supportActionBar?.setDisplayHomeAsUpEnabled(toolbarSettings.isBackButton)
            if (toolbarSettings.menu != 0 && toolbarSettings.menuInterface != null) {
                binding.toolbar.inflateMenu(toolbarSettings.menu)
                toolbarSettings.menuInterface.run(binding.toolbar.menu)
            } else if (binding.toolbar.menu != null) {
                binding.toolbar.menu.close()
            }
        }
    }
}
package com.betterbrainmemory.opentoday.gui.activity

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Menu
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.appcompat.app.AppCompatActivity
import com.betterbrainmemory.opentoday.R
import com.betterbrainmemory.opentoday.app.Telemetry.UiClosedLPacket
import com.betterbrainmemory.opentoday.databinding.ActivityMainBinding
import com.betterbrainmemory.opentoday.databinding.NotificationDebugappBinding
import com.betterbrainmemory.opentoday.databinding.NotificationUpdateAvailableBinding
import com.betterbrainmemory.opentoday.gui.UI
import com.betterbrainmemory.opentoday.gui.UINotification
import com.betterbrainmemory.opentoday.gui.fragment.MainRootFragment
import com.betterbrainmemory.opentoday.gui.interfaces.BackStackMember
import com.betterbrainmemory.opentoday.util.InlineUtil.nullStat
import com.betterbrainmemory.opentoday.util.InlineUtil.viewClick
import com.betterbrainmemory.opentoday.util.InlineUtil.viewVisible
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale
import java.util.Stack


@SuppressLint("NonConstantResourceId")
class MainActivity : AppCompatActivity(),
    com.betterbrainmemory.opentoday.gui.UIRoot {
    companion object {
        private const val TAG = "MainActivity"
        private const val CONTAINER_ID = R.id.mainActivity_rootFragmentContainer
        private val DEFAULT_ACTIVITY_SETTINGS: com.betterbrainmemory.opentoday.gui.ActivitySettings =
            com.betterbrainmemory.opentoday.gui.ActivitySettings()
        private val PROFILER = com.betterbrainmemory.opentoday.app.App.createProfiler("MainActivity")
    }
    private lateinit var binding: ActivityMainBinding
    private lateinit var app: com.betterbrainmemory.opentoday.app.App
    private lateinit var settingsManager: com.betterbrainmemory.opentoday.app.settings.SettingsManager
    private var lastExitClick: Long = 0

    // Current Date
    private lateinit var currentDateHandler: Handler
    private lateinit var currentDateRunnable: Runnable
    private lateinit var currentDateCalendar: GregorianCalendar
    private lateinit var settingsAnalogClockCallback: com.betterbrainmemory.opentoday.app.settings.SettingsManager.OptionChangedCallback
    private lateinit var settingsActionbarCallback: com.betterbrainmemory.opentoday.app.settings.SettingsManager.OptionChangedCallback
    private var activitySettingsStack: Stack<com.betterbrainmemory.opentoday.gui.ActivitySettings> = Stack()
    private var debugView = false
    private var debugHandler: Handler? = null
    private lateinit var debugRunnable: Runnable
    private var debugViewSize = 13
    private var debugViewBackground: Int = Color.BLACK
    @SuppressLint("SetTextI18n")
    private var importantDebugCallback =
        com.betterbrainmemory.opentoday.app.ImportantDebugCallback { m ->
            if (!com.betterbrainmemory.opentoday.Debug.DEBUG_IMPORTANT_NOTIFICATIONS) return@ImportantDebugCallback com.betterbrainmemory.opentoday.util.callback.Status.Builder()
                .setRemoveCallback(true)
                .build()
            val text = TextView(this@MainActivity)
            val p = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            p.topMargin = 10
            p.bottomMargin = 10
            text.layoutParams = p
            text.setBackgroundColor(Color.RED)
            text.text = "Debug: $m"
            addNotification(UINotification.create(text, 2500).setEndCallback {
                com.betterbrainmemory.opentoday.util.Logger.d(
                    TAG,
                    "ImportantDebugCallback notification ended successfully"
                )
            })
            return@ImportantDebugCallback com.betterbrainmemory.opentoday.util.callback.Status.NONE
        }

    // Activity overrides
    override fun onCreate(savedInstanceState: Bundle?) {
        PROFILER.push("MainActivity:onCreate")

        PROFILER.push("phase0")
        com.betterbrainmemory.opentoday.gui.BackendInitializer.startBackInitializerThread()
        val startTime = System.currentTimeMillis()
        com.betterbrainmemory.opentoday.app.CrashReportContext.mainActivityCreate()
        com.betterbrainmemory.opentoday.app.CrashReportContext.FRONT.push("MainActivity")
        com.betterbrainmemory.opentoday.util.Logger.d(TAG, "onCreate", nullStat(savedInstanceState))

        PROFILER.swap("phase1")
        app = com.betterbrainmemory.opentoday.app.App.get(this)
        while (com.betterbrainmemory.opentoday.gui.BackendInitializer.isWaitForModule(
                com.betterbrainmemory.opentoday.gui.BackendInitializer.Module.SETTINGS_MANAGER)) {
            // do nothing
        }
        settingsManager = app.settingsManager
        app.tryInitPlugins()

        PROFILER.swap("inflate&set")

        PROFILER.push("inflate")
        binding = ActivityMainBinding.inflate(layoutInflater)

        PROFILER.swap("set")
        setContentView(binding.root)

        PROFILER.pop()

        PROFILER.swap("super.onCreate")
        super.onCreate(savedInstanceState)

        PROFILER.swap("phase2")
        val theme = com.betterbrainmemory.opentoday.app.settings.SettingsManager.THEME.get(settingsManager)
        UI.setTheme(theme)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.hide()
        settingsActionbarCallback = com.betterbrainmemory.opentoday.app.settings.SettingsManager.OptionChangedCallback { option, value ->
            if (option == com.betterbrainmemory.opentoday.app.settings.SettingsManager.ACTIONBAR_POSITION) {
                val actionParams = (binding.toolbar.layoutParams as RelativeLayout.LayoutParams)
                val dateParams = (binding.currentDateDate.layoutParams as RelativeLayout.LayoutParams)
                val timeParams = (binding.currentDateTime.layoutParams as RelativeLayout.LayoutParams)
                val containerParams = (binding.mainActivityRootFragmentContainer.layoutParams as RelativeLayout.LayoutParams)
                val pos = value as com.betterbrainmemory.opentoday.app.settings.ActionBarPosition
                if (pos == com.betterbrainmemory.opentoday.app.settings.ActionBarPosition.TOP) {
                    actionParams.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                    dateParams.addRule(RelativeLayout.BELOW, binding.toolbar.id)
                    timeParams.addRule(RelativeLayout.BELOW, binding.toolbar.id)
                    containerParams.removeRule(RelativeLayout.ABOVE)
                } else {
                    actionParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
                    dateParams.removeRule(RelativeLayout.BELOW)
                    timeParams.removeRule(RelativeLayout.BELOW)
                    containerParams.addRule(RelativeLayout.ABOVE, binding.toolbar.id)
                }
            }
            return@OptionChangedCallback com.betterbrainmemory.opentoday.util.callback.Status.NONE
        }
        settingsActionbarCallback.run(com.betterbrainmemory.opentoday.app.settings.SettingsManager.ACTIONBAR_POSITION, com.betterbrainmemory.opentoday.app.settings.SettingsManager.ACTIONBAR_POSITION.get(settingsManager))
        settingsManager.callbacks.addCallback(com.betterbrainmemory.opentoday.util.callback.CallbackImportance.DEFAULT, settingsActionbarCallback)


        debugRunnable = Runnable {
            binding.debugInfo.text = com.betterbrainmemory.opentoday.util.ColorUtil.colorize(
                com.betterbrainmemory.opentoday.Debug.getDebugInfoText(),
                Color.WHITE,
                Color.TRANSPARENT,
                Typeface.NORMAL
            )
            if (debugView && debugHandler != null) {
                debugHandler!!.postDelayed(this.debugRunnable, 50)
            }
        }
        if (com.betterbrainmemory.opentoday.Debug.CUSTOM_MAINACTIVITY_BACKGROUND) binding.root.setBackgroundColor(Color.parseColor("#00ffff"))

        if (settingsManager.isQuickNoteNotification) {
            PROFILER.push("send_quick_note")
            com.betterbrainmemory.opentoday.app.items.QuickNoteReceiver.sendQuickNoteNotification(this)
            PROFILER.pop()
        }

        PROFILER.swap("wait_gui_for_back")
        while (com.betterbrainmemory.opentoday.gui.BackendInitializer.isWaitGuiForBack()) {
            // waiting back initialize
        }
        PROFILER.swap("telemetry")
        app.telemetry.send(com.betterbrainmemory.opentoday.app.Telemetry.UiOpenLPacket())

        PROFILER.swap("savedInstanceState==null actions")
        if (savedInstanceState == null) {
            PROFILER.push("fragment begin")

            supportFragmentManager.beginTransaction()
                .replace(CONTAINER_ID, MainRootFragment.create(), "MainRootFragment")
                .commit()

            PROFILER.pop()
        }

        PROFILER.swap("phase3")
        settingsAnalogClockCallback = com.betterbrainmemory.opentoday.app.settings.SettingsManager.OptionChangedCallback { option, value ->
            if (option == com.betterbrainmemory.opentoday.app.settings.SettingsManager.ANALOG_CLOCK_ENABLE) {
                val visible: Boolean = value as Boolean
                if (visible) {
                    if (getCurrentActivitySettings().isClockVisible) {
                        viewVisible(binding.analogClock, true, View.GONE)
                    }
                } else {
                    viewVisible(binding.analogClock, false, View.GONE)
                }

            } else if (option == com.betterbrainmemory.opentoday.app.settings.SettingsManager.ANALOG_CLOCK_SIZE) {
                val size: Int = (value as Int).coerceAtMost(500)
                val layoutParams = RelativeLayout.LayoutParams(size, size)
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END)
                binding.analogClock.layoutParams = layoutParams

            } else if (option == com.betterbrainmemory.opentoday.app.settings.SettingsManager.ANALOG_CLOCK_TRANSPARENCY) {
                val alpha: Float = ((value as Int) / 100f)
                binding.analogClock.alpha = alpha

            } else if (option == com.betterbrainmemory.opentoday.app.settings.SettingsManager.ANALOG_CLOCK_COLOR_SECONDS) {
                binding.analogClock.setSecondTint(value as Int)
            } else if (option == com.betterbrainmemory.opentoday.app.settings.SettingsManager.ANALOG_CLOCK_COLOR_MINUTE) {
                binding.analogClock.setMinuteTint(value as Int)
            } else if (option == com.betterbrainmemory.opentoday.app.settings.SettingsManager.ANALOG_CLOCK_COLOR_HOUR) {
                binding.analogClock.setHourTint(value as Int)
            }

            return@OptionChangedCallback com.betterbrainmemory.opentoday.util.callback.Status.NONE
        }
        setupNotifications()
        setupCurrentDate()
        updateDebugView()
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val exit = Runnable { this@MainActivity.finish() }
                val def = Runnable {
                    if (System.currentTimeMillis() - lastExitClick > 2000) {
                        Toast.makeText(
                            this@MainActivity,
                            R.string.abc_pressAgainForExitWarning,
                            Toast.LENGTH_SHORT
                        ).show()
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

        PROFILER.swap("phase4")
        updateByActivitySettings()
        com.betterbrainmemory.opentoday.Debug.mainActivityStartupTime = System.currentTimeMillis() - startTime
        app.importantDebugCallbacks.addCallback(com.betterbrainmemory.opentoday.util.callback.CallbackImportance.DEFAULT, importantDebugCallback)
        if (com.betterbrainmemory.opentoday.Debug.DEBUG_LOG_ALL_IN_MAINACTIVITY) {
            com.betterbrainmemory.opentoday.util.Logger.i(TAG, "------------------")
            com.betterbrainmemory.opentoday.util.Logger.d(
                TAG,
                "Example debug message",
                10,
                20,
                30,
                Exception("Exception in debug logging")
            )
            com.betterbrainmemory.opentoday.util.Logger.w(TAG, "Example warning message")
            com.betterbrainmemory.opentoday.util.Logger.e(TAG, "Example error message", Exception("Example exception for logger"))
            com.betterbrainmemory.opentoday.util.Logger.i(TAG, "------------------")
        }

        val HIDE_IMPORTANT_TODOS = true
        if (com.betterbrainmemory.opentoday.app.App.DEBUG) {
            try {
                val todo = com.betterbrainmemory.opentoday.util.StreamUtil.read(assets.open("IMPORTANT_TODO"))
                if (todo.isNotEmpty() && !HIDE_IMPORTANT_TODOS) {
                    binding.importantTodo.visibility = View.VISIBLE
                    binding.importantTodo.text = todo
                }
            } catch (ignored: Exception) {
            }
        }

        PROFILER.pop()
        PROFILER.pop()

        val requestPermissionLauncher = registerForActivityResult(
            RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, R.string.abc_success, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.abc_not_success, Toast.LENGTH_LONG).show();
            }
            tryCheckPermissions()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(
                android.Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            tryCheckPermissions()
        }
    }

    private fun tryCheckPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager: AlarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, R.string.main_activity_allowExactAlarms, Toast.LENGTH_LONG)
                    .show()
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                return
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            if (!notificationManager.canUseFullScreenIntent()) {
                Toast.makeText(
                    this,
                    R.string.main_activity_allowFullScreenIntent,
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)
                intent.data = Uri.parse("package:${applicationInfo.packageName}")
                startActivity(intent)
                return
            }
        }
    }

    override fun onResume() {
        super.onResume()
        com.betterbrainmemory.opentoday.util.Logger.i(TAG, "onResume")
        tryCheckPermissions()
    }

    override fun onPause() {
        super.onPause()
        com.betterbrainmemory.opentoday.app.CrashReportContext.mainActivityPause()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val res = getCurrentActivitySettings().toolbarSettings?.menu
        if (res != null && res != 0) {
            menuInflater.inflate(res, menu)
        }
        return true
    }

    private fun setupNotifications() {
        setupAppDebugNotify()
        setupUpdateAvailableNotify()
    }

    override fun onDestroy() {
        super.onDestroy()
        com.betterbrainmemory.opentoday.util.Logger.d(TAG, "onDestroy")
        app.telemetry.send(UiClosedLPacket())
        currentDateHandler.removeCallbacks(currentDateRunnable)
        app.importantDebugCallbacks.removeCallback(importantDebugCallback)
        settingsManager.callbacks.removeCallback(settingsAnalogClockCallback)
        settingsManager.callbacks.removeCallback(settingsActionbarCallback)

        com.betterbrainmemory.opentoday.app.CrashReportContext.mainActivityDestroy()
        com.betterbrainmemory.opentoday.app.CrashReportContext.FRONT.pop()
    }

    // Current Date
    private fun setupCurrentDate() {
        PROFILER.push("setupCurrentDate")
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
        settingsManager.callbacks.addCallback(com.betterbrainmemory.opentoday.util.callback.CallbackImportance.DEFAULT, settingsAnalogClockCallback)
        manualCallSettingAnalog(com.betterbrainmemory.opentoday.app.settings.SettingsManager.ANALOG_CLOCK_ENABLE)
        manualCallSettingAnalog(com.betterbrainmemory.opentoday.app.settings.SettingsManager.ANALOG_CLOCK_TRANSPARENCY)
        manualCallSettingAnalog(com.betterbrainmemory.opentoday.app.settings.SettingsManager.ANALOG_CLOCK_SIZE)
        manualCallSettingAnalog(com.betterbrainmemory.opentoday.app.settings.SettingsManager.ANALOG_CLOCK_COLOR_SECONDS)
        manualCallSettingAnalog(com.betterbrainmemory.opentoday.app.settings.SettingsManager.ANALOG_CLOCK_COLOR_MINUTE)
        manualCallSettingAnalog(com.betterbrainmemory.opentoday.app.settings.SettingsManager.ANALOG_CLOCK_COLOR_HOUR)
        PROFILER.pop()
    }

    private fun manualCallSettingAnalog(option: com.betterbrainmemory.opentoday.app.settings.Option) {
        settingsAnalogClockCallback.run(option, option.getObject(settingsManager))
    }

    private fun internalItemsTick() {
        if (!app.isFeatureFlag(com.betterbrainmemory.opentoday.app.FeatureFlag.DISABLE_AUTOMATIC_TICK)) {
            app.tickThread.requestTick()
        }
    }

    private fun setCurrentDate() {
        PROFILER.push("setCurrentDate")
        currentDateCalendar.timeInMillis = System.currentTimeMillis()
        val time = currentDateCalendar.time

        // Date
        var dateFormat = SimpleDateFormat(settingsManager.datePattern, Locale.getDefault())
        binding.currentDateDate.text = dateFormat.format(time)

        // Time
        dateFormat = SimpleDateFormat(settingsManager.timePattern, Locale.getDefault())
        binding.currentDateTime.text = dateFormat.format(time)

        // Analog
        if (com.betterbrainmemory.opentoday.app.settings.SettingsManager.ANALOG_CLOCK_ENABLE.get(settingsManager) || getCurrentActivitySettings().isAnalogClockForceVisible) {
            if (getCurrentActivitySettings().isAnalogClockForceHidden) {
                return
            }
            val hour = currentDateCalendar.get(Calendar.HOUR)
            val minute = currentDateCalendar.get(Calendar.MINUTE)
            val second = currentDateCalendar.get(Calendar.SECOND)
            val millis = currentDateCalendar.get(Calendar.MILLISECOND)
            binding.analogClock.setTime(hour, minute, second, millis)
        }
        PROFILER.pop()
    }

    // Update checker
    private fun setupUpdateAvailableNotify() {
        PROFILER.push("setupUpdateAvailableNotify")
        com.betterbrainmemory.opentoday.app.UpdateChecker.check(app) { available: Boolean, url: String?, name: String? ->
            runOnUiThread {
                if (available) {
                    val updateAvailableLayout = NotificationUpdateAvailableBinding.inflate(layoutInflater)
                    updateAvailableLayout.updateAvailableText.text = getString(R.string.notification_ui_updateAvailable,
                        name ?: "OTN"
                    )
                    binding.notifications.addView(updateAvailableLayout.root)
                    if (url != null) {
                        viewClick(updateAvailableLayout.root, Runnable { com.betterbrainmemory.opentoday.util.NetworkUtil.openBrowser(this@MainActivity, url) })
                    }
                } else {
                    com.betterbrainmemory.opentoday.app.ImportantDebugCallback.pushStatic("update checked return false..")
                }
            }
        }
        PROFILER.pop()
    }

    // App is DEBUG warning notify
    private fun setupAppDebugNotify() {
        if (!com.betterbrainmemory.opentoday.app.App.DEBUG || app.isFeatureFlag(
                com.betterbrainmemory.opentoday.app.FeatureFlag.DISABLE_DEBUG_MODE_NOTIFICATION)) return

        val b = NotificationDebugappBinding.inflate(layoutInflater)
        b.notificationText.text = getString(R.string.notification_ui_debugApp, com.betterbrainmemory.opentoday.app.App.VERSION_BRANCH)
        binding.notifications.addView(b.root)
    }

    override fun addNotification(notification: com.betterbrainmemory.opentoday.gui.UINotification) {
        binding.notifications.addView(notification.view)
        notification.attach {
            binding.notifications.removeView(notification.view)
        }
        if (notification.duration != com.betterbrainmemory.opentoday.gui.UINotification.DURATION_PERMANENT) {
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
                val IS_PROFILERS = com.betterbrainmemory.opentoday.Build.isProfilersEnabled()
                var text = app.logs.toString()
                if (IS_PROFILERS) {
                    text = ""
                    com.betterbrainmemory.opentoday.app.App.get().profilers.forEach {
                        text += "\n\n" + it.getResult(-1)
                    }
                }


                viewVisible(binding.debugLogsScroll, binding.debugLogsSwitch.isChecked, View.GONE)
                binding.debugLogsText.text = com.betterbrainmemory.opentoday.util.ColorUtil.colorize("\n\n\n\n\n\n\n\n\n\n\n\n\n"+text, Color.YELLOW, Color.TRANSPARENT, 0, false)
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

            binding.debugLogsScroll.setBackgroundColor(debugViewBackground)
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

    override fun pushActivitySettings(a: com.betterbrainmemory.opentoday.gui.ActivitySettings) {
        com.betterbrainmemory.opentoday.util.Logger.d(TAG, "pushActivitySettings(value)")
        activitySettingsStack.push(a)
        updateByActivitySettings()
    }

    override fun pushActivitySettings(a: ActivitySettingsPush) {
        com.betterbrainmemory.opentoday.util.Logger.d(TAG, "pushActivitySettings(interface)")
        val copy = getCurrentActivitySettings().clone()
        a.validate(copy)
        activitySettingsStack.push(copy)
        updateByActivitySettings()
    }

    fun interface ActivitySettingsPush {
        fun validate(a: com.betterbrainmemory.opentoday.gui.ActivitySettings)
    }

    override fun popActivitySettings() {
        com.betterbrainmemory.opentoday.util.Logger.d(TAG, "popActivitySettings")
        activitySettingsStack.pop()
        updateByActivitySettings()
    }

    private fun getCurrentActivitySettings(): com.betterbrainmemory.opentoday.gui.ActivitySettings {
        if (activitySettingsStack.empty()) {
            activitySettingsStack.push(DEFAULT_ACTIVITY_SETTINGS)
        }
        return activitySettingsStack.lastElement()
    }

    private fun updateByActivitySettings() {
        val settings = getCurrentActivitySettings()

        com.betterbrainmemory.opentoday.util.Logger.i(TAG, "update activity settings: $settings")

        val canon = settings.isShowCanonicalClock && com.betterbrainmemory.opentoday.app.settings.SettingsManager.ACTIONBAR_POSITION[settingsManager] == com.betterbrainmemory.opentoday.app.settings.ActionBarPosition.BOTTOM;
        val clockVisible = settings.isClockVisible || (canon)
        var analogClockVisible = (settings.isClockVisible && com.betterbrainmemory.opentoday.app.settings.SettingsManager.ANALOG_CLOCK_ENABLE.get(settingsManager)) || settings.isAnalogClockForceVisible
        if (settings.isAnalogClockForceHidden || canon) analogClockVisible = false

        viewVisible(binding.currentDateDate, clockVisible, View.GONE)
        viewVisible(binding.currentDateTime, clockVisible, View.GONE)
        viewVisible(binding.analogClock, analogClockVisible, View.GONE)
        binding.currentDateDate.isEnabled = settings.isDateClickCalendar
        binding.currentDateTime.isEnabled = settings.isDateClickCalendar
        viewVisible(binding.notifications, settings.isNotificationsVisible || com.betterbrainmemory.opentoday.Debug.DEBUG_ALWAYS_SHOW_UI_NOTIFICATIONS, View.GONE)

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
            if (toolbarSettings.menu != 0) {
                com.betterbrainmemory.opentoday.util.Logger.d(TAG, "toolbar inflated...")
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.toolbar.menu.clear()
                    binding.toolbar.invalidateMenu()
                    binding.toolbar.inflateMenu(toolbarSettings.menu)
                    toolbarSettings.menuInterface.run(binding.toolbar.menu)

                }, 25)
            } else if (binding.toolbar.menu != null) {
                binding.toolbar.menu.clear()
                binding.toolbar.menu.close()
                com.betterbrainmemory.opentoday.util.Logger.d(TAG, "toolbar closed...")
            }
        }
    }
}
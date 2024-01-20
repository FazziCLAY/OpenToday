package com.betterbrainmemory.opentoday.gui.fragment.settings

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.betterbrainmemory.opentoday.R
import com.betterbrainmemory.opentoday.app.PinCodeManager.PinCodeNotValidateException
import com.betterbrainmemory.opentoday.app.PinCodeManager.ValidationException
import com.betterbrainmemory.opentoday.app.items.item.ItemsRegistry.ItemInfo
import com.betterbrainmemory.opentoday.databinding.ExportBinding
import com.betterbrainmemory.opentoday.databinding.FragmentSettingsBinding
import com.betterbrainmemory.opentoday.gui.UI
import com.betterbrainmemory.opentoday.gui.fragment.MainRootFragment
import com.betterbrainmemory.opentoday.util.InlineUtil.viewClick
import org.json.JSONException
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.Locale

class SettingsFragment : Fragment() {
    companion object {
        private const val TAG = "SettingsFragment"

        @JvmStatic
        fun create(): SettingsFragment {
            return SettingsFragment()
        }
    }

    private lateinit var binding: FragmentSettingsBinding
    private lateinit var app: com.betterbrainmemory.opentoday.app.App
    private lateinit var sm: com.betterbrainmemory.opentoday.app.settings.SettingsManager
    private lateinit var colorHistoryManager: com.betterbrainmemory.opentoday.app.ColorHistoryManager
    private lateinit var pinCodeManager: com.betterbrainmemory.opentoday.app.PinCodeManager
    private var pinCodeCallback = Runnable {}
    private var easterEggLastClick: Long = 0
    private var easterEggCounter = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.betterbrainmemory.opentoday.app.CrashReportContext.FRONT.push("SettingsFragment")
        com.betterbrainmemory.opentoday.util.Logger.d(TAG, "onCreate")
        app = com.betterbrainmemory.opentoday.app.App.get(requireContext())
        sm = app.settingsManager
        colorHistoryManager = app.colorHistoryManager
        pinCodeManager = app.pinCodeManager
        UI.getUIRoot(this).pushActivitySettings { a ->
            a.isNotificationsVisible = false
            a.isClockVisible = false
            a.toolbarSettings = com.betterbrainmemory.opentoday.gui.ActivitySettings.ToolbarSettings.createBack(R.string.settings_title) { UI.rootBack(this@SettingsFragment) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        com.betterbrainmemory.opentoday.app.CrashReportContext.FRONT.pop()
        UI.getUIRoot(this).popActivitySettings()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSettingsBinding.inflate(inflater)
        setupView()
        return binding.root
    }

    private fun setupView() {
        setupThemeSpinner()
        setupFirstDayOfWeekSpinner()
        setupFirstTabSpinner()

        viewClick(binding.dateAndTimeFormat, Runnable {
            val preview = TextView(requireContext())
            val spinner = Spinner(requireContext())
            val adapter =
                com.betterbrainmemory.opentoday.util.SimpleSpinnerAdapter<com.betterbrainmemory.opentoday.app.settings.enums.DateAndTimePreset>(
                    requireContext()
                )
            spinner.adapter = adapter
            for (value in com.betterbrainmemory.opentoday.app.settings.enums.DateAndTimePreset.values()) {
                adapter.add(value.name, value)
            }
            spinner.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view123: View?, position: Int, id: Long) {
                    val t = adapter.getItem(position)
                    sm.applyDateAndTimePreset(t)
                    sm.save()

                    val current = GregorianCalendar().time
                    val dateFormat = SimpleDateFormat(sm.datePattern, Locale.getDefault())
                    val timeFormat = SimpleDateFormat(sm.timePattern, Locale.getDefault())

                    val previewText = dateFormat.format(current) + "  " + timeFormat.format(current)

                    preview.text = previewText
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            val view = LinearLayout(requireContext())
            view.orientation = LinearLayout.VERTICAL
            view.addView(preview)
            view.addView(spinner)

            AlertDialog.Builder(requireContext())
                .setView(view)
                .show()
        })

        // Debug
        viewClick(binding.themeTitle, Runnable { experimentalFeaturesInteract() })

        // QuickNote
        binding.quickNoteCheckbox.isChecked = sm.isQuickNoteNotification
        viewClick(binding.quickNoteCheckbox, Runnable {
            sm.isQuickNoteNotification = binding.quickNoteCheckbox.isChecked
            if (sm.isQuickNoteNotification) {
                com.betterbrainmemory.opentoday.app.items.QuickNoteReceiver.sendQuickNoteNotification(requireContext())
            } else {
                com.betterbrainmemory.opentoday.app.items.QuickNoteReceiver.cancelQuickNoteNotification(requireContext())
            }
            sm.save()
        })



        // Lock color history
        binding.colorHistoryEnabled.isChecked = com.betterbrainmemory.opentoday.app.settings.SettingsManager.COLOR_HISTORY_ENABLED[sm]
        viewClick(binding.colorHistoryEnabled, Runnable {
            binding.colorHistoryLocked.isEnabled = binding.colorHistoryEnabled.isChecked
            com.betterbrainmemory.opentoday.app.settings.SettingsManager.COLOR_HISTORY_ENABLED[sm] = binding.colorHistoryEnabled.isChecked
            sm.save()
        })

        binding.colorHistoryLocked.isChecked = colorHistoryManager.isLocked
        binding.colorHistoryLocked.isEnabled = binding.colorHistoryEnabled.isChecked
        viewClick(binding.colorHistoryLocked, Runnable {
            colorHistoryManager.isLocked = binding.colorHistoryLocked.isChecked
        })

        // Export
        viewClick(binding.export, Runnable { showExportDialog(requireActivity(), sm, colorHistoryManager) })

        // Is telemetry
        binding.isTelemetry.isChecked = com.betterbrainmemory.opentoday.app.settings.SettingsManager.IS_TELEMETRY.get(sm)
        viewClick(binding.isTelemetry, Runnable {
            val isTelemetry = binding.isTelemetry.isChecked
            com.betterbrainmemory.opentoday.app.settings.SettingsManager.IS_TELEMETRY.set(sm, isTelemetry)
            sm.save()
            app.telemetry.setEnabled(isTelemetry)
            if (isTelemetry) AlertDialog.Builder(requireContext())
                .setTitle(R.string.setup_telemetry)
                .setMessage(R.string.setup_telemetry_details)
                .setPositiveButton(R.string.abc_ok, null)
                .show()
        })
        binding.defaultQuickNoteType.text = getString(R.string.settings_defaultQuickNoteType, com.betterbrainmemory.opentoday.gui.item.registry.ItemsGuiRegistry.REGISTRY.nameOf(context, sm.defaultQuickNoteType))
        viewClick(binding.defaultQuickNoteType, Runnable {
            com.betterbrainmemory.opentoday.gui.dialog.DialogSelectItemType(
                context,
                { type: ItemInfo ->
                    sm.defaultQuickNoteType = type
                    binding.defaultQuickNoteType.text = getString(
                        R.string.settings_defaultQuickNoteType,
                        com.betterbrainmemory.opentoday.gui.item.registry.ItemsGuiRegistry.REGISTRY.nameOf(
                            context,
                            sm.defaultQuickNoteType
                        )
                    )
                    sm.save()
                },
                sm.defaultQuickNoteType.identifier
            ).setTitle(getString(R.string.dialog_selectItemType_generic_title)).show()
        })
        pinCodeCallback = Runnable { binding.pincode.text = getString(R.string.settings_pincode, if (pinCodeManager.isPinCodeSet) getString(R.string.settings_pincode_on) else getString(R.string.settings_pincode_off)) }
        pinCodeCallback.run()
        viewClick(binding.pincode, Runnable { showPinCodeDialog() })

        // add item to top
        binding.addItemsToTop.isChecked = sm.itemAddPosition == com.betterbrainmemory.opentoday.app.settings.enums.ItemAddPosition.TOP
        viewClick(binding.addItemsToTop, Runnable {
            sm.itemAddPosition = if (binding.addItemsToTop.isChecked) com.betterbrainmemory.opentoday.app.settings.enums.ItemAddPosition.TOP else com.betterbrainmemory.opentoday.app.settings.enums.ItemAddPosition.BOTTOM
            sm.save()
        })

        // action bar position
        binding.actionbarInBottom.isChecked = com.betterbrainmemory.opentoday.app.settings.SettingsManager.ACTIONBAR_POSITION[sm] == com.betterbrainmemory.opentoday.app.settings.ActionBarPosition.BOTTOM
        viewClick(binding.actionbarInBottom, Runnable {
            com.betterbrainmemory.opentoday.app.settings.SettingsManager.ACTIONBAR_POSITION[sm] = if (binding.actionbarInBottom.isChecked) com.betterbrainmemory.opentoday.app.settings.ActionBarPosition.BOTTOM else com.betterbrainmemory.opentoday.app.settings.ActionBarPosition.TOP
            sm.save()
            UI.rootBack(this)
        })

        attachCheckBox(binding.parseTimeFromQuickNote, com.betterbrainmemory.opentoday.app.settings.SettingsManager.QUICK_NOTE_PARSE_TIME_FROM_ITEM)
        attachCheckBox(binding.minimizeGrayColor, com.betterbrainmemory.opentoday.app.settings.SettingsManager.ITEM_MINIMIZE_GRAY_COLOR)
        attachCheckBox(binding.trimItemNamesOnEdit, com.betterbrainmemory.opentoday.app.settings.SettingsManager.ITEM_TRIM_NAMES_IN_EDITOR)
        attachCheckBox(binding.confirmFastChanges, com.betterbrainmemory.opentoday.app.settings.SettingsManager.FAST_CHANGES_CONFIRM)
        attachCheckBox(binding.autoCloseToolbar, com.betterbrainmemory.opentoday.app.settings.SettingsManager.TOOLBAR_AUTOMATICALLY_CLOSE)
        attachCheckBox(binding.scrollToAddedItem, com.betterbrainmemory.opentoday.app.settings.SettingsManager.ITEM_IS_SCROLL_TO_ADDED)
        attachCheckBox(binding.itemInternalBackgroundFromItem, com.betterbrainmemory.opentoday.app.settings.SettingsManager.ITEM_EDITOR_BACKGROUND_AS_ITEM)
        attachCheckBox(binding.isItemBackgroundRandom, com.betterbrainmemory.opentoday.app.settings.SettingsManager.ITEM_RANDOM_BACKGROUND)
        attachCheckBox(binding.isAnalogClock, com.betterbrainmemory.opentoday.app.settings.SettingsManager.ANALOG_CLOCK_ENABLE)
        attachCheckBox(binding.showItemPath, com.betterbrainmemory.opentoday.app.settings.SettingsManager.ITEM_PATH_VISIBLE)
        binding.analogClockOptions.setOnClickListener {
            UI.findFragmentInParents(this, MainRootFragment::class.java)?.navigate(
                com.betterbrainmemory.opentoday.gui.fragment.settings.AnalogClockSettingsFragment(), true)
        }
    }

    private fun attachCheckBox(compoundButton: CompoundButton, booleanOption: com.betterbrainmemory.opentoday.app.settings.BooleanOption) {
        compoundButton.isChecked = booleanOption.get(sm)
        viewClick(compoundButton, Runnable {
            booleanOption.set(sm, compoundButton.isChecked)
            sm.save()
        })
    }

    private fun setupFirstTabSpinner() {
        val adapter =
            com.betterbrainmemory.opentoday.util.SimpleSpinnerAdapter<com.betterbrainmemory.opentoday.app.settings.enums.FirstTab>(
                requireContext()
            )
        com.betterbrainmemory.opentoday.util.EnumUtil.addToSimpleSpinnerAdapter(requireContext(), adapter, com.betterbrainmemory.opentoday.app.settings.enums.FirstTab.values())
        binding.firstTab.adapter = adapter
        binding.firstTab.setSelection(adapter.getValuePosition(sm.firstTab))
        binding.firstTab.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val t = adapter.getItem(position)
                sm.firstTab = t
                sm.save()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupThemeSpinner() {
        val adapter = com.betterbrainmemory.opentoday.util.SimpleSpinnerAdapter<Int>(
            requireContext()
        )
                .add(requireContext().getString(R.string.settings_theme_system), AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                .add(requireContext().getString(R.string.settings_theme_light), AppCompatDelegate.MODE_NIGHT_NO)
                .add(requireContext().getString(R.string.settings_theme_night), AppCompatDelegate.MODE_NIGHT_YES)
        binding.themeSpinner.adapter = adapter
        binding.themeSpinner.setSelection(adapter.getValuePosition(sm.theme))
        binding.themeSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val theme = adapter.getItem(position)
                UI.setTheme(theme)
                sm.theme = theme
                sm.save()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupFirstDayOfWeekSpinner() {
        val weekdays = DateFormatSymbols.getInstance(Locale.getDefault()).weekdays
        val adapter = com.betterbrainmemory.opentoday.util.SimpleSpinnerAdapter<Int>(
            requireContext()
        )
                .add(weekdays[Calendar.SUNDAY], Calendar.SUNDAY)
                .add(weekdays[Calendar.MONDAY], Calendar.MONDAY)
        binding.firstDayOfWeekSpinner.adapter = adapter
        binding.firstDayOfWeekSpinner.setSelection(adapter.getValuePosition(sm.firstDayOfWeek))
        binding.firstDayOfWeekSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val day = adapter.getItem(position)
                sm.firstDayOfWeek = day
                sm.save()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun showPinCodeDialog() {
        val isPinSet = pinCodeManager.isPinCodeSet
        val d = AlertDialog.Builder(requireContext())
                .setTitle(R.string.settings_pincode_title)
                .setMessage(if (isPinSet) getString(R.string.settings_pincode_message_on, pinCodeManager.pinCode) else getString(R.string.settings_pincode_message_off))
                .setNeutralButton(R.string.settings_pincode_cancel, null)
                .setPositiveButton(if (isPinSet) R.string.settings_pincode_button_disable else R.string.settings_pincode_button_enable) { _: DialogInterface?, _: Int ->
                    if (isPinSet) {
                        pinCodeManager.disablePinCode()
                        pinCodeCallback.run()
                        Toast.makeText(app, R.string.settings_pincode_disable_success, Toast.LENGTH_SHORT).show()
                    } else {
                        val t = EditText(requireContext())
                        t.inputType = InputType.TYPE_CLASS_NUMBER
                        t.setHint(R.string.settings_pincode_enable_hint)
                        t.filters = arrayOf(InputFilter.LengthFilter(com.betterbrainmemory.opentoday.app.PinCodeManager.MAX_LENGTH))
                        AlertDialog.Builder(requireContext())
                                .setTitle(R.string.settings_pincode_enable_title)
                                .setMessage(R.string.settings_pincode_enable_message)
                                .setView(t)
                                .setPositiveButton(R.string.settings_pincode_enable_apply) EnterNewPinCodeDialog@ { _: DialogInterface?, _: Int ->
                                    try {
                                        pinCodeManager.enablePinCode(t.text.toString())
                                        pinCodeCallback.run()
                                        Toast.makeText(app, R.string.settings_pincode_enable_success, Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        if (e is PinCodeNotValidateException) {
                                            if (e.validationException == ValidationException.CONTAINS_NON_DIGITS_CHARS) {
                                                Toast.makeText(app, R.string.settings_pincode_enable_nonDigitsError, Toast.LENGTH_SHORT).show()
                                                return@EnterNewPinCodeDialog
                                            } else if (e.validationException == ValidationException.EMPTY) {
                                                Toast.makeText(app, R.string.settings_pincode_enable_emptyError, Toast.LENGTH_SHORT).show()
                                                return@EnterNewPinCodeDialog
                                            } else if (e.validationException == ValidationException.TOO_LONG) {
                                                Toast.makeText(app, R.string.settings_pincode_enable_tooLongError, Toast.LENGTH_SHORT).show()
                                                return@EnterNewPinCodeDialog
                                            }
                                        }
                                        Toast.makeText(app, R.string.settings_pincode_enable_unknownError, Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .setNegativeButton(R.string.settings_pincode_enable_cancel, null)
                                .show()
                    }
                }
        d.show()
    }

    private fun showExportDialog(context: Activity, settingsManager: com.betterbrainmemory.opentoday.app.settings.SettingsManager?, colorHistoryManager: com.betterbrainmemory.opentoday.app.ColorHistoryManager?) {
        val binding = ExportBinding.inflate(context.layoutInflater)
        AlertDialog.Builder(context)
                .setView(binding.root)
                .setTitle(R.string.settings_export_dialog_title)
                .setNegativeButton(R.string.abc_cancel, null)
                .setPositiveButton(R.string.settings_export_dialog_export) { _: DialogInterface?, _: Int ->
                    val tabsManger = com.betterbrainmemory.opentoday.app.App.get(context).tabsManager
                    val perms: MutableList<com.betterbrainmemory.opentoday.app.ImportWrapper.Permission> = ArrayList()
                    val isAllItems = binding.exportAllItems.isChecked
                    val isSettings = binding.exportSettings.isChecked
                    val isColorHistory = binding.exportColorHistory.isChecked
                    val dialogMessage = binding.exportDialogMessage.text.toString().trim { it <= ' ' }
                    val isDialogMessage = dialogMessage.isNotEmpty()
                    if (isAllItems) perms.add(com.betterbrainmemory.opentoday.app.ImportWrapper.Permission.ADD_TABS)
                    if (isSettings) perms.add(com.betterbrainmemory.opentoday.app.ImportWrapper.Permission.OVERWRITE_SETTINGS)
                    if (isColorHistory) perms.add(com.betterbrainmemory.opentoday.app.ImportWrapper.Permission.OVERWRITE_COLOR_HISTORY)
                    if (isDialogMessage) perms.add(com.betterbrainmemory.opentoday.app.ImportWrapper.Permission.PRE_IMPORT_SHOW_DIALOG)
                    val i = com.betterbrainmemory.opentoday.app.ImportWrapper.createImport(*perms.toTypedArray())
                    if (isDialogMessage) i.setDialogMessage(dialogMessage)
                    if (isAllItems) i.addTabAll(*tabsManger.allTabs)
                    if (isSettings) {
                        try {
                            i.setSettings(settingsManager!!.exportJSONSettings())
                        } catch (e: JSONException) {
                            Toast.makeText(context, context.getString(R.string.export_error, e.toString()), Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }
                    }
                    if (isColorHistory) {
                        try {
                            i.setColorHistory(colorHistoryManager!!.exportJSONColorHistory())
                        } catch (e: JSONException) {
                            Toast.makeText(context, context.getString(R.string.export_error, e.toString()), Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }
                    }
                    try {
                        val s = i.build().finalExport()
                        val clipboardManager = context.getSystemService(ClipboardManager::class.java)
                        clipboardManager.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.export_clipdata_label), s))
                        Toast.makeText(context, R.string.export_success, Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, context.getString(R.string.export_error, e.toString()), Toast.LENGTH_SHORT).show()
                    }
                }
                .show()
    }

    private fun experimentalFeaturesInteract() {
        if (!com.betterbrainmemory.opentoday.app.App.SECRET_SETTINGS_AVAILABLE) {
            return
        }

        if (System.currentTimeMillis() - easterEggLastClick < 1000) {
            easterEggCounter++
            if (easterEggCounter >= 6) {
                easterEggCounter = 0
                UI.Debug.showFeatureFlagsDialog(app, requireContext())
            }
        } else {
            easterEggCounter = 0
        }
        easterEggLastClick = System.currentTimeMillis()
    }
}
package com.fazziclay.opentoday.gui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.fazziclay.opentoday.R
import com.fazziclay.opentoday.app.App
import com.fazziclay.opentoday.app.FeatureFlag
import com.fazziclay.opentoday.app.items.tick.ItemsTickReceiver
import com.fazziclay.opentoday.gui.callbacks.UIDebugCallback
import com.fazziclay.opentoday.gui.fragment.MainRootFragment
import com.fazziclay.opentoday.gui.interfaces.NavigationHost
import com.fazziclay.opentoday.util.InlineUtil
import com.fazziclay.opentoday.util.callback.CallbackStorage
import com.fazziclay.opentoday.util.callback.Status
import java.util.UUID

object UI {
    private val debugCallbacks: CallbackStorage<UIDebugCallback> = CallbackStorage()

    @JvmStatic
    fun <T : Fragment?> findFragmentInParents(fragment: Fragment, find: Class<T>): T? {
        var parent = fragment.parentFragment
        while (true) {
            if (parent == null) {
                return null
            }
            if (parent.javaClass == find) {
                return parent as T
            }
            parent = parent.parentFragment
        }
    }

    @JvmStatic
    fun rootBack(fragment: Fragment) {
        val host = findFragmentInParents(fragment, MainRootFragment::class.java) ?: throw RuntimeException("Fragment is not contains MainRootFragment in parents!")
        host.popBackStack()
    }

    @JvmStatic
    fun getDebugCallbacks(): CallbackStorage<UIDebugCallback> {
        return debugCallbacks;
    }

    @JvmStatic
    fun navigate(navigationHost: NavigationHost, fragment: Fragment, addToBackStack: Boolean) {
        navigationHost.navigate(fragment, addToBackStack)
    }

    @JvmStatic
    fun setTheme(i: Int) {
        AppCompatDelegate.setDefaultNightMode(i)
    }

    object Debug {
        @SuppressLint("SetTextI18n")
        @JvmStatic
        fun showPersonalTickDialog(context: Context) {
            val idview = EditText(context)
            idview.hint = "Enter item UUID"

            val paths = CheckBox(context)
            paths.text = "Use paths?"

            val view = LinearLayout(context)
            view.orientation = LinearLayout.VERTICAL
            view.addView(idview)
            view.addView(paths)

            AlertDialog.Builder(context)
                    .setView(view)
                    .setPositiveButton("TICK") { _: DialogInterface?, _: Int ->
                        try {
                            val id = UUID.fromString(idview.text.toString())
                            context.sendBroadcast(ItemsTickReceiver.createIntent(context, id, paths.isChecked).putExtra("debugMessage", "Debug personal tick is work!"))
                        } catch (e: Exception) {
                            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                    .show()
        }

        @JvmStatic
        fun showCrashWithMessageDialog(context: Context?, exceptionMessagePattern: String?) {
            Toast.makeText(context, R.string.manuallyCrash_crash, Toast.LENGTH_SHORT).show()
            val message = EditText(context)
            message.setHint(R.string.manuallyCrash_dialog_inputHint)
            val dialog: Dialog = AlertDialog.Builder(context)
                    .setTitle(R.string.manuallyCrash_dialog_title)
                    .setView(message)
                    .setMessage(R.string.manuallyCrash_dialog_message)
                    .setPositiveButton(R.string.manuallyCrash_dialog_apply) { _: DialogInterface?, _: Int -> throw RuntimeException(String.format(exceptionMessagePattern!!, message.text.toString())) }
                    .setNegativeButton(R.string.manuallyCrash_dialog_cancel, null)
                    .create()
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
        }

        @JvmStatic
        fun showFeatureFlagsDialog(app: App, context: Context?) {
            val view = LinearLayout(context)
            view.orientation = LinearLayout.VERTICAL
            for (featureFlag in FeatureFlag.values()) {
                val c = CheckBox(context)
                c.text = featureFlag.name
                c.isChecked = app.isFeatureFlag(featureFlag)
                InlineUtil.viewClick(c, Runnable {
                    val `is` = c.isChecked
                    if (`is`) {
                        if (!app.isFeatureFlag(featureFlag)) {
                            app.featureFlags.add(featureFlag)
                        }
                    } else {
                        if (app.isFeatureFlag(featureFlag)) {
                            app.featureFlags.remove(featureFlag)
                        }
                    }
                    if (featureFlag == FeatureFlag.TOOLBAR_DEBUG) {
                        debugCallbacks.run(CallbackStorage.RunCallbackInterface { _, callback ->
                            callback.debugChange(`is`)
                            return@RunCallbackInterface Status.NONE
                        })
                    }
                })
                val textView = TextView(context)
                textView.text = featureFlag.description
                textView.textSize = 11f
                textView.setPadding(60, 0, 0, 0)
                view.addView(c)
                view.addView(textView)
            }
            val scrollView = ScrollView(context)
            scrollView.addView(view)
            val dialog: Dialog = AlertDialog.Builder(context)
                    .setView(scrollView)
                    .setTitle("DEBUG: FeatureFlags")
                    .setNegativeButton(R.string.abc_cancel, null)
                    .create()
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
        }
    }
}
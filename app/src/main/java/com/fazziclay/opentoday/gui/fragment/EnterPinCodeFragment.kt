package com.fazziclay.opentoday.gui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.fazziclay.opentoday.app.App
import com.fazziclay.opentoday.databinding.FragmentEnterPincodeBinding
import com.fazziclay.opentoday.databinding.NotificationTooLongPincodeBinding
import com.fazziclay.opentoday.gui.UI
import com.fazziclay.opentoday.gui.UINotification
import com.fazziclay.opentoday.gui.fragment.item.ItemsTabIncludeFragment
import com.fazziclay.opentoday.gui.fragment.settings.SettingsFragment
import com.fazziclay.opentoday.gui.interfaces.ActivitySettingsMember
import com.fazziclay.opentoday.gui.interfaces.NavigationHost
import com.fazziclay.opentoday.util.InlineUtil.viewClick

class EnterPinCodeFragment : Fragment(), ActivitySettingsMember {
    companion object {
        fun create(): EnterPinCodeFragment {
            return EnterPinCodeFragment()
        }
    }

    private lateinit var binding: FragmentEnterPincodeBinding
    private lateinit var app: App
    private lateinit var navigationHost: NavigationHost
    private var isAllowed = false
    private var tryNumber = 0
    private var currentPin = ""
    private var isKeyboardLock = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = App.get(requireContext())
        navigationHost = UI.findFragmentInParents(this, MainRootFragment::class.java)!!
        UI.getUIRoot(this).pushActivitySettings { a ->
            a.isNotificationsVisible = false
            a.isClockVisible = true
            a.isDateClickCalendar = false
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEnterPincodeBinding.inflate(layoutInflater)
        setupKeyboardEmulator()
        return binding.root
    }

    private fun allow() {
        if (app.isPinCodeTooLong) {
            UI.getUIRoot(this).addNotification(UINotification.create(generateTooLongDisableNotificationView(), 20000))
            app.pinCodeManager.disablePinCode()
        }
        isAllowed = true
        navigationHost.navigate(ItemsTabIncludeFragment.create(), false)
    }

    private fun generateTooLongDisableNotificationView(): View {
        val binding = NotificationTooLongPincodeBinding.inflate(layoutInflater)
        binding.root.setOnClickListener {
            navigationHost.navigate(SettingsFragment.create(), true)
        }
        return binding.root
    }

    private fun setupKeyboardEmulator() {
        viewClick(binding.number0, Runnable { onNumberPress('0') })
        viewClick(binding.number1, Runnable { onNumberPress('1') })
        viewClick(binding.number2, Runnable { onNumberPress('2') })
        viewClick(binding.number3, Runnable { onNumberPress('3') })
        viewClick(binding.number4, Runnable { onNumberPress('4') })
        viewClick(binding.number5, Runnable { onNumberPress('5') })
        viewClick(binding.number6, Runnable { onNumberPress('6') })
        viewClick(binding.number7, Runnable { onNumberPress('7') })
        viewClick(binding.number8, Runnable { onNumberPress('8') })
        viewClick(binding.number9, Runnable { onNumberPress('9') })
    }

    private fun onNumberPress(n: Char) {
        if (isKeyboardLock) return
        currentPin += n
        onPinChanged()
    }

    private fun onPinChanged() {
        val s = currentPin
        binding.pin.text = s
        if (s.length == app.pinCodeLength) {
            if (app.isPinCodeAllow(s)) {
                binding.pin.setTextColor(Color.GREEN)
                allow()
            } else {
                tryNumber++
                if (tryNumber >= 5) {
                    isKeyboardLock = true
                    binding.pin.visibility = View.GONE
                    binding.timeout.visibility = View.VISIBLE
                }
                currentPin = ""
                onPinChanged()
            }
        }
    }
}
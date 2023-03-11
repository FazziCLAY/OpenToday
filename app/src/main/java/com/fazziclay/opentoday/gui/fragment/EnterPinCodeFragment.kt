package com.fazziclay.opentoday.gui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.fazziclay.opentoday.app.App
import com.fazziclay.opentoday.databinding.FragmentEnterPincodeBinding
import com.fazziclay.opentoday.gui.UI
import com.fazziclay.opentoday.util.InlineUtil.viewClick

class EnterPinCodeFragment : Fragment() {
    companion object {
        fun create(): EnterPinCodeFragment {
            return EnterPinCodeFragment()
        }
    }

    private lateinit var binding: FragmentEnterPincodeBinding
    private lateinit var app: App
    private var isAllowed = false
    private var tryNumber = 0
    private var currentPin = ""
    private var isKeyboardLock = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = App.get(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEnterPincodeBinding.inflate(layoutInflater)
        setupKeyboardEmulator()
        return binding.root
    }

    private fun allow() {
        isAllowed = true
        UI.findFragmentInParents(this, MainRootFragment::class.java)!!.navigate(ItemsTabIncludeFragment.create(), false)
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
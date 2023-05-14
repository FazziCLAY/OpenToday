package com.fazziclay.opentoday.gui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.fazziclay.opentoday.R
import com.fazziclay.opentoday.app.App
import com.fazziclay.opentoday.databinding.FragmentAboutBinding
import com.fazziclay.opentoday.gui.UI
import com.fazziclay.opentoday.gui.activity.OpenSourceLicensesActivity
import com.fazziclay.opentoday.util.InlineUtil.viewClick
import com.fazziclay.opentoday.util.NetworkUtil
import java.text.SimpleDateFormat
import java.util.*

class AboutFragment : Fragment() {
    companion object {
        private const val LINK_OPENSOURCE = "https://github.com/fazziclay/opentoday"
        private const val LINK_ISSUES = "https://github.com/fazziclay/opentoday/issues"
        @JvmStatic
        fun create(): Fragment {
            return AboutFragment()
        }
    }

    private lateinit var binding: FragmentAboutBinding
    private var easterEggLastClick: Long = 0
    private var easterEggCounter = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAboutBinding.inflate(inflater)
        binding.textVersion.text = App.VERSION_NAME + " " + getReleaseTime()
        binding.textPackage.text = App.APPLICATION_ID
        viewClick(binding.title, this::manuallyCrashInteract)
        viewClick(binding.sourceCode, Runnable { NetworkUtil.openBrowser(requireActivity(), LINK_OPENSOURCE) })
        viewClick(binding.issues, Runnable { NetworkUtil.openBrowser(requireActivity(), LINK_ISSUES) })
        viewClick(binding.licenses, Runnable { requireActivity().startActivity(OpenSourceLicensesActivity.createLaunchIntent(requireContext())) })
        viewClick(binding.ok, Runnable { UI.rootBack(this) })
        return binding.root
    }

    private fun getReleaseTime(): String? {
        return SimpleDateFormat("yyyy.MM.dd HH:mm:ss").format(Date(App.VERSION_RELEASE_TIME * 1000))
    }

    private fun manuallyCrashInteract() {
        if (System.currentTimeMillis() - easterEggLastClick < 1000) {
            easterEggCounter++
            if (easterEggCounter == 3) {
                Toast.makeText(requireContext(), R.string.manuallyCrash_7tap, Toast.LENGTH_SHORT).show()
            }
            if (easterEggCounter >= 10) {
                easterEggCounter = 0
                UI.Debug.showCrashWithMessageDialog(requireContext(), "Crash by AboutFragment easterEgg :) %s")
            }
        } else {
            easterEggCounter = 0
        }
        easterEggLastClick = System.currentTimeMillis()
    }
}
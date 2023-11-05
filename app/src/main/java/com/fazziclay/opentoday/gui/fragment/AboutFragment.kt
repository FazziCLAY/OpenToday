package com.fazziclay.opentoday.gui.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.fazziclay.opentoday.R
import com.fazziclay.opentoday.app.App
import com.fazziclay.opentoday.databinding.FragmentAboutBinding
import com.fazziclay.opentoday.gui.ActivitySettings
import com.fazziclay.opentoday.gui.UI
import com.fazziclay.opentoday.gui.activity.OpenSourceLicensesActivity
import com.fazziclay.opentoday.gui.interfaces.ActivitySettingsMember
import com.fazziclay.opentoday.util.InlineUtil.viewClick
import com.fazziclay.opentoday.util.NetworkUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AboutFragment : Fragment(), ActivitySettingsMember {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UI.getUIRoot(this).pushActivitySettings { a ->
            a.isClockVisible = false
            a.isNotificationsVisible = true
            a.toolbarSettings = ActivitySettings.ToolbarSettings.createBack(R.string.aboutapp_title) { UI.rootBack(this) }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAboutBinding.inflate(inflater)

        if (binding.aboutText.background != null || !binding.aboutText.text.equals("OpenToday") || binding.aboutText.length() != 9 || !binding.aboutText.text.startsWith("Open") || !binding.aboutText.text.endsWith("Today")) {
            @SuppressLint("SetTextI18n")
            binding.aboutText.text = "OpenToday... Meow!"
            binding.aboutText.setTextColor(Color.GREEN)
            binding.aboutText.setBackgroundColor(Color.BLACK)
        }

        binding.textVersion.text = App.VERSION_NAME
        binding.textReleaseTime.text = getReleaseTime()
        binding.textBranch.text = App.VERSION_BRANCH
        if (!App.VERSION_BRANCH.equals("main", ignoreCase = true)) binding.textBranch.setTextColor(Color.RED)
        binding.textPackage.text = App.APPLICATION_ID
        viewClick(binding.aboutText, this::manuallySecretSettingsInteract)
        viewClick(binding.sourceCode, Runnable { NetworkUtil.openBrowser(requireActivity(), LINK_OPENSOURCE) })
        viewClick(binding.issues, Runnable { NetworkUtil.openBrowser(requireActivity(), LINK_ISSUES) })
        viewClick(binding.licenses, Runnable { requireActivity().startActivity(OpenSourceLicensesActivity.createLaunchIntent(requireContext())) })
        viewClick(binding.changelog, Runnable { UI.findFragmentInParents(this, MainRootFragment::class.java)!!.navigate(ChangelogFragment.create(), true) })
        return binding.root
    }

    private fun getReleaseTime(): String? {
        return SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault()).format(Date(App.VERSION_RELEASE_TIME * 1000))
    }

    private fun manuallySecretSettingsInteract() {
        if (!App.SECRET_SETTINGS_AVAILABLE) {
            return
        }

        if (System.currentTimeMillis() - easterEggLastClick < 1000) {
            easterEggCounter++
            if (easterEggCounter == 3) {
                Toast.makeText(requireContext(), R.string.fragment_about_secretSettingsWarning, Toast.LENGTH_SHORT).show()
            }
            if (easterEggCounter >= 10) {
                easterEggCounter = 0
                UI.findFragmentInParents(this, MainRootFragment::class.java)?.navigate(DeveloperFragment(), true)
            }
        } else {
            easterEggCounter = 0
        }
        easterEggLastClick = System.currentTimeMillis()
    }
}
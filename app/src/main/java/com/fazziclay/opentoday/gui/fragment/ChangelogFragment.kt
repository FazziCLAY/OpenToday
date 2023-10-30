package com.fazziclay.opentoday.gui.fragment

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.fazziclay.opentoday.R
import com.fazziclay.opentoday.databinding.FragmentChangelogBinding
import com.fazziclay.opentoday.gui.ActivitySettings
import com.fazziclay.opentoday.gui.UI
import com.fazziclay.opentoday.gui.interfaces.ActivitySettingsMember
import com.fazziclay.opentoday.util.ColorUtil
import com.fazziclay.opentoday.util.Logger
import com.fazziclay.opentoday.util.ResUtil
import com.fazziclay.opentoday.util.StreamUtil

class ChangelogFragment : Fragment(), ActivitySettingsMember {
    companion object {
        fun create(): Fragment {
            return ChangelogFragment()
        }

        private const val TAG: String = "ChangelogFragment"
    }

    private lateinit var binding: FragmentChangelogBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        UI.getUIRoot(this).pushActivitySettings {a ->
            a.isClockVisible = false
            a.isNotificationsVisible = false
            a.toolbarSettings = ActivitySettings.ToolbarSettings.createBack(R.string.changelogFragment_toolbar_title) {
                UI.rootBack(this)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangelogBinding.inflate(layoutInflater)
        try {
            binding.changelogText.text = ColorUtil.colorize(StreamUtil.read(requireActivity().assets.open("CHANGELOG")), ResUtil.getAttrColor(requireContext(), com.google.android.material.R.attr.colorOnBackground), Color.TRANSPARENT, Typeface.NORMAL)
        } catch (e: Exception) {
            Logger.e(TAG, "while set text", e)
            Toast.makeText(requireContext(), "Error", Toast.LENGTH_SHORT).show()
        }
        return binding.root
    }
}
package com.betterbrainmemory.opentoday.gui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.betterbrainmemory.opentoday.R
import com.betterbrainmemory.opentoday.gui.UI.getUIRoot
import com.betterbrainmemory.opentoday.gui.interfaces.BackStackMember
import com.betterbrainmemory.opentoday.gui.interfaces.NavigationHost
import com.betterbrainmemory.opentoday.util.InlineUtil.nullStat

class MainRootFragment : Fragment(), NavigationHost {
    companion object {
        private const val TAG = "MainRootFragment"
        private const val CONTAINER_ID = R.id.content
        fun create(): MainRootFragment {
            return MainRootFragment()
        }
    }

    private val firstFragmentInterface: FirstFragmentInterface = object : FirstFragmentInterface {
        override fun create(): Fragment {
            val app = com.betterbrainmemory.opentoday.app.App.get(requireContext())
            return if (app.isPinCodeNeed) {
                EnterPinCodeFragment.create()
            } else {
                com.betterbrainmemory.opentoday.gui.fragment.item.ItemsTabIncludeFragment.create()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.betterbrainmemory.opentoday.app.CrashReportContext.setMainRootFragment("onCreate")
        com.betterbrainmemory.opentoday.util.Logger.d(TAG, "onCreate saved=${nullStat(savedInstanceState)}")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        com.betterbrainmemory.opentoday.util.Logger.d(TAG, "onCreateView inflater=${nullStat(inflater)} container=${nullStat(container)} saved=${nullStat(savedInstanceState)}")
        val frameLayout = FrameLayout(requireContext())
        frameLayout.id = CONTAINER_ID
        return frameLayout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        com.betterbrainmemory.opentoday.util.Logger.d(TAG, "onViewCreated saved=${nullStat(savedInstanceState)}")
        if (savedInstanceState == null) {
            val first = firstFragmentInterface.create()
            com.betterbrainmemory.opentoday.app.CrashReportContext.setMainRootFragment("first fragment: $first")
            com.betterbrainmemory.opentoday.util.Logger.d(TAG, "onViewCreated", "fragment replaced to ${first.javaClass.canonicalName}")
            childFragmentManager.beginTransaction()
                    .replace(CONTAINER_ID, first)
                    .commit()
        }
    }

    override fun popBackStack(): Boolean {
        com.betterbrainmemory.opentoday.util.Logger.d(TAG, "popBackStack")
        val currentFragment = childFragmentManager.findFragmentById(CONTAINER_ID)
        if (currentFragment is BackStackMember) {
            com.betterbrainmemory.opentoday.util.Logger.d(TAG, "popBackStack", "current fragment is BackStackMember!")
            if (currentFragment.popBackStack()) {
                com.betterbrainmemory.opentoday.app.CrashReportContext.setMainRootFragment("popBackStack from BackStackMember child: true")
                return true
            }
        }
        com.betterbrainmemory.opentoday.app.CrashReportContext.setMainRootFragment("popBackStack")
        com.betterbrainmemory.opentoday.util.Logger.d(TAG, "popBackStack", "pop internal")
        if (childFragmentManager.backStackEntryCount > 0) {
            if (currentFragment is com.betterbrainmemory.opentoday.gui.interfaces.ActivitySettingsMember) {
                getUIRoot(this).popActivitySettings()
            }
            childFragmentManager.popBackStack()
            return true
        }
        return false
    }

    override fun navigate(fragment: Fragment, addToBackStack: Boolean) {
        com.betterbrainmemory.opentoday.app.CrashReportContext.setMainRootFragment("navigate addToBack=$addToBackStack fragment=$fragment")
        com.betterbrainmemory.opentoday.util.Logger.d(TAG, "navigate to=$fragment addToBack=$addToBackStack")
        val transaction = childFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in,  // enter
                R.anim.fade_out,  // exit
                R.anim.fade_in,   // popEnter
                R.anim.slide_out  // popExit
            )
            .replace(CONTAINER_ID, fragment)
        if (addToBackStack) transaction.addToBackStack(null)
        transaction.commit()
    }

    internal interface FirstFragmentInterface {
        fun create(): Fragment
    }
}
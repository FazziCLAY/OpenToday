package com.fazziclay.opentoday.gui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import com.fazziclay.opentoday.R
import com.fazziclay.opentoday.app.App
import com.fazziclay.opentoday.app.CrashReportContext
import com.fazziclay.opentoday.gui.UI.getUIRoot
import com.fazziclay.opentoday.gui.fragment.item.ItemsTabIncludeFragment
import com.fazziclay.opentoday.gui.interfaces.ActivitySettingsMember
import com.fazziclay.opentoday.gui.interfaces.BackStackMember
import com.fazziclay.opentoday.gui.interfaces.NavigationHost
import com.fazziclay.opentoday.util.InlineUtil.nullStat
import com.fazziclay.opentoday.util.Logger

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
            val app = App.get(requireContext())
            return if (app.isPinCodeNeed) {
                EnterPinCodeFragment.create()
            } else {
                ItemsTabIncludeFragment.create()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CrashReportContext.setMainRootFragment("onCreate")
        Logger.d(TAG, "onCreate saved=${nullStat(savedInstanceState)}")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Logger.d(TAG, "onCreateView inflater=${nullStat(inflater)} container=${nullStat(container)} saved=${nullStat(savedInstanceState)}")
        val frameLayout = FrameLayout(requireContext())
        frameLayout.id = CONTAINER_ID
        return frameLayout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Logger.d(TAG, "onViewCreated saved=${nullStat(savedInstanceState)}")
        if (savedInstanceState == null) {
            val first = firstFragmentInterface.create()
            CrashReportContext.setMainRootFragment("first fragment: $first")
            Logger.d(TAG, "onViewCreated", "fragment replaced to ${first.javaClass.canonicalName}")
            childFragmentManager.beginTransaction()
                    .replace(CONTAINER_ID, first)
                    .commit()
        }
    }

    override fun popBackStack(): Boolean {
        Logger.d(TAG, "popBackStack")
        val currentFragment = childFragmentManager.findFragmentById(CONTAINER_ID)
        if (currentFragment is BackStackMember) {
            Logger.d(TAG, "popBackStack", "current fragment is BackStackMember!")
            if (currentFragment.popBackStack()) {
                CrashReportContext.setMainRootFragment("popBackStack from BackStackMember child: true")
                return true
            }
        }
        CrashReportContext.setMainRootFragment("popBackStack")
        Logger.d(TAG, "popBackStack", "pop internal")
        if (childFragmentManager.backStackEntryCount > 0) {
            if (currentFragment is ActivitySettingsMember) {
                getUIRoot(this).popActivitySettings()
            }
            childFragmentManager.popBackStack()
            return true
        }
        return false
    }

    override fun navigate(fragment: Fragment, addToBackStack: Boolean) {
        CrashReportContext.setMainRootFragment("navigate addToBack=$addToBackStack fragment=$fragment")
        Logger.d(TAG, "navigate to=$fragment addToBack=$addToBackStack")
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
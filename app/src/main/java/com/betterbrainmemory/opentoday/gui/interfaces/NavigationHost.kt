package com.betterbrainmemory.opentoday.gui.interfaces

import androidx.fragment.app.Fragment

interface NavigationHost : BackStackMember {
    fun navigate(fragment: Fragment, addToBackStack: Boolean)
}
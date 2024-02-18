package com.betterbrainmemory.opentoday.gui.interfaces

interface BackStackMember {
    // return true if block
    fun popBackStack(): Boolean
}
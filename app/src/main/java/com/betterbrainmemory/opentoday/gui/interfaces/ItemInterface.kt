package com.betterbrainmemory.opentoday.gui.interfaces

import com.betterbrainmemory.opentoday.app.items.item.Item

fun interface ItemInterface {
    fun run(item: Item?)
}
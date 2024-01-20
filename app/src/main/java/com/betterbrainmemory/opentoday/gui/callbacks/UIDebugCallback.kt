package com.betterbrainmemory.opentoday.gui.callbacks

import com.betterbrainmemory.opentoday.app.FeatureFlag

abstract class UIDebugCallback :
    com.betterbrainmemory.opentoday.util.callback.Callback {
    abstract fun debugChange(state: Boolean)
    abstract fun featureFlagsChanged(featureFlag: FeatureFlag, boolean: Boolean)
}

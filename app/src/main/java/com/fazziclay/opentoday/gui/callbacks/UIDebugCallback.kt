package com.fazziclay.opentoday.gui.callbacks

import com.fazziclay.opentoday.app.FeatureFlag
import com.fazziclay.opentoday.util.callback.Callback

abstract class UIDebugCallback : Callback {
    abstract fun debugChange(state: Boolean)
    abstract fun featureFlagsChanged(featureFlag: FeatureFlag, boolean: Boolean)
}

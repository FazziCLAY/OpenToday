package com.fazziclay.opentoday.util;

import com.fazziclay.opentoday.util.callback.Callback;

public interface OnDebugLog extends Callback {
    void run(String text);
}

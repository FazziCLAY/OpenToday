package com.fazziclay.opentoday.util;

import com.fazziclay.opentoday.callback.Callback;

public interface OnDebugLog extends Callback {
    void run(String text);
}

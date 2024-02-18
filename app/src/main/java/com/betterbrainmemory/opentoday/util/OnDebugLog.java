package com.betterbrainmemory.opentoday.util;

import com.betterbrainmemory.opentoday.util.callback.Callback;

public interface OnDebugLog extends Callback {
    void run(String text);
}

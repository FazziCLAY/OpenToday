package ru.fazziclay.opentoday.util;

import ru.fazziclay.opentoday.callback.Callback;

public interface OnDebugLog extends Callback {
    void run(String text);
}

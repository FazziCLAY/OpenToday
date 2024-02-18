package com.betterbrainmemory.opentoday.fun.mathgame;

import com.betterbrainmemory.opentoday.util.callback.Callback;
import com.betterbrainmemory.opentoday.util.callback.Status;

public class MathGameItemCallback implements Callback {
    public Status mathGameStatUpdated(MathGameItem item, String text) {
        return Status.NONE;
    }
}

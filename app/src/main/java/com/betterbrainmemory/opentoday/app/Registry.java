package com.betterbrainmemory.opentoday.app;

import com.betterbrainmemory.opentoday.util.ThrowableRunnable;

public abstract class Registry <KEY, UNIT> {
    private boolean defaultInitialized = false;

    public void initializeDefault(ThrowableRunnable runnable) throws Exception {
        if (!defaultInitialized) {
            runnable.run();
            defaultInitialized = true;
        }
    }

    public abstract UNIT getByKey(KEY key);

}

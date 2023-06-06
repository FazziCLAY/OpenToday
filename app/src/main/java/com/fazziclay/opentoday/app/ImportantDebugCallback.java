package com.fazziclay.opentoday.app;

import com.fazziclay.opentoday.util.Logger;
import com.fazziclay.opentoday.util.callback.Callback;
import com.fazziclay.opentoday.util.callback.Status;

public interface ImportantDebugCallback extends Callback {
    static void pushStatic(String m) {
        Logger.i("ImportantDebugCallback", "message: " + m);
        App app = App.get();
        if (app != null) {
            app.getImportantDebugCallbacks().run((callbackStorage, callback) -> callback.push(m));
        } else {
            Logger.w("ImportantDebugCallback", "pushStatic app is null...");
        }
    }

    Status push(String m);
}

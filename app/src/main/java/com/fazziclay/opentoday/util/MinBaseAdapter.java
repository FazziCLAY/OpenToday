package com.fazziclay.opentoday.util;

import android.widget.BaseAdapter;
import android.widget.EditText;

public abstract class MinBaseAdapter extends BaseAdapter {
    /**
     * @deprecated use in MinTextWatcher
     */
    @Deprecated
    public static void after(EditText text, Runnable o) {
        MinTextWatcher.after(text, o);
    }

    @Override
    public Object getItem(int position) {
        return null;
    }
    @Override
    public long getItemId(int position) {
        return 0;
    }
}

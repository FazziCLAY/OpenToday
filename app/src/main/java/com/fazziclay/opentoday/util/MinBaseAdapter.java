package com.fazziclay.opentoday.util;

import android.text.Editable;
import android.widget.BaseAdapter;
import android.widget.EditText;

public abstract class MinBaseAdapter extends BaseAdapter {
    // TODO: 09.06.2023 what? move to mintextwatcher
    public static void after(EditText text, Runnable o) {
        text.addTextChangedListener(new MinTextWatcher(){
            @Override
            public void afterTextChanged(Editable s) {
                o.run();
            }
        });
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

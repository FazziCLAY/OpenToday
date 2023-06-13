package com.fazziclay.opentoday.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

public class MinTextWatcher implements TextWatcher {
    public static void runAtDisabled(TextView textView, TextWatcher textWatcher, Runnable f) {
        textView.removeTextChangedListener(textWatcher);
        f.run();
        textView.addTextChangedListener(textWatcher);
    }

    public static void afterAll(Runnable runnable, TextView... text) {
        for (TextView textView : text) {
            after(textView, runnable);
        }
    }

    public static void after(TextView text, Runnable runnable) {
        text.addTextChangedListener(new MinTextWatcher(){
            @Override
            public void afterTextChanged(Editable s) {
                runnable.run();
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}
    @Override
    public void afterTextChanged(Editable s) {}
}

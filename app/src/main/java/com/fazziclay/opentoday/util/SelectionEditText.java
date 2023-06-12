package com.fazziclay.opentoday.util;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SelectionEditText extends androidx.appcompat.widget.AppCompatEditText {
    private OnSelectionChangedListener onSelectionChangedListener = null;
    private int selectionStart = -1;
    private int selectionEnd = -1;

    public SelectionEditText(@NonNull Context context) {
        super(context);
    }

    public SelectionEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SelectionEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        this.selectionStart = selStart;
        this.selectionEnd = selEnd;
        if (onSelectionChangedListener != null) {
            onSelectionChangedListener.onSelectionChanged(this, selStart, selEnd);
        }
        super.onSelectionChanged(selStart, selEnd);
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener onSelectionChangedListener) {
        this.onSelectionChangedListener = onSelectionChangedListener;
    }

    public OnSelectionChangedListener getOnSelectionChangedListener() {
        return onSelectionChangedListener;
    }

    @Override
    public int getSelectionStart() {
        return selectionStart;
    }

    @Override
    public int getSelectionEnd() {
        return selectionEnd;
    }

    public interface OnSelectionChangedListener {
        void onSelectionChanged(SelectionEditText view, int start, int end);
    }
}

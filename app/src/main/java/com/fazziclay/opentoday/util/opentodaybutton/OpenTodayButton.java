package com.fazziclay.opentoday.util.opentodaybutton;

import android.content.Context;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.AbsSavedState;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.fazziclay.opentoday.util.Logger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class OpenTodayButton<B extends View, T extends View> extends RelativeLayout {
    private static final String TAG = "OpenTodayButton";
    private B button;
    private T indicator;

    public OpenTodayButton(Context context) {
        super(context);
        init(context, null);
    }

    public OpenTodayButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public OpenTodayButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public OpenTodayButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    protected void setButton(B b) {
        this.button = b;
    }

    protected void setIndicator(T t) {
        this.indicator = t;
    }

    protected abstract void initView(Context context, AttributeSet attrs);

    protected void initIndicator(Context context, AttributeSet attrs, T indicator) {
        var params = new RelativeLayout.LayoutParams(64, 64);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        params.addRule(ALIGN_PARENT_END);
        params.setMargins(10, 10, 10, 10);
        indicator.setLayoutParams(params);
    }

    protected void initButton(Context context, AttributeSet attrs, B button) {
        var params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        button.setPadding(0, 32, 75, 32);
        button.setLayoutParams(params);
    }

    protected void init(@NotNull Context context, @Nullable AttributeSet attrs) {
        Logger.d(TAG, "init() attrs="+attrs);
        initView(context, attrs);

        // indicator
        initIndicator(context, attrs, indicator);

        // button
        initButton(context, attrs, button);

        button.setElevation(0f);
        addView(button);

        indicator.setElevation(15f);
        addView(indicator);
    }

    public B getButton() {
        return button;
    }

    public T getIndicator() {
        return indicator;
    }

    @androidx.annotation.Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        var v = super.onSaveInstanceState();
        Logger.d(TAG, "onSaveInstanceState() returned v="+v + "v.s="+((AbsSavedState)v).getSuperState());
        return v;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Logger.d(TAG, "onRestoreInstanceState() state=" + state);
        super.onRestoreInstanceState(state);
    }
}

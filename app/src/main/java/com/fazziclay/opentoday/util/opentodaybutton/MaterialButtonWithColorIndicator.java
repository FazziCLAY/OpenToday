package com.fazziclay.opentoday.util.opentodaybutton;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.fazziclay.opentoday.R;
import com.google.android.material.button.MaterialButton;

public class MaterialButtonWithColorIndicator extends OpenTodayButton<MaterialButton, ImageView> {
    private int color;
    private boolean isSet = false;

    public MaterialButtonWithColorIndicator(@NonNull Context context) {
        super(context);
    }

    public MaterialButtonWithColorIndicator(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MaterialButtonWithColorIndicator(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MaterialButtonWithColorIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void initView(Context context, AttributeSet attrs) {
        setButton(new MaterialButton(context, attrs));
        setIndicator(new ImageView(context));
    }

    @Override
    protected void initIndicator(Context context, AttributeSet attrs, ImageView indicator) {
        super.initIndicator(context, attrs, indicator);

        indicator.setForeground(AppCompatResources.getDrawable(context, R.drawable.color_indicator));
        indicator.setScaleType(ImageView.ScaleType.FIT_XY);
        updateSetState();
    }

    @Override
    protected void initButton(Context context, AttributeSet attrs, MaterialButton button) {
        super.initButton(context, attrs, button);
    }

    public void setColor(int color) {
        this.color = color;
        setColorSet(true);
        getIndicator().setImageTintList(ColorStateList.valueOf(color));
    }



    public void setColorSet(boolean set) {
        this.isSet = set;
        updateSetState();
    }

    private void updateSetState() {
        var indicator = getIndicator();
        indicator.setImageResource(!isSet ? R.drawable.close_24px : R.drawable.shape);
        indicator.setBackgroundResource(!isSet ? 0 : com.rarepebble.colorpicker.R.drawable.checker_background);
        if (!isSet) {
            indicator.setImageTintList(ColorStateList.valueOf(Color.RED));
        }
    }

    public int getColor() {
        return color;
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        getButton().setOnClickListener(l);
    }

    public void setText(String s) {
        getButton().setText(s);
    }

    public void setText(int s) {
        getButton().setText(s);
    }
}

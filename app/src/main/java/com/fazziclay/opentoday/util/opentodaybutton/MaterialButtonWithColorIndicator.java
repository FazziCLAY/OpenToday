package com.fazziclay.opentoday.util.opentodaybutton;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.util.ResUtil;
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
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.text, android.R.attr.backgroundTint, android.R.attr.textColor});
        try {
            final MaterialButton button = new MaterialButton(context);
            button.setText(typedArray.getString(0));
            button.setBackgroundTintList(ColorStateList.valueOf(typedArray.getColor(1, ResUtil.getAttrColor(context, R.attr.button_buttonWithColorIndicator))));
            button.setTextColor(typedArray.getColor(2, ResUtil.getAttrColor(context, com.google.android.material.R.attr.colorOnPrimary)));


            setButton(button);
            setIndicator(new ImageView(context));
        } finally {
            typedArray.recycle();
        }

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

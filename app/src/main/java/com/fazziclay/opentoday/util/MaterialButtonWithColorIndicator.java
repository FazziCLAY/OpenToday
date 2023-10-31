package com.fazziclay.opentoday.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.fazziclay.opentoday.R;
import com.google.android.material.button.MaterialButton;

public class MaterialButtonWithColorIndicator extends RelativeLayout {
    private int color;
    private MaterialButton button;
    private ImageView indicator;

    public MaterialButtonWithColorIndicator(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
        button = new MaterialButton(context, attrs);
        indicator = new ImageView(context);

        // indicator
        indicator.setForeground(AppCompatResources.getDrawable(context, R.drawable.color_indicator));
        indicator.setImageResource(R.drawable.shape);
        indicator.setBackgroundResource(com.rarepebble.colorpicker.R.drawable.checker_background);
        indicator.setScaleType(ImageView.ScaleType.FIT_XY);

        var params = new RelativeLayout.LayoutParams(64, 64);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        params.addRule(ALIGN_PARENT_END);
        params.setMargins(10, 10, 10, 10);
        indicator.setLayoutParams(params);

        // button
        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        button.setPadding(0, 32, 75, 32);
        button.setLayoutParams(params);


        button.setElevation(0f);
        addView(button);
        indicator.setElevation(15f);
        addView(indicator);
    }

    public MaterialButtonWithColorIndicator(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MaterialButtonWithColorIndicator(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void setColor(int color) {
        this.color = color;
        indicator.setImageTintList(ColorStateList.valueOf(color));
    }

    @Override
    public void setOnClickListener(@Nullable OnClickListener l) {
        button.setOnClickListener(l);
    }

    public void setText(String s) {
        button.setText(s);
    }

    public void setText(int s) {
        button.setText(s);
    }

    public int getColor() {
        return color;
    }
}

package com.fazziclay.opentoday.gui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;

import com.fazziclay.opentoday.app.ColorHistoryManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.rarepebble.colorpicker.ColorPickerView;

public class ColorPicker {
    private final Context context;
    private int startColor;
    private ColorHistoryManager colorHistoryManager;
    private boolean showHex;
    private boolean showPreview;
    private boolean showAlpha;

    public ColorPicker(Context context) {
        this.context = context;
    }

    public ColorPicker(Context context, int startColor) {
        this.context = context;
        this.startColor = startColor;
    }

    public ColorPicker setting(boolean showHex, boolean showPreview, boolean showAlpha) {
        this.showHex = showHex;
        this.showPreview = showPreview;
        this.showAlpha = showAlpha;
        return this;
    }

    public void showDialog(int title, int negative, int positive, ColorPickerInterface colorPickerInterface) {
        showDialog(context.getString(title), context.getString(negative), context.getString(positive), colorPickerInterface);
    }

    public void showDialog(String title, String negative, String positive, ColorPickerInterface colorPickerInterface) {
        ColorPickerView pickerView = new ColorPickerView(context);
        pickerView.showHex(showHex);
        pickerView.showPreview(showPreview);
        pickerView.showAlpha(showAlpha);
        pickerView.setCurrentColor(startColor);
        pickerView.setOriginalColor(startColor);

        HorizontalScrollView historyHorizontal = new HorizontalScrollView(context);

        LinearLayout dialogLayout = new LinearLayout(context);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.addView(pickerView);

        if (colorHistoryManager != null) {
            ChipGroup history = new ChipGroup(context);
            int[] colors = colorHistoryManager.getHistory(5);
            for (int color : colors) {
                Chip chip = new Chip(context);
                chip.setChipBackgroundColor(ColorStateList.valueOf(color));
                chip.setOnClickListener(v -> pickerView.setCurrentColor(color));
                chip.setText(String.format("#%08x", color));
                history.addView(chip);
            }
            historyHorizontal.addView(history);
        }

        new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(dialogLayout)
                .setNegativeButton(negative, null)
                .setPositiveButton(positive, ((_dialog1, _which) -> {
                    int color = pickerView.getColor();
                    if (colorHistoryManager != null)colorHistoryManager.addColor(color);
                    colorPickerInterface.selected(color);
                }))
                .show();
    }

    public ColorPicker setStartColor(int color) {
        this.startColor = color;
        return this;
    }

    public ColorPicker setColorHistoryManager(ColorHistoryManager colorHistoryManager) {
        this.colorHistoryManager = colorHistoryManager;
        return this;
    }

    public interface ColorPickerInterface {
        void selected(int color);
    }
}

package com.fazziclay.opentoday.gui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;

import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.ColorHistoryManager;
import com.fazziclay.opentoday.app.settings.SettingsManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rarepebble.colorpicker.ColorPickerView;

public class ColorPicker {
    private final Context context;
    private int startColor;
    private SettingsManager settingsManager;
    private ColorHistoryManager colorHistoryManager;
    private int colorHistoryMax = 5;
    private boolean showHex;
    private boolean showPreview;
    private boolean showAlpha;
    private Runnable neutralDialogButtonRunnable = null;
    private String neutralDialogButtonText = null;

    public ColorPicker(Context context) {
        this.context = context;
    }

    public ColorPicker(Context context, int startColor) {
        this.context = context;
        this.startColor = startColor;
        this.settingsManager = App.get(context).getSettingsManager();
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


        LinearLayout dialogLayout = new LinearLayout(context);
        dialogLayout.setOrientation(LinearLayout.VERTICAL);
        dialogLayout.addView(pickerView);

        if (colorHistoryManager != null) {
            ChipGroup history = new ChipGroup(context);
            int[] colors = colorHistoryManager.getHistory(colorHistoryMax);
            for (int color : colors) {
                Chip chip = new Chip(context);
                chip.setChipBackgroundColor(ColorStateList.valueOf(color));
                chip.setOnClickListener(v -> pickerView.setCurrentColor(color));
                chip.setText(String.format("#%08x", color));
                history.addView(chip);
            }
            HorizontalScrollView historyHorizontal = new HorizontalScrollView(context);
            historyHorizontal.addView(history);
            dialogLayout.addView(historyHorizontal);
        }

        AlertDialog.Builder builder = (colorHistoryManager == null ? new MaterialAlertDialogBuilder(context) : new AlertDialog.Builder(context))
                .setTitle(title)
                .setView(dialogLayout)
                .setNegativeButton(negative, null)
                .setPositiveButton(positive, ((_dialog1, _which) -> {
                    int color = pickerView.getColor();
                    if (colorHistoryManager != null)colorHistoryManager.addColor(color);
                    colorPickerInterface.selected(color);
                }));

        if (neutralDialogButtonRunnable != null && neutralDialogButtonText != null) {
            builder.setNeutralButton(neutralDialogButtonText, (_ignore, _ignore0) -> neutralDialogButtonRunnable.run());
        }

        builder.show();
    }

    public ColorPicker setStartColor(int color) {
        this.startColor = color;
        return this;
    }

    public ColorPicker setColorHistoryManager(ColorHistoryManager colorHistoryManager) {
        if (SettingsManager.COLOR_HISTORY_ENABLED.get(settingsManager)) {
            this.colorHistoryManager = colorHistoryManager;
        }
        return this;
    }

    public ColorPicker setColorHistoryMax(int colorHistoryMax) {
        this.colorHistoryMax = colorHistoryMax;
        return this;
    }

    public ColorPicker setNeutralDialogButton(String text, Runnable runnable) {
        this.neutralDialogButtonText = text;
        this.neutralDialogButtonRunnable = runnable;
        return this;
    }

    public ColorPicker setNeutralDialogButton(int resId, Runnable runnable) {
        this.neutralDialogButtonText = context.getString(resId);
        this.neutralDialogButtonRunnable = runnable;
        return this;
    }

    public interface ColorPickerInterface {
        void selected(int color);
    }
}

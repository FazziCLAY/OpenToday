package com.fazziclay.opentoday.app.items.item.filter;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.data.Cherry;

public class IntegerValue extends Value implements Cloneable {
    private int shift = 0;
    private int value = 0;
    private String mode;

    public boolean isFit(int i) {
        boolean isFit = true;
        if (shift != 0) {
            i = i + shift;
        }

        if (mode == null) {
            isFit = i == value;
        } else {
            switch (mode) {
                case "==":
                    isFit = i == value;
                    break;
                case ">":
                    isFit = i > value;
                    break;
                case "<":
                    isFit = i < value;
                    break;
                case ">=":
                    isFit = i >= value;
                    break;
                case "<=":
                    isFit = i <= value;
                    break;

                case "%":
                    if (value != 0) {
                        isFit = i % value == 0;
                    } else {
                        isFit = false;
                    }
                    break;

            }
        }

        return (isInvert() != isFit);
    }

    public Cherry exportCherry() {
        return super.exportCherry()
                .put("value", value)
                .put("mode", mode)
                .put("shift", shift);
    }

    public static IntegerValue importCherry(Cherry cherry) {
        if (cherry == null || cherry.isEmpty()) {
            return null;
        }
        IntegerValue integerValue = new IntegerValue();
        integerValue.value = cherry.optInt("value", 0);
        integerValue.setInvert(cherry.optBoolean("isInvert", false));
        integerValue.mode = cherry.optString("mode", "==");
        integerValue.shift = cherry.optInt("shift", 0);
        return integerValue;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getShift() {
        return shift;
    }

    public void setShift(int shift) {
        this.shift = shift;
    }

    @NonNull
    @Override
    protected IntegerValue clone() throws CloneNotSupportedException {
        return (IntegerValue) super.clone();
    }
}

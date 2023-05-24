package com.fazziclay.opentoday.app.items.item.filter;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.data.Cherry;

public abstract class Value implements Cloneable {
    private boolean isInvert = false;

    public boolean isInvert() {
        return isInvert;
    }

    public void setInvert(boolean invert) {
        isInvert = invert;
    }

    public Cherry exportCherry() {
        return new Cherry()
                .put("isInvert", isInvert);
    }

    @NonNull
    @Override
    protected Value clone() throws CloneNotSupportedException {
        return (Value) super.clone();
    }
}

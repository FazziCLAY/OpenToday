package com.fazziclay.opentoday.app.items.tag;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemTag {
    private @NotNull String name;
    private @Nullable String value;
    private @NotNull ValueType valueType = ValueType.NULL;
    private boolean bind = false;

    public ItemTag(@NonNull String name, @Nullable String value) {
        this.name = name;
        this.value = value;
        recalcValueType();
    }

    private void recalcValueType() {
        if (value == null) {
            valueType = ValueType.NULL;
        } else {
            try {
                Double.valueOf(value);
                valueType = ValueType.NUMBER;
            } catch (Exception e) {
                valueType = ValueType.STRING;
            }
        }
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    public void setValue(@Nullable String value) {
        this.value = value;
        recalcValueType();
    }

    @NotNull
    public ValueType getValueType() {
        return valueType;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Nullable
    public String getValue() {
        return value;
    }

    protected ItemTag copy() {
        return new ItemTag(name, value);
    }

    public void bind() {
        if (bind) {
            throw new RuntimeException("This ItemTag already bind.");
        }
        bind = true;
    }

    public enum ValueType {
        NULL,
        NUMBER,
        STRING
    }
}
